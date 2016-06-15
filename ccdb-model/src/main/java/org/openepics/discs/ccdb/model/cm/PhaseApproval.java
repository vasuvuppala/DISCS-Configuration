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

package org.openepics.discs.ccdb.model.cm;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.openepics.discs.ccdb.model.ConfigurationEntity;
import org.openepics.discs.ccdb.model.auth.User;

/**
 * Approval for the completion of a phase
 * 
 * @author <a href="mailto:vuppala@frib.msu.edu">Vasu Vuppala</a>
 */
@Entity
@Table(name = "cm_phase_approval")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PhaseApproval.findAll", query = "SELECT d FROM PhaseApproval d"),
    @NamedQuery(name = "PhaseApproval.findByType", query = "SELECT d FROM PhaseApproval d WHERE d.assignment.phaseGroup = :group"),
    @NamedQuery(name = "PhaseApproval.findByName", query = "SELECT d FROM PhaseApproval d WHERE d.assignment = :assignment")
})
public class PhaseApproval extends ConfigurationEntity {

    private static final long serialVersionUID = 1L;
   
    @ManyToOne(optional = false)
    @JoinColumn(name = "assignment")
    private PhaseAssignment assignment;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "phaseOfGroup")
    private PhaseGroupMember phaseOfGroup;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_approver")
    private User assignedApprover;
    
    @Column(name = "approved")
    @Basic(optional = false)
    private boolean approved = false;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "status")
    private StatusOption status;
    
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approved_by;
    
    @Column(name = "approved_at")
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private Date approved_at;
    
    @Column(name = "comment")
    @Size(max = 1024)
    @Basic
    private String comment;
    
    // --

    public PhaseAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(PhaseAssignment assignment) {
        this.assignment = assignment;
    }

    public User getAssignedApprover() {
        return assignedApprover;
    }

    public void setAssignedApprover(User assignedApprover) {
        this.assignedApprover = assignedApprover;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public User getApproved_by() {
        return approved_by;
    }

    public void setApproved_by(User approved_by) {
        this.approved_by = approved_by;
    }

    public Date getApproved_at() {
        return approved_at;
    }

    public void setApproved_at(Date approved_at) {
        this.approved_at = approved_at;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public StatusOption getStatus() {
        return status;
    }

    public void setStatus(StatusOption status) {
        this.status = status;
    }

    public PhaseGroupMember getPhaseOfGroup() {
        return phaseOfGroup;
    }

    public void setPhaseOfGroup(PhaseGroupMember phaseOfGroup) {
        this.phaseOfGroup = phaseOfGroup;
    }
    
}