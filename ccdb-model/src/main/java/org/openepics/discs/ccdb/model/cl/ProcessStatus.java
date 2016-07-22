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
import javax.validation.constraints.Size;
import org.openepics.discs.ccdb.model.ConfigurationEntity;
import org.openepics.discs.ccdb.model.auth.User;

/**
 * Status of a checklist assignment 
 * 
 * @author <a href="mailto:vuppala@frib.msu.edu">Vasu Vuppala</a>
 */
@Entity
@Table(name = "cl_proc_status",
       uniqueConstraints=@UniqueConstraint(columnNames={"assignment", "field"}))
@NamedQueries({
    @NamedQuery(name = "ProcessStatus.findAll", query = "SELECT d FROM ProcessStatus d"),
    @NamedQuery(name = "ProcessStatus.findValid", query = "SELECT d FROM ProcessStatus d"),
    @NamedQuery(name = "ProcessStatus.findGroupStatus", query = "SELECT d FROM ProcessStatus d WHERE d.assignment.slotGroup IS NOT null"),
    @NamedQuery(name = "ProcessStatus.findSlotStatus", query = "SELECT d FROM ProcessStatus d WHERE d.assignment.slot IS NOT null"),
    @NamedQuery(name = "ProcessStatus.findDeviceStatus", query = "SELECT d FROM ProcessStatus d WHERE d.assignment.slot IS null AND d.assignment.device IS NOT null"),
    @NamedQuery(name = "ProcessStatus.findByChecklist", query = "SELECT d FROM ProcessStatus d WHERE d.field.checklist = :checklist"),
    @NamedQuery(name = "ProcessStatus.findByAssignment", query = "SELECT d FROM ProcessStatus d WHERE d.assignment = :assignment")
})
public class ProcessStatus extends ConfigurationEntity {

    private static final long serialVersionUID = 1L;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "assignment")
    private Assignment assignment;
       
    @ManyToOne(optional = false)
    @JoinColumn(name = "field")
    private ChecklistField field;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "assigned_sme")
    private User assignedSME;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "status")
    private StatusOption status;    
    
    @Column(name = "comment")
    @Size(max = 1024)
    @Basic
    private String comment;
    // --

    public StatusOption getStatus() {
        return status;
    }

    public void setStatus(StatusOption status) {
        this.status = status;
    }  

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public User getAssignedSME() {
        return assignedSME;
    }

    public void setAssignedSME(User assignedSME) {
        this.assignedSME = assignedSME;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ChecklistField getField() {
        return field;
    }

    public void setField(ChecklistField field) {
        this.field = field;
    }

}
