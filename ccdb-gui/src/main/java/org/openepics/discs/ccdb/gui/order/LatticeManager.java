/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2012.
 * 
 * You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *       http://www.gnu.org/licenses/gpl.txt
 * 
 * Contact Information:
 *   Facilitty for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 * 
 */
package org.openepics.discs.ccdb.gui.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import org.jgrapht.graph.*;
import org.jgrapht.*;
import org.jgrapht.alg.DijkstraShortestPath;
import org.openepics.discs.ccdb.core.ejb.LatticeEJB;
import org.openepics.discs.ccdb.core.ejb.SlotEJB;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.SlotPropertyValue;
import org.openepics.discs.ccdb.model.order.Element;
import org.openepics.discs.ccdb.model.order.Lattice;
import org.openepics.discs.ccdb.model.order.LatticeEdge;
import org.openepics.discs.ccdb.model.values.Value;

/**
 * Bean for extracting elements from a lattice.
 * 
 * ToDo: Use dynamic columns.
 * 
 * @author vuppala
 */
@Named
@ViewScoped
public class LatticeManager implements Serializable {

    /*
     * Path of a beam.
     * 
     */
    public class Path {
        
        private final String name;
        private Element start; 
        private Element end;
        private List<Element> path;
        
        public Path(String name) {
            this.name = name;
        }
        
        public void findPath(DirectedGraph graph, Element start, Element end) {
            this.start = start;
            this.end = end;
            DijkstraShortestPath graphPath = new DijkstraShortestPath(graph, start, end);
            path = Graphs.getPathVertexList(graphPath.getPath());
        }
        
//        public void addBeampath(Path bpath) {
//            if (this.start == null) {
//                this.start = bpath.start;
//            }
//            this.end = bpath.end;
//            if (this.path == null) {
//                path = new ArrayList(bpath.path);
//            } else {
//                this.path.addAll(bpath.path);
//            }
//        }
               
        public String getName() {
            return name;
        }
        
        public Element getStart() {
            return start;
        }
        
        public Element getEnd() {
            return end;
        }
        
        public List<Element> getPath() {
            return path;
        }
        
    }
    
    @EJB
    private LatticeEJB latticeEJB;
     @EJB
    private SlotEJB slotEJB;
    
    private static final Logger LOGGER = Logger.getLogger(LatticeManager.class.getName());
    
    private List<Lattice> lattices;
    private List<Element> filteredObjects;
    // private List<String> beamlineNames;
    private List<Element> slotElements;
    private List<Slot> slots;
    
    private DirectedGraph<Element, DefaultEdge> latticeGraph; // graph of the lattice

    // Input form fields
    private Element inpStartElement;
    private Element inpEndElement;
    private Lattice inpLattice;
//    private String inpStartSection;
//    private String inpEndSection;
    
    // output
    private List<Path> resultBeampaths;

    /**
     * Creates a new instance of LatticeView
     */
    public LatticeManager() {
    }
    
    @PostConstruct
    public void init() {
        try {
            lattices = latticeEJB.findAllLattices();
            slots = slotEJB.findAll();
            // slotElements = latticeEJB.findAllElements();
            if (lattices.isEmpty()) {
                LOGGER.log(Level.WARNING, "No lattices found in the database");                
            } else {
                inpLattice = lattices.get(0);
            }
            resetInput();         
        } catch (Exception e) {
            System.err.println(e);
            LOGGER.log(Level.SEVERE, "Cannot retrieve source slots");
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Error in getting source slots", " ");
        }
    }

    /**
     * reset data based on input
     * 
     */
    public void resetInput() {
        if (inpLattice != null) slotElements = latticeEJB.findElements(inpLattice);
    }
    
