/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 *
 * Controls Configuration Database is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.conf.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import org.openepics.discs.ccdb.jaxrs.PathResource;
import org.openepics.discs.ccdb.model.order.Element;
import org.openepics.discs.ccdb.model.order.Lattice;
import org.openepics.discs.ccdb.model.order.LatticeEdge;
import org.jgrapht.graph.*;
import org.jgrapht.*;
import org.jgrapht.alg.DijkstraShortestPath;
import org.openepics.discs.ccdb.core.ejb.LatticeEJB;
import org.openepics.discs.ccdb.jaxb.ElementRep;
import org.openepics.discs.ccdb.jaxb.PathRep;

/**
 * An implementation of the InstallationSlotResource interface.
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 */
public class PathResourceImpl implements PathResource {
    
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
    
    @EJB LatticeEJB latticeEJB;
    
    private static final Logger LOGGER = Logger.getLogger(PathResourceImpl.class.getName());
    private DirectedGraph<Element, DefaultEdge> latticeGraph; // graph of the lattice
    private List<Path> resultBeampaths;
     
    @Override
    public List<PathRep> getPaths(String start, String end) {
        Element startElement = null, endElement = null;
        
        if ( ! "undefined".equals(start)) {
            startElement = latticeEJB.findElements(start).get(0);      
        }
        if ( ! "undefined".equals(end)) {
            endElement = latticeEJB.findElements(end).get(0);      
        }
        genElementPaths(startElement, endElement);
        if (resultBeampaths == null || resultBeampaths.isEmpty()) {
            return null;
        } else {
            List<PathRep> paths = new ArrayList<>();
            for (Path bpath: resultBeampaths) {
                paths.add(new PathRep(bpath.name, bpath.getPath().stream().map(e -> ElementRep.newInstance(e)).collect(Collectors.toList())));
            }
            return paths;
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
        
        }
    }


    /**
     * Generate path from start element to end element
     *
     * @author vuppala
     * @param inpStartElement
     * @param inpEndElement
     */
    public void genElementPaths(Element inpStartElement, Element inpEndElement) {
        try {
            LOGGER.log(Level.INFO, "Creating beamline paths");
            resultBeampaths = new ArrayList();
            List<Element> startSlots;
            List<Element> endSlots;
            Lattice inpLattice = latticeEJB.findDefaultLattice();
            
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
        }
    }

}
