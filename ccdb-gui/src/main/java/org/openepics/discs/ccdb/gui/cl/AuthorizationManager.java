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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import org.openepics.discs.ccdb.core.auth.LocalAuthEJB;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.auth.AuthOperation;
import org.openepics.discs.ccdb.model.auth.AuthResource;
import org.openepics.discs.ccdb.model.auth.AuthUser;
import org.openepics.discs.ccdb.model.cl.ProcessStatus;


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
@ViewScoped
public class AuthorizationManager implements Serializable {
//    @EJB
//    private AuthEJB authEJB;
    @EJB
    private LocalAuthEJB authEJB;
            
    private static final Logger LOGGER = Logger.getLogger(AuthorizationManager.class.getName());
//    @Inject
//    UserSession userSession;
      
    private AuthUser currentUser;
    
    
    public AuthorizationManager() {
    }
    
    @PostConstruct
    public void init() {       
        currentUser = authEJB.getCurrentUser();
    }
    
    public boolean canManageChecklists() {
        LOGGER.log(Level.INFO, "Checking can mcanage checklists");
        return authEJB.hasPermission(AuthResource.SLOT, null, AuthOperation.MANAGE_CHECKLISTS);
    }
    
    public boolean canAssignChecklists(List<Slot>  slots) {
        for(Slot slot: slots) {
            if (! authEJB.hasPermission(AuthResource.SLOT, slot, AuthOperation.ASSIGN_CHECKLISTS)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean canUpdateStatus(ProcessStatus procStatus) { 
        if (procStatus == null || currentUser == null) return false;
        if (procStatus.getAssignedSME() != null) {
            procStatus.getAssignedSME().equals(currentUser);
        }
        
        return (authEJB.belongsTo(procStatus.getField().getSme()));
    }
    
    public boolean canManageGroups() {
        return authEJB.hasPermission(AuthResource.SLOT, null, AuthOperation.MANAGE_GROUPS);
    }
    
    public boolean canApproveDHR(List<Slot>  slots) {
        for(Slot slot: slots) {
            if (! authEJB.hasPermission(AuthResource.SLOT, slot, AuthOperation.APPROVE_DHR)) {
                return false;
            }
        }
        return true;
    }   
    
    public boolean canApproveARR(List<Slot>  slots) {
        for(Slot slot: slots) {
            if (! authEJB.hasPermission(AuthResource.SLOT, slot, AuthOperation.APPROVE_ARR)) {
                return false;
            }
        }
        return true;
    }  
    //-- Getters/Setters     
}
