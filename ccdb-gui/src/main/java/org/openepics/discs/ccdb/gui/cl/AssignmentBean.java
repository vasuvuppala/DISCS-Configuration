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

import org.openepics.discs.ccdb.model.cl.ChecklistEntity;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.openepics.discs.ccdb.core.ejb.AuthEJB;
import org.openepics.discs.ccdb.core.ejb.DeviceEJB;
import org.openepics.discs.ccdb.core.ejb.ChecklistEJB;
import org.openepics.discs.ccdb.core.ejb.SlotEJB;
import org.openepics.discs.ccdb.core.security.SecurityPolicy;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.Slot;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

/**
 * Description: State for Checklist Assignment View
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
public class AssignmentBean implements Serializable {
//    @EJB
//    private AuthEJB authEJB;

    @EJB
    private ChecklistEJB lcEJB;
    @EJB
    private SlotEJB slotEJB;
    @EJB
    private DeviceEJB deviceEJB;
    @EJB
    private AuthEJB authEJB;
    @Inject
    private SecurityPolicy securityPolicy;

    private static final Logger LOGGER = Logger.getLogger(AssignmentBean.class.getName());

    // request parameters   
    private ChecklistEntity entityType = ChecklistEntity.GROUP;

  
    // view data
    private List<Slot> slots;
    
    // view data
    private List<Slot> selectedSlots;
    private List<Slot> filteredSlots;
    private InputAction inputAction;

    public AssignmentBean() {
    }

    @PostConstruct
    public void init() {
        slots = lcEJB.findUnassignedSlots();
        initialize();
        resetInput();
    }

    /**
     * Initialize data in view
     *
     * @return
     */
    public String initialize() {
        String nextView = null;
        
        switch (entityType) {
            case GROUP:
//                entities = lcEJB.findGroupAssignments();
                break;
            case SLOT:
                slots = slotEJB.findUserSlots();
                break;
            case DEVICE:
//                entities = lcEJB.findDeviceAssignments();
                break;
            default:
                LOGGER.log(Level.WARNING, "Invalid CM entity type {0}", entityType);
                slots = slotEJB.findUserSlots();
        }

        return nextView;
    }

    private void resetInput() {
        inputAction = InputAction.READ;
    }

    public void onRowSelect(SelectEvent event) {
    }

    public boolean isAuthorized() {
        String userId = securityPolicy.getUserId();
        if (userId == null) {
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, UiUtility.MESSAGE_SUMMARY_SUCCESS,
                    "Not authorized. User Id is null.");
            return false;
        }
        // User user = new User(userId);
        return true;
    }

    public void onAddCommand(ActionEvent event) {
        inputAction = InputAction.CREATE;
    }

    public void onDeleteCommand(ActionEvent event) {
        inputAction = InputAction.DELETE;
    }

    private boolean inputIsValid() {
        return true;
    }

    /**
     * Assign checklists to selected entities
     * 
     */
    public void assignCheckist() {
        LOGGER.log(Level.INFO, "Assigning checklists");
        try {
            if (!isAuthorized()) {
                RequestContext.getCurrentInstance().addCallbackParam("success", false);
                return;
            }
            if (!inputIsValid()) {
                RequestContext.getCurrentInstance().addCallbackParam("success", false);
                return;
            }

            switch (entityType) {
                case GROUP:
                    // lcEJB.assignChecklist(entityType, groups);
                    break;
                case SLOT:
                    LOGGER.log(Level.INFO, "Assigning checklists to {0} slots", selectedSlots.size());
                    lcEJB.assignChecklist(entityType, selectedSlots);
                    break;
                case DEVICE:
                    // lcEJB.assignChecklist(entityType, devices);
                    break;
                default:
                    LOGGER.log(Level.WARNING, "Invalid CM entity type {0}", entityType);
                    lcEJB.assignChecklist(entityType, selectedSlots);
            }

            resetInput();
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Success", "Assigned");
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Failure", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);
        }
    }

    /**
     * Unassign checklist from selected entities
     * 
     */
    public void unassignChecklist() {
        try {
            // lcEJB.deleteAssignment(selectedEntity);
            // entities.remove(selectedEntity);
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Success", "Operation was successful. However, you may have to refresh the page.");
            resetInput();
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Failure", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);
        }
    }

    //-- Getters/Setters 
   

    public List<Slot> getSlots() {
        return slots;
    }

    public ChecklistEntity getEntityType() {
        return entityType;
    }

    public void setEntityType(ChecklistEntity entityType) {
        this.entityType = entityType;
    }

    public List<Slot> getSelectedSlots() {
        return selectedSlots;
    }

    public void setSelectedSlots(List<Slot> selectedSlots) {
        this.selectedSlots = selectedSlots;
    }

    public List<Slot> getFilteredSlots() {
        return filteredSlots;
    }

    public void setFilteredSlots(List<Slot> filteredSlots) {
        this.filteredSlots = filteredSlots;
    }

    public InputAction getInputAction() {
        return inputAction;
    }

    public void setInputAction(InputAction inputAction) {
        this.inputAction = inputAction;
    }
    
}
