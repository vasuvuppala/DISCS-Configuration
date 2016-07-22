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

import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.openepics.discs.ccdb.model.ConfigurationEntity;

/**
 * A checklist
 * 
 * @author <a href="mailto:vuppala@frib.msu.edu">Vasu Vuppala</a>
 */
@Entity
@Table(name = "cl_checklist" )
@NamedQueries({
    @NamedQuery(name = "Checklist.findAll", query = "SELECT d FROM Checklist d"),
    @NamedQuery(name = "Checklist.findDefaultForSlots", query = "SELECT d FROM Checklist d WHERE d.forDevices = FALSE AND d.defaultList = TRUE"),
    @NamedQuery(name = "Checklist.findDefaultForDevices", query = "SELECT d FROM Checklist d WHERE d.forDevices = TRUE AND d.defaultList = TRUE"),
    @NamedQuery(name = "Checklist.findByName", query = "SELECT d FROM Checklist d WHERE  d.name = :name")
})
public class Checklist extends ConfigurationEntity {

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
    @NotNull
    @Column(name = "default_list")
    private Boolean defaultList = false; // is it the default list?
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "for_devices")
    private Boolean forDevices = false; // true: the list applies to devices. False: for slots and groups
    
    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, mappedBy = "checklist")
    private List<StatusOption> options;
  
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy="checklist")
    private List<ChecklistField> fields; // fields in a checklist
    
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

    public List<StatusOption> getOptions() {
        return options;
    }

    public void setOptions(List<StatusOption> options) {
        this.options = options;
    }

    public List<ChecklistField> getFields() {
        return fields;
    }

    public void setFields(List<ChecklistField> fields) {
        this.fields = fields;
    } 

    public Boolean getDefaultList() {
        return defaultList;
    }

    public void setDefaultList(Boolean defaultList) {
        this.defaultList = defaultList;
    }

    public Boolean getForDevices() {
        return forDevices;
    }

    public void setForDevices(Boolean forDevices) {
        this.forDevices = forDevices;
    }
}
