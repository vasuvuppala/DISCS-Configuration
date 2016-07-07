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
package org.openepics.discs.ccdb.model.order;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An edge of a graph made from a set of elements.
 * 
 * @author vuppala
 */
@Entity
@Table(name = "ord_edge",
            uniqueConstraints = @UniqueConstraint(columnNames={"current_element", "next_element"})
)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "LatticeEdge.findAll", query = "SELECT l FROM LatticeEdge l"),
    @NamedQuery(name = "LatticeEdge.findLattice", query = "SELECT l FROM LatticeEdge l WHERE l.currentElement.lattice = :lattice AND l.nextElement.lattice = :lattice"),
    @NamedQuery(name = "LatticeEdge.findPrevious", query = "SELECT l.currentElement FROM LatticeEdge l WHERE l.nextElement = :element AND l.nextElement.lattice = :lattice"),
    @NamedQuery(name = "LatticeEdge.findNext", query = "SELECT l.nextElement FROM LatticeEdge l WHERE l.currentElement = :element AND l.currentElement.lattice = :lattice"),
    @NamedQuery(name = "LatticeEdge.findRoots", query = "SELECT DISTINCT sp.currentElement FROM LatticeEdge sp WHERE sp.currentElement.lattice = :lattice AND sp.currentElement NOT IN (SELECT sc.nextElement FROM LatticeEdge sc WHERE sc.nextElement.lattice = :lattice)"),
    @NamedQuery(name = "LatticeEdge.findLeafs", query = "SELECT DISTINCT sp.nextElement FROM LatticeEdge sp WHERE sp.nextElement.lattice = :lattice AND sp.nextElement NOT IN (SELECT sc.currentElement FROM LatticeEdge sc WHERE sc.currentElement.lattice = :lattice)"),
})
public class LatticeEdge implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    
    @JoinColumn(name = "current_element", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Element currentElement;
        
    @JoinColumn(name = "next_element", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Element nextElement;

    public LatticeEdge() {
    }

    public LatticeEdge(Element current, Element next) {
        currentElement = current;
        nextElement = next;
    }
    
    public LatticeEdge(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Element getCurrentElement() {
        return currentElement;
    }

    public void setCurrentElement(Element currentElement) {
        this.currentElement = currentElement;
    }

    public Element getNextElement() {
        return nextElement;
    }

    public void setNextElement(Element nextElement) {
        this.nextElement = nextElement;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LatticeEdge)) {
            return false;
        }
        LatticeEdge other = (LatticeEdge) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.openepics.discs.conf.ent.LatticeEdge[ id=" + id + " ]";
    }
    
}
