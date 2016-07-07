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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.openepics.discs.ccdb.model.Slot;

/**
 * An element is a physics-based representation of a slot.
 * 
 * @author vuppala
 */
@Entity
@Table(name = "ord_element")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Element.findAll", query = "SELECT l FROM Element l"),
    @NamedQuery(name = "Element.findByLattice", query = "SELECT l FROM Element l WHERE l.lattice = :lattice"),
    @NamedQuery(name = "Element.findByName", query = "SELECT l FROM Element l WHERE l.name = :name")
})
public class Element implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    
    @JoinColumn(name = "lattice", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Lattice lattice;
    
    @Basic(optional = false)
    @Column(name = "name", unique=true)
    @Size(min=1, max=255)
    private String name;
    
    @Basic(optional = true)
    @Column(name = "description")
    @Size(max=1024)
    private String description;
    
    @JoinColumn(name = "slot", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Slot slot;
           
    public Element() {
    }

    public Element(Lattice lattice, String name, String description, Slot slot) {
        this.lattice = lattice;
        this.name = name;
        this.description = description;
        this.slot = slot;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public Lattice getLattice() {
        return lattice;
    }

    public void setLattice(Lattice lattice) {
        this.lattice = lattice;
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
        if (!(object instanceof Element)) {
            return false;
        }
        Element other = (Element) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.openepics.discs.ccdb.model.order.Element[ id=" + id + " ]";
    }
    
}
