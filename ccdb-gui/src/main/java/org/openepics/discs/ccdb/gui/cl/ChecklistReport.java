/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.discs.ccdb.gui.cl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.openepics.discs.ccdb.core.ejb.ChecklistEJB;
import org.openepics.discs.ccdb.core.ejb.SlotEJB;
import org.openepics.discs.ccdb.model.Slot;

/**
 *
 * @author vuppala
 */
@Named
@ViewScoped
public class ChecklistReport implements Serializable {
    
    @EJB private SlotEJB slotEJB;
    @EJB private ChecklistEJB clEJB;
    
    private static final Logger LOGGER = Logger.getLogger(ChecklistReport.class.getName());
        
    public static class SlotReportEntry {
        private Slot slot;
        // private Device installedDevice = null;      
        private Long numOfSubSlots = 0L;
        private Long numOfInstalledSlots = 0L;
        private Long numOfAssignedSlots = 0L;
        private Long numOfChecklistApprovedSlots = 0L;
        private Long numOfApprovedSlots = 0L;
        private Long percentApproved = 0L;
        private Long percentInstalled = 0L;
        
        private SlotReportEntry() {
            
        }
        
        public static SlotReportEntry generateInstance(Slot slot) {
            SlotReportEntry sre = new SlotReportEntry();
            
            sre.slot = slot;
            if (slot.getInstallationRecordList() != null &&  !slot.getInstallationRecordList().isEmpty()) {
                // sre.installedDevice = slot.getInstallationRecordList().get(0).getDevice();
            }
            
            return sre;
        }
        
       
        
        // ----
        public Slot getSlot() {
            return slot;
        }

//        public Device getInstalledDevice() {
//            return installedDevice;
//        }

        public Long getNumOfSubSlots() {
            return numOfSubSlots;
        }

        public Long getNumOfInstalledSlots() {
            return numOfInstalledSlots;
        }

        public Long getNumOfAssignedSlots() {
            return numOfAssignedSlots;
        }

        public Long getNumOfApprovedSlots() {
            return numOfApprovedSlots;
        }      

        public Long getNumOfChecklistApprovedSlots() {
            return numOfChecklistApprovedSlots;
        }

        public Long getPercentApproved() {
            return percentApproved;
        }

        public void setPercentApproved(Long percentApproved) {
            this.percentApproved = percentApproved;
        }

        public Long getPercentInstalled() {
            return percentInstalled;
        }

        public void setPercentInstalled(Long percentInstalled) {
            this.percentInstalled = percentInstalled;
        }       
    }
    
    private List<Slot> slots;
    private Slot selectedSlot;
    private List<SlotReportEntry> entries = new ArrayList<>();
    private List<SlotReportEntry> filteredEntries;
    
    public ChecklistReport() {
    }

    /**
     * ToDo: not complete.
     * 
     * @param entry 
     */
    private void refresh(SlotReportEntry entry) {
        String prefix = "_ROOT".equals(entry.slot.getName()) ? "%" : entry.slot.getName() + "%";

        entry.numOfSubSlots = clEJB.numberOfHostingSlots(prefix);
        entry.numOfInstalledSlots = clEJB.numberOfInstalledSlots(prefix);
        entry.numOfAssignedSlots = clEJB.numberOfAssignedSlots(prefix);
        entry.numOfApprovedSlots = clEJB.numberOfApprovedSlots(prefix);
        entry.numOfChecklistApprovedSlots = clEJB.numberOfChecklistApprovedSlots(prefix);
        entry.percentApproved = entry.numOfSubSlots == 0L ? 0L : (100 * entry.numOfApprovedSlots) / entry.numOfSubSlots;
        entry.percentInstalled = entry.numOfSubSlots == 0L ? 0L : (100 * entry.numOfInstalledSlots) / entry.numOfSubSlots;
    }
    
     
    @PostConstruct
    public void init() {
        slots = slotEJB.findByIsHostingSlot(false);
        LOGGER.log(Level.FINE, "Found number of slots: {0}", slots.size());
        SlotReportEntry entry;
        for (Slot slot: slots) {
            entry = SlotReportEntry.generateInstance(slot);
            refresh(entry);
            entries.add(entry);
        }
    }
    
    // --- getters/setters

    public List<Slot> getSlots() {
        return slots;
    } 

    public Slot getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(Slot selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public List<SlotReportEntry> getEntries() {
        return entries;
    }

    public List<SlotReportEntry> getFilteredEntries() {
        return filteredEntries;
    }

    public void setFilteredEntries(List<SlotReportEntry> filteredEntries) {
        this.filteredEntries = filteredEntries;
    }

}
