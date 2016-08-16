/*
 * This software is Copyright by the Board of Trustees of Michigan
 *  State University (c) Copyright 2013, 2014.
 *  
 *  You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *    http://www.gnu.org/licenses/gpl.txt
 *  
 *  Contact Information:
 *       Facility for Rare Isotope Beam
 *       Michigan State University
 *       East Lansing, MI 48824-1321
 *        http://frib.msu.edu
 */

package org.openepics.discs.ccdb.gui.cl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.openepics.discs.ccdb.core.auth.LocalAuthEJB;
import org.openepics.discs.ccdb.model.Device;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.auth.AuthOperation;
import org.openepics.discs.ccdb.model.auth.AuthResource;
import org.openepics.discs.ccdb.model.auth.AuthUser;
import org.openepics.discs.ccdb.model.cl.Assignment;
import org.openepics.discs.ccdb.model.cl.ProcessStatus;
import org.openepics.discs.ccdb.model.cl.SlotGroup;


/**
 * Description: State for Manage Process View
 *
 * Methods:
 * <p>
 * Init: to initialize the state
 * <p>
 * resetInput: reset all inputs on the view
 * <p>
 * onRowSelect: things to do when an item is selected
 * <p>
 * onAddCommand: things to do before adding an item
 * <p>
 * onEditCommand: things to do before editing an item
 * <p>
 * onDeleteCommand: things to do before deleting an item
 * <p>
 * saveXXXX: save the input or edited item
 * <p>
 * deleteXXXX: delete the selected item
 *
 * @author vuppala
 *
 */

@Named
@RequestScoped
public class AuthorizationManager implements Serializable {
//    @EJB
//    private AuthEJB authEJB;
    @EJB
    private LocalAuthEJB authEJB;
            
    private static final Logger LOGGER = Logger.getLogger(AuthorizationManager.class.getName());
//    @Inject
//    UserSession userSession;
      
    // private AuthUser currentUser;
    
    
    public AuthorizationManager() {
    }
    
    @PostConstruct
    public void init() {       
        // currentUser = authEJB.getCurrentUser();
    }
    
    public boolean isLoggedIn() {
        return authEJB.isLoggedIn();
    }
    
    public boolean canManageChecklists() {
        LOGGER.log(Level.INFO, "Checking can mcanage checklists");
        return authEJB.hasPermission(AuthResource.CHECKLIST, null, AuthOperation.MANAGE);
    }
    

    
    // ToDo: Make it generic for slots, devices, and groups
    public boolean canAssignSlotChecklists(List<Slot>  slots) {
        for(Slot slot: slots) {
            if (! authEJB.hasPermission(AuthResource.SLOT, slot, AuthOperation.ASSIGN_CHECKLISTS)) {
                return false;
            }
        }
        return true;
    }
    
    // ToDo: Make it generic for slots, devices, and groups
    public boolean canAssignDevChecklists(List<Device>  devices) {
        for(Device device: devices) {
            if (! authEJB.hasPermission(AuthResource.DEVICE, device, AuthOperation.ASSIGN_CHECKLISTS)) {
                return false;
            }
        }
        return true;
    }
    
    // ToDo: Make it generic for slots, devices, and groups
    public boolean canAssignGroupChecklists(List<SlotGroup>  groups) {
        AuthUser currentUser = authEJB.getCurrentUser();
        for(SlotGroup group: groups) {
            if (! group.getOwner().equals(currentUser)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean canAssignProcesses(List<Assignment>  assignments) {
        AuthUser currentUser = authEJB.getCurrentUser();
        for(Assignment assignment: assignments) {
            if (assignment.getSlot() != null && ! authEJB.hasPermission(AuthResource.SLOT, assignment.getSlot(), AuthOperation.ASSIGN_CHECKLISTS)) {
                return false;
            }
            if (assignment.getSlotGroup() != null && assignment.getSlotGroup().getOwner() != null 
                    && ! assignment.getSlotGroup().getOwner().equals(currentUser)) {
                return false;
            }
            if (assignment.getDevice() != null && ! authEJB.hasPermission(AuthResource.DEVICE, assignment.getDevice(), AuthOperation.ASSIGN_CHECKLISTS)) {
                return false;
            }
        }
        return true;
    }
     
    public boolean canAssignSME(Assignment assignment) {
        List<Assignment> assignments  = new ArrayList<>();
        assignments.add(assignment);
        return canAssignProcesses(assignments);
    }
    
    public boolean canAssignProcess(Assignment assignment) {
        List<Assignment> assignments  = new ArrayList<>();
        assignments.add(assignment);
        return canAssignProcesses(assignments);
    }
    
    public boolean canUpdateStatus(ProcessStatus procStatus) { 
        AuthUser currentUser = authEJB.getCurrentUser();
        if (procStatus == null || currentUser == null) return false;
        if (procStatus.getAssignedSME() != null && procStatus.getAssignedSME().equals(currentUser)) {
            return true;
        }
        
        if (authEJB.belongsTo(procStatus.getField().getSme())) return true;
        return canAssignProcess(procStatus.getAssignment()); // if the user can assign processes then she can update the status i.e she is the owner
    }
    
    public boolean canManageGroups() {
        return authEJB.hasPermission(AuthResource.SLOT_GROUP, null, AuthOperation.MANAGE);
    }
    
    private boolean canApproveDRR(List<Slot>  slots) {
        for(Slot slot: slots) {
            if (! authEJB.hasPermission(AuthResource.SLOT, slot, AuthOperation.APPROVE_DRR)) {
                return false;
            }
        }
        return true;
    }   
    
    private boolean canApproveARR(List<Slot>  slots) {
        for(Slot slot: slots) {
            if (! authEJB.hasPermission(AuthResource.SLOT, slot, AuthOperation.APPROVE_ARR)) {
                return false;
            }
        }
        return true;
    }  
    
    public boolean canAuthorize() {
        if (authEJB.needAuthBoostrap()) return true; // if there are no auth records (users, roles, permissions etc), allow to bootstrap
        return authEJB.hasPermission(AuthResource.AUTHORIZATIONS, null, AuthOperation.MANAGE);
    }
    
    public boolean canApprove(String processName, List<Slot> slots) {
        switch (processName) {
            case "DRR":
                return canApproveDRR(slots);
            case "ARR":
                return canApproveARR(slots);
            default:
                LOGGER.log(Level.SEVERE, "Invalid process name {0}", processName);
                break;
        }
        
        return false;
    }
    //-- Getters/Setters     
}
