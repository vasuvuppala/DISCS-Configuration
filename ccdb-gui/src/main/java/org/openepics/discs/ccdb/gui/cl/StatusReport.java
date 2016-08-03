package org.openepics.discs.ccdb.gui.cl;

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


import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.openepics.discs.ccdb.core.ejb.ChecklistEJB;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.Device;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.cl.Approval;
import org.openepics.discs.ccdb.model.cl.Process;
import org.openepics.discs.ccdb.model.cl.ProcessStatus;
import org.openepics.discs.ccdb.model.cl.Checklist;
import org.openepics.discs.ccdb.model.cl.ChecklistEntity;
import org.openepics.discs.ccdb.model.cl.SlotGroup;

/**
 * Bean for status reports
 *
 * @author vuppala
 *
 */
@Named
@ViewScoped
public class StatusReport implements Serializable {

//    static public class ColumnModel implements Serializable {
// 
//        private String header;
//        private String property;
// 
//        public ColumnModel(String header, String property) {
//            this.header = header;
//            this.property = property;
//        }
// 
//        public String getHeader() {
//            return header;
//        }
// 
//        public String getProperty() {
//            return property;
//        }
//    }
    
    private static final Logger LOGGER = Logger.getLogger(StatusReport.class.getName());
    @EJB
    private ChecklistEJB lcEJB;
    
    // request parameter
    private ChecklistEntity selectedType = ChecklistEntity.GROUP;
    
    // view data
    private List<ProcessStatus> statusList;
    private List<ProcessStatus> filteredStatus;
    private List<Process> phases;
    private Set<SlotGroup> slotGroups;
    private Set<Slot> slots;
    private Set<Device> devices;
    
    public StatusReport() {
        
    }

    /**
     *
     */
    @PostConstruct
    public void init() {
        // slotGroups = lcEJB.findAllSlotGroups();
        initialize();        
    }

    /**
     * Overall status report
     * 
     * ToDo: really preliminary. pretty bad. improve.
     * 
     * @return 
     */
    public String initialize() {
        String nextView = null;
        Checklist checklist = lcEJB.findDefaultChecklist(selectedType);
        
        if (checklist == null) {
            statusList = lcEJB.findAllStatuses();
            phases = lcEJB.findAllPhases();
        } else {                 
            statusList = lcEJB.findAllStatuses(checklist); 
            phases = lcEJB.findPhases(checklist);
        }
        slotGroups = statusList.stream().filter(stat -> stat.getAssignment().getSlotGroup() != null).map(stat -> stat.getAssignment().getSlotGroup()).collect(Collectors.toSet());
        slots = statusList.stream().filter(stat -> stat.getAssignment().getSlot() != null).map(stat -> stat.getAssignment().getSlot()).collect(Collectors.toSet());
        if (slotGroups != null && !slotGroups.isEmpty()) slots.addAll(lcEJB.findSlots(slotGroups));
 
        devices = statusList.stream().filter(stat -> stat.getAssignment().getDevice() != null).map(stat -> stat.getAssignment().getDevice()).collect(Collectors.toSet());

        return nextView;
    }
    

    public ProcessStatus getStatusRec(Slot slot, Process phase) {
       
          if (slot == null || phase == null ) {
              return null;
          }
          
          for(ProcessStatus lcstat: statusList) {
              if (slot.getCmGroup() != null) {
                  return getGroupStatusRec(slot.getCmGroup(), phase);
              }
              if (slot.equals(lcstat.getAssignment().getSlot()) && phase.equals(lcstat.getField().getProcess())) {
                  return lcstat;
             }
          }
          
        return null;
    }
    
    public ProcessStatus getGroupStatusRec(SlotGroup group, Process phase) {
       
          if (group == null || phase == null ) {
              return null;
          }
          
          for(ProcessStatus lcstat: statusList) {
              if (group.equals(lcstat.getAssignment().getSlotGroup()) && phase.equals(lcstat.getField().getProcess())) {
                  return lcstat;
             }
          }
          
        return null;
    }
    

    public ProcessStatus getDeviceStatusRec(Device device, Process phase) {
       
          if (device == null || phase == null ) {
              return null;
          }
          
          for(ProcessStatus lcstat: statusList) {
              if (device.equals(lcstat.getAssignment().getDevice()) && phase.equals(lcstat.getField().getProcess())) {
                  return lcstat;
             }
          }
          
        return null;
    }
    
    /**
     * approval status
     * 
     * @param processName
     * @param slot
     * @return 
     */
    public String approvalStatus(String processName, Slot slot) {       
        Process selectedProcess = lcEJB.findPhaseByName(processName);
        if (selectedProcess == null) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Invalid process name", "not process found");
        }
        
        if ("AM-OK".equals(processName)) {
            ProcessStatus status = getStatusRec(slot, selectedProcess);
            if (status == null) return "Not Assigned";
            return status.getStatus().getDescription();
        } else {
            Approval approval = lcEJB.findApproval(selectedProcess, slot);
            if (approval == null) return "Not Approved";
            return approval.getApproved() ? "Approved" : "Not Approved"; 
        }
         
    }
    // getters and setters

    public List<ProcessStatus> getFilteredStatus() {
        return filteredStatus;
    }

    public void setFilteredStatus(List<ProcessStatus> filteredStatus) {
        this.filteredStatus = filteredStatus;
    }

    public ChecklistEntity getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(ChecklistEntity selectedType) {
        this.selectedType = selectedType;
    }

    public List<Process> getPhases() {
        return phases;
    }

    public Set<Slot> getSlots() {
        return slots;
    }   

    public Set<Device> getDevices() {
        return devices;
    }

    public Set<SlotGroup> getSlotGroups() {
        return slotGroups;
    }
}
