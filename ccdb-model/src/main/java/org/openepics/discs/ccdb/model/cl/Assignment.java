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
@Table(name = "cm_assignment")
@NamedQueries({
    @NamedQuery(name = "Assignment.findAll", query = "SELECT d FROM Assignment d"),
    @NamedQuery(name = "Assignment.findGroupAssignments", query = "SELECT d FROM Assignment d WHERE d.slotGroup IS NOT null"),
    @NamedQuery(name = "Assignment.findSlotAssignments", query = "SELECT d FROM Assignment d WHERE d.slot IS NOT null AND d.slot.cmGroup IS NULL"),
    @NamedQuery(name = "Assignment.findAllSlotAssignments", query = "SELECT d FROM Assignment d WHERE d.slot IS NOT null"),
    @NamedQuery(name = "Assignment.findDeviceAssignments", query = "SELECT d FROM Assignment d WHERE d.slot IS null AND d.device IS NOT null"),
    @NamedQuery(name = "Assignment.findBySlotChecklist", query = "SELECT d FROM Assignment d WHERE d.slot  IN :slots AND d.checklist = :checklist"),
    @NamedQuery(name = "Assignment.findByGroupChecklist", query = "SELECT d FROM Assignment d WHERE d.slotGroup  IN :groups AND d.checklist = :checklist"),
    @NamedQuery(name = "Assignment.findByDeviceChecklist", query = "SELECT d FROM Assignment d WHERE d.device  IN :devices AND d.checklist = :checklist"),
    @NamedQuery(name = "Assignment.findByGroup", query = "SELECT d FROM Assignment d WHERE d.checklist = :checklist"),
    @NamedQuery(name = "Assignment.findBySlotGroup", query = "SELECT d FROM Assignment d WHERE d.slotGroup = :group"),   
    @NamedQuery(name = "Assignment.numberOfAssignedSlots", query = "SELECT COUNT(DISTINCT d.slot) FROM Assignment d WHERE d.slot IN :slots"),    
    @NamedQuery(name = "Assignment.numberOfAssignedGroups", query = "SELECT COUNT(DISTINCT d.slotGroup) FROM Assignment d WHERE d.slotGroup IN :groups"),    
    @NamedQuery(name = "Assignment.numberOfAssignedDevices", query = "SELECT COUNT(DISTINCT d.device) FROM Assignment d WHERE d.device IN :devices"),    
    @NamedQuery(name = "Assignment.findBySlot", query = "SELECT d FROM Assignment d WHERE d.slot = :slot"),
    @NamedQuery(name = "Assignment.findByDevice", query = "SELECT d FROM Assignment d WHERE d.device = :device")
})
public class Assignment extends ConfigurationEntity {

    private static final long serialVersionUID = 1L;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "slot_group")
    private SlotGroup slotGroup;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "slot")
    private Slot slot;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "device")
    private Device device;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "checklist")
    private Checklist checklist;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "requestor")
    private User requestor; 
    
    @OneToMany(cascade = {CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, mappedBy = "assignment")
    private List<ProcessStatus> statuses;
    
    // getters and setters

    public User getRequestor() {
        return requestor;
    }

    public void setRequestor(User requestor) {
        this.requestor = requestor;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public Checklist getChecklist() {
        return checklist;
    }

    public void setChecklist(Checklist checklist) {
        this.checklist = checklist;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public SlotGroup getSlotGroup() {
        return slotGroup;
    }

    public void setSlotGroup(SlotGroup slotGroup) {
        this.slotGroup = slotGroup;
    }

    public List<ProcessStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<ProcessStatus> statuses) {
        this.statuses = statuses;
    }
    
}
