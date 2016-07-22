/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 *
 * Controls Configuration Database is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.conf.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import org.openepics.discs.ccdb.core.ejb.ChecklistEJB;
import org.openepics.discs.ccdb.core.ejb.SlotEJB;
import org.openepics.discs.ccdb.jaxb.SlotReportEntry;
import org.openepics.discs.ccdb.jaxrs.ReportResource;

import org.openepics.discs.ccdb.model.Slot;

/**
 * An implementation of the InstallationSlotResource interface.
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 */
public class ReportResourceImpl implements ReportResource {
    
    @EJB private SlotEJB slotEJB;
    @EJB private ChecklistEJB clEJB;
    private static final Logger LOGGER = Logger.getLogger(ReportResourceImpl.class.getName());
   
   
    /**
     * ToDo: Temporary. Change. Remove 'FRIB' etc
     * 
     * @param entry 
     */
    private void refresh(SlotReportEntry entry) {
        String prefix = entry.getSlot() + "%";
        if ("_ROOT".equals(entry.getSlot())) {
            prefix = "%";
            entry.setSlot("FRIB"); 
        }

        entry.setSubSlots(clEJB.numberOfHostingSlots(prefix));
//        entry.setSubSlots(0L);
        entry.setInstalledSlots(0L); //ToDo: calculate number of installed slots
        //entry.setApprovedSlots(clEJB.numberOfApprovedSlots(prefix));
        entry.setApprovedSlots(0L);
//        entry.percentComplete = entry.numOfAssignedSlots == 0L ? 0L : (100 * entry.numOfApprovedSlots) / entry.numOfAssignedSlots;
    }
     
    @Override
    public List<SlotReportEntry> getSlotStatus(String prefix) {
         List<Slot> slots;
         
        slots = slotEJB.findByIsHostingSlot(false);         
        LOGGER.log(Level.FINE, "Found number of slots: {0}", slots.size());
        
        SlotReportEntry entry;
        List<SlotReportEntry> entries = new ArrayList<>();
        for (Slot slot: slots) {
            if (! slot.getName().startsWith(prefix)) {
                continue;
            }           
            entry = new SlotReportEntry(slot.getName());
            refresh(entry);
            entries.add(entry);
        }
        
        return entries;
    }  
}
