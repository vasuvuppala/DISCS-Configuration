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
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.openepics.discs.ccdb.model.ConfigurationEntity;
import org.openepics.discs.ccdb.model.Device;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.auth.User;

/**
 * Assignment of a checklist to a slot, device or group.
 * ToDo: Not optimal  design. 
 *       Constraints: Either a device, a group, or a slot must be present (not null). 
 *                    It is possible to have slot and device not null (installed device). 
 *                    There can be only one checklist per entity (slot, group, device).
 *                    These checks cannot be made in the database.
 * 
 * @author <a href="mailto:vuppala@frib.msu.edu">Vasu Vuppala</a>
 */
@Entity
@Table(name = "cl_approval")
@NamedQueries({
    @NamedQuery(name = "Approval.findAll", query = "SELECT d FROM Approval d"),
    @NamedQuery(name = "Approval.findBySlotProc", query = "SELECT d FROM Approval d WHERE d.slot = :slot AND d.process = :process"),
    @NamedQuery(name = "Approval.findBySlots", query = "SELECT d FROM Approval d WHERE d.slot IN :slots AND d.process = :process"),
    @NamedQuery(name = "Approval.findBySlot", query = "SELECT d FROM Approval d WHERE d.slot = :slot")
})
public class Approval extends ConfigurationEntity {

    private static final long serialVersionUID = 1L;
       
    @ManyToOne(optional = false)
    @JoinColumn(name = "slot")
    private Slot slot;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "process")
    private Process process;
    
    @Basic(optional=false)
    private Boolean approved = false;
    
    // getters and setters

   
    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
}
