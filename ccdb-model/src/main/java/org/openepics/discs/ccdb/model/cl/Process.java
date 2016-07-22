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

package org.openepics.discs.ccdb.model.cl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.openepics.discs.ccdb.model.ConfigurationEntity;
import org.openepics.discs.ccdb.model.cm.LevelOfCare;

/**
 * Life cycle phase
 * 
 * @author <a href="mailto:vuppala@frib.msu.edu">Vasu Vuppala</a>
 */
@Entity
@Table(name = "cl_process" )
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Process.findAll", query = "SELECT d FROM Process d"),
    @NamedQuery(name = "Process.findByName", query = "SELECT d FROM Process d WHERE d.name = :name")
})
public class Process extends ConfigurationEntity {

    private static final long serialVersionUID = 1L; 

    @Basic(optional = false)
    @NotNull
    @Size(min=1, max=64)
    @Column(name = "name", unique = true)
    private String name;
    
    @Basic(optional = false)
    @NotNull
    @Size(min=1, max=255)
    @Column(name = "description")
    private String description;
    
   
    @Basic(optional = false)
    @Column(name = "loc")
    @Enumerated(EnumType.STRING)
    private LevelOfCare levelOfCare = LevelOfCare.NONE;
    
   
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

    public LevelOfCare getLevelOfCare() {
        return levelOfCare;
    }

    public void setLevelOfCare(LevelOfCare levelOfCare) {
        this.levelOfCare = levelOfCare;
    }
}
