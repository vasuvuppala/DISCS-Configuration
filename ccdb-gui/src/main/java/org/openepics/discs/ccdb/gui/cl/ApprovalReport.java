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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.openepics.discs.ccdb.core.ejb.ChecklistEJB;
import org.openepics.discs.ccdb.core.ejb.SlotEJB;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.SlotPropertyValue;
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
public class ApprovalReport implements Serializable {

    static public class ApprovalReportEntry implements Serializable {
        private Slot slot;
        private String associatedDRR;
        private String associatedARR;
        private String CLstatus; // checklist status
        private String DRRstatus;
        private String ARRstatus;
        private String overallStatus;

        private ApprovalReportEntry() {
        }

        // getters/setters

        public Slot getSlot() {
            return slot;
        }

        public String getAssociatedDRR() {
            return associatedDRR;
        }

        public String getAssociatedARR() {
            return associatedARR;
        }

        public String getCLstatus() {
            return CLstatus;
        }

        public String getDRRstatus() {
            return DRRstatus;
        }

        public String getARRstatus() {
            return ARRstatus;
        }

        public String getOverallStatus() {
            return overallStatus;
        }       
    }

    @EJB
    private  ChecklistEJB lcEJB;
    @EJB
    private  SlotEJB slotEJB;

    private static final Logger LOGGER = Logger.getLogger(ApprovalReport.class.getName());
     
    // ToDo: remove hardcoded process and property names
    private final static String CL_PROCESS = "AM-OK";
    private final static String DRR_PROCESS = "DRR";
    private final static String ARR_PROCESS = "ARR";
    private final static String DRR_PROPERTY = "AssociatedDRR";
    private final static String ARR_PROPERTY = "AssociatedARR";
    
    private Boolean showAllSlots = false;
    // view data
    private List<ProcessStatus> statusList;
    private List<ApprovalReportEntry> filteredEntries;
    private List<ApprovalReportEntry> entries;
    private List<Slot> slots;

    public ApprovalReport() {

    }

    /**
     *
     */
    @PostConstruct
    public void init() {
        Checklist checklist = lcEJB.findDefaultChecklist(ChecklistEntity.SLOT);
        if (checklist == null) {
            statusList = lcEJB.findAllStatuses();           
        } else {
            statusList = lcEJB.findAllStatuses(checklist);           
        } 
        initialize();
    }

    /**
     * Overall status report
     *
     * ToDo: really preliminary. pretty bad. improve performance. do not compute entry for each slot.
     *
     * @return
     */
    public String initialize() {
        String nextView = null;
       
        slots = showAllSlots ? slotEJB.findNonSystemSlots() : slotEJB.findByIsHostingSlot(true);
        entries = slots.stream().map(s -> toApprovalEntry(s)).collect(Collectors.toList());
        return nextView;
    }

    private ProcessStatus getStatusRec(Slot slot, Process phase) {

        if (slot == null || phase == null) {
            return null;
        }

        for (ProcessStatus lcstat : statusList) {
            if (slot.getCmGroup() != null) {
                return getGroupStatusRec(slot.getCmGroup(), phase);
            }
            if (slot.equals(lcstat.getAssignment().getSlot()) && phase.equals(lcstat.getField().getProcess())) {
                return lcstat;
            }
        }

        return null;
    }

    private String slotProperty(Slot slot, String property) {
        SlotPropertyValue value = slotEJB.getPropertyValue(slot, property);
        return value == null? "": (value.getPropValue() == null? "": value.getPropValue().toString());
    }
    
    private ProcessStatus getGroupStatusRec(SlotGroup group, Process phase) {
        if (group == null || phase == null) {
            return null;
        }
        for (ProcessStatus lcstat : statusList) {
            if (group.equals(lcstat.getAssignment().getSlotGroup()) && phase.equals(lcstat.getField().getProcess())) {
                return lcstat;
            }
        }

        return null;
    }

    private Boolean approvalStatus(Slot slot, String name) {
        Process selectedProcess = lcEJB.findPhaseByName(name);
        if (selectedProcess == null) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Invalid process ", name);
            LOGGER.log(Level.SEVERE, "Invalid process {0}", name);
            return null;
        }
        Approval approval = lcEJB.findApproval(selectedProcess, slot);      
        return approval == null? false: approval.getApproved();
    }

    private ProcessStatus checklistStatus(Slot slot) {
        Process selectedProcess = lcEJB.findPhaseByName(CL_PROCESS);
        if (selectedProcess == null) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Invalid process  ", CL_PROCESS);
            return null;
        }
        return getStatusRec(slot, selectedProcess);       
    }

    private ApprovalReportEntry toApprovalEntry(Slot slot) {
        ApprovalReportEntry are = new ApprovalReportEntry();
        are.slot = slot;
        
        are.associatedDRR = slotProperty(slot, DRR_PROPERTY);
        are.associatedARR = slotProperty(slot, ARR_PROPERTY);
        
        ProcessStatus CLproc = checklistStatus(slot);
        Boolean CLresult = CLproc == null ? null: CLproc.getStatus().getLogicalValue();        
        are.CLstatus = CLproc == null ? "Undefined" :  CLproc.getStatus().getDescription();
        
        Boolean DRRresult = approvalStatus(slot, DRR_PROCESS);
        Boolean ARRresult = approvalStatus(slot, ARR_PROCESS);
        are.DRRstatus = DRRresult == null ? "Undefined" : (DRRresult? "Approved" : "Not Approved");
        are.ARRstatus = ARRresult == null ? "Undefined" : (ARRresult? "Approved" : "Not Approved");

        boolean overall = CLresult != null && DRRresult != null && ARRresult != null && CLresult && DRRresult && ARRresult;
        are.overallStatus = overall ? "Approved" : "Not Approved";
        
        return are;
    }

    // getters and setters

    public List<ApprovalReportEntry> getEntries() {
        return entries;
    }

    public Boolean getShowAllSlots() {
        return showAllSlots;
    }

    public void setShowAllSlots(Boolean showAllSlots) {
        this.showAllSlots = showAllSlots;
    }

    public List<ApprovalReportEntry> getFilteredEntries() {
        return filteredEntries;
    }

    public void setFilteredEntries(List<ApprovalReportEntry> filteredEntries) {
        this.filteredEntries = filteredEntries;
    }
    
}