    /**
     *
     * @author vuppala
     */
    private void printSlots(String name, List<Element> elements) {
        LOGGER.log(Level.INFO, "Print elements {0}", name);
        for (Element element : elements) {
            LOGGER.log(Level.INFO, element.getName());
        }
    }

   
    /**
     *
     * @author vuppala
     */
    private void buildLayoutGraph(Lattice lattice) {
        LOGGER.log(Level.INFO, "Creating graph");
        try {
            latticeGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

            // LayoutRelation = slotRelationEJB.findRelation(LayoutRelationName);
            List<LatticeEdge> slotPairs = latticeEJB.findElementPairs(lattice);
            
            for (LatticeEdge sp : slotPairs) {
                // logger.log(Level.INFO, "   Adding nodes");
                latticeGraph.addVertex(sp.getCurrentElement());
                latticeGraph.addVertex(sp.getNextElement());
                // logger.log(Level.INFO, "   Adding edges");
                latticeGraph.addEdge(sp.getCurrentElement(), sp.getNextElement());
            }

            // List<DefaultEdge> path = DijkstraShortestPath.findPathBetween(layoutGraph,"REA_BTS10:ATP_D0862","REA_BTS10:VD_D0892");
            String gr = latticeGraph.toString();
            // System.out.println(gr);
            // logger.log(Level.INFO, "Graph: " + gr);
        } catch (Exception e) {
            System.err.println(e);
            LOGGER.log(Level.SEVERE, "buildLayoutGraph: Cannot build graph");
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "buildLayoutGraph: Cannot build graph", " ");
        }
    }


    /**
     * Generate path from start element to end element
     *
     * @author vuppala
     */
    public void genElementPaths() {
        try {
            LOGGER.log(Level.INFO, "Creating beamline paths");
            resultBeampaths = new ArrayList();
            List<Element> startSlots;
            List<Element> endSlots;
            
            // LayoutRelation = slotRelationEJB.findRelation(LayoutRelationName);
            if (inpStartElement == null) {
                startSlots = latticeEJB.findRootElements(inpLattice);
            } else {
                startSlots = new ArrayList();
                startSlots.add(inpStartElement);
            }
            
            if (inpEndElement == null) {
                endSlots = latticeEJB.findLeafElements(inpLattice);
            } else {
                endSlots = new ArrayList();
                endSlots.add(inpEndElement);
            }
            buildLayoutGraph(inpLattice);
            for (Element source : startSlots) {
                for (Element end : endSlots) {
                    // logger.log(Level.INFO, " root slot for LA " + source.getName());
                    Path path = new Path(source.getName() + " - " + end.getName());
                    path.findPath(latticeGraph, source, end);
                    resultBeampaths.add(path);
                }
            }
            LOGGER.log(Level.INFO, "Number of beamline paths {0}",  resultBeampaths.size());
        } catch (Exception e) {
            System.err.println(e);
            LOGGER.log(Level.SEVERE, "genElementPaths: Cannot find path");
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Sorry, cannot find a path between two elements", " ");
        }
    }


    /**
     * Get property of an element
     * 
     * @param element
     * @param propName
     * @return 
     */
     public Value elementPropertyValue(Element element, String propName) {
        Value pvalue = null;

        if (element == null) {
            return null;
        }
        for (SlotPropertyValue sp : element.getSlot().getSlotPropertyList()) {
            if ( sp.getProperty().getName().equals(propName)) {
                pvalue = sp.getPropValue();
                break;
            }
        }
        return pvalue;
    }
       
        
    // ---- getters and setters
    
    public List<Element> getFilteredObjects() {
        return filteredObjects;
    }
    
    public void setFilteredObjects(List<Element> filteredObjects) {
        this.filteredObjects = filteredObjects;
    }  
    
    public Element getInpStartElement() {
        return inpStartElement;
    }
    
    public void setInpStartElement(Element inpStartElement) {
        this.inpStartElement = inpStartElement;
    }
    
    public Element getInpEndElement() {
        return inpEndElement;
    }
    
    public void setInpEndElement(Element inpEndElement) {
        this.inpEndElement = inpEndElement;
    }
    
    public List<Path> getResultBeampaths() {
        return resultBeampaths;
    }
    
    public Lattice getInpLattice() {
        return inpLattice;
    }

    public void setInpLattice(Lattice inpLattice) {
        this.inpLattice = inpLattice;
    }

    public List<Lattice> getLattices() {
        return lattices;
    }

    public List<Element> getSlotElements() {
        return slotElements;
    }

    public List<Slot> getSlots() {
        return slots;
    }

   
}
