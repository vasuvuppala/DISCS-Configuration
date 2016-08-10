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

package org.openepics.discs.ccdb.model;

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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Process variable associated with a slot
 * 
 * @author <a href="mailto:vuppala@frib.msu.edu">Vasu Vuppala</a>
 */
@Entity
@Table(name = "process_variable" )
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ProcessVariable.findAll", query = "SELECT d FROM ProcessVariable d"),
    @NamedQuery(name = "ProcessVariable.findBySlot", query = "SELECT d FROM ProcessVariable d WHERE d.slot = :slot"), 
    @NamedQuery(name = "ProcessVariable.findByName", query = "SELECT d FROM ProcessVariable d WHERE d.name = :name")
})
public class ProcessVariable implements Serializable {

    private static final long serialVersionUID = 1L; 

    @Id   
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    
    @Basic(optional = false)
    @NotNull
    @Size(min=1, max=256)
    @Column(name = "name", unique = true)
    private String name;
    
    @Basic(optional = false)
    @NotNull
    @Size(min=1, max=1024)
    @Column(name = "description")
    private String description;
       
    @ManyToOne(optional = false)
    @NotNull
    @JoinColumn(name = "slot")
    private Slot slot;

    @ManyToOne(optional = true)
    @JoinColumn(name = "property")
    private Property property;
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if ((object == null) || (object.getClass() != this.getClass())) {
            return false;
        }

        ConfigurationEntity other = (ConfigurationEntity) object;
        if (this.id == null && other.id != null) {
            return false;
        }

        // return true for the same DB entity
        if (this.id != null) {
            return this.id.equals(other.id);
        }

        return this==object;
    }
    
    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + "[ id=" + id + " ]";
    }

    // getters and setters

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

    public Long getId() {
        return id;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

}
