/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openepics.discs.ccdb.core.ejb;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.openepics.discs.ccdb.model.order.Element;
import org.openepics.discs.ccdb.model.order.Lattice;
import org.openepics.discs.ccdb.model.order.LatticeEdge;


/**
 *
 * @author vuppala
 */
@Stateless
public class LatticeEJB  {

    private static final Logger LOGGER = Logger.getLogger(LatticeEJB.class.getName());   
    @PersistenceContext
    private EntityManager em;        
    
    // ----------------  Layout Slot  -------------------------
   
    public List<Lattice> findAllLattices() {
        List<Lattice> lats = em.createNamedQuery("Lattice.findAll", Lattice.class).getResultList();
        LOGGER.log(Level.INFO, "Number of lattices: {0}", lats.size());
        
        return lats;
    }
    
     /**
     * find the default lattice.
     * 
     * @return 
     */
    public Lattice findDefaultLattice() {
        List<Lattice> lats = em.createNamedQuery("Lattice.findAll", Lattice.class)
                .setMaxResults(1)
                .getResultList();
        if (lats == null || lats.isEmpty()) {
            return null;
        }
        return lats.get(0);
    }
    
    /**
     * Find a lattice
     * 
     * @param id
     * @return 
     */
    public Lattice findLattice(Long id) {
        return em.find(Lattice.class, id);
    }
   
    
    /**
     * find elements of a lattice
     * 
     * @param lattice
     * @return 
     */
    public List<Element> findElements(Lattice lattice) {
        return em.createNamedQuery("Element.findByLattice",Element.class).setParameter("lattice", lattice).getResultList();
    }
    
    public List<Element> findElements(String name) {
        return em.createNamedQuery("Element.findByName",Element.class).setParameter("name", name).getResultList();
    }
    
    public List<Element> findAllElements() {
        return em.createNamedQuery("Element.findAll",Element.class).getResultList();
    }
    
    /**
     * Find an element
     * 
     * @param id
     * @return 
     */
    public Element findElement(Long id) {
        return em.find(Element.class, id);
    }
   
    
    /* 
     * find all edges of a lattice
     *
     */
    public List<LatticeEdge> findElementPairs(Lattice lattice) {
        List<LatticeEdge> pairs = em.createNamedQuery("LatticeEdge.findLattice", LatticeEdge.class)
                .setParameter("lattice", lattice).getResultList();
        
        LOGGER.log(Level.INFO, "findElementPairs: No of related pairs {0}", pairs.size());
        return pairs;
    }    
    
    /* 
     * find all root nodes
     *
     */
    public List<Element> findRootElements(Lattice lattice) {
        List<Element> roots = em.createNamedQuery("LatticeEdge.findRoots", Element.class)
                .setParameter("lattice", lattice).getResultList();       
        
        LOGGER.log(Level.INFO, "findRootElements: Number of root slots {0}", roots.size());
        return roots;
    }
    

    /* 
     * find all leaf elements of a lttice
     *
     */
    public List<Element> findLeafElements(Lattice lattice) {
        List<Element> leafs = em.createNamedQuery("LatticeEdge.findLeafs", Element.class)
                .setParameter("lattice", lattice).getResultList();       
        
        LOGGER.log(Level.INFO, "findLeafElements: Number of leaf slots {0}", leafs.size());
        return leafs;
    }
    
}
