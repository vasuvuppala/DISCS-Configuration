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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import org.openepics.discs.ccdb.model.ConfigurationEntity;
import org.openepics.discs.ccdb.model.auth.Role;

/**
 * A field in a checklist
 * 
 * @author <a href="mailto:vuppala@frib.msu.edu">Vasu Vuppala</a>
 */
@Entity
@Table(name = "cm_checklist_field", 
       uniqueConstraints=@UniqueConstraint(columnNames={"process", "checklist"}))
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ChecklistField.findAll", query = "SELECT d FROM ChecklistField d"),
    @NamedQuery(name = "ChecklistField.findPhasesByChecklist", query = "SELECT d.process FROM ChecklistField d WHERE d.checklist = :checklist ORDER BY d.position ASC"),
    @NamedQuery(name = "ChecklistField.findDefault", query = "SELECT d.defaultStatus FROM ChecklistField d WHERE d.checklist = :checklist AND d.process = :process"),
    @NamedQuery(name = "ChecklistField.findByChecklist", query = "SELECT d FROM ChecklistField d WHERE d.checklist = :checklist ORDER BY d.position ASC")
})
public class ChecklistField extends ConfigurationEntity {

    private static final long serialVersionUID = 1L;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "process")
    private Process process;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "checklist")
    private Checklist checklist;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "sme")
    private Role sme; // Subject Matter Expert
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "summary_proc")
    private Boolean summaryProcess = false; // is this a summary inspection (like 'AM OK')?
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "optional")
    private Boolean optional = true; // can the status option be null for this phase?
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "default_status")
    private StatusOption defaultStatus;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "position")
    private Integer position = 0; // position in the group (used for ordering the phases in a group)
    
    // --

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public Checklist getChecklist() {
        return checklist;
    }

    public void setChecklist(Checklist checklist) {
        this.checklist = checklist;
    }

    public Boolean getSummaryProcess() {
        return summaryProcess;
    }

    public void setSummaryProcess(Boolean summaryProcess) {
        this.summaryProcess = summaryProcess;
    }


    public Role getSme() {
        return sme;
    }

    public void setSme(Role sme) {
        this.sme = sme;
    }

    
    public Boolean getOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public StatusOption getDefaultStatus() {
        return defaultStatus;
    }

    public void setDefaultStatus(StatusOption defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
    
}
