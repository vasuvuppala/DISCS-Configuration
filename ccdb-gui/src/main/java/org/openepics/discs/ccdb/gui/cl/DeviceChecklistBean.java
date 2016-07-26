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

import org.openepics.discs.ccdb.gui.ui.util.InputAction;
import org.openepics.discs.ccdb.model.cl.ChecklistEntity;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.openepics.discs.ccdb.core.ejb.DeviceEJB;
import org.openepics.discs.ccdb.core.ejb.ChecklistEJB;
import org.openepics.discs.ccdb.core.security.SecurityPolicy;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.Device;
import org.openepics.discs.ccdb.model.cl.Checklist;

import org.primefaces.context.RequestContext;

/**
 * Description: State for 'Assign Checklists to Slot' View
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
public class DeviceChecklistBean implements Serializable {
//    @EJB
//    private AuthEJB authEJB;

    @EJB
    private ChecklistEJB lcEJB;
    @EJB
    private DeviceEJB deviceEJB;
    @Inject
    private SecurityPolicy securityPolicy;

    private static final Logger LOGGER = Logger.getLogger(DeviceChecklistBean.class.getName());
    private final ChecklistEntity ENTITY_TYPE = ChecklistEntity.DEVICE;

  
    // view data
    private List<Device> entities;
    private List<Device> selectedEntities;
    private List<Device> filteredEntities;
    private InputAction inputAction;
    
    // state
    private Boolean noneHasChecklists = false; // none of the selected entities has checklists 
    private Boolean allHaveChecklists = false; // all of the selected entities have checklists 
    
    private Checklist defaultChecklist;

    public DeviceChecklistBean() {
    }

    @PostConstruct
    public void init() {
        entities = deviceEJB.findAll();
        defaultChecklist = lcEJB.findDefaultChecklist(ENTITY_TYPE);
        resetInput();
    }

    private void resetInput() {
        inputAction = InputAction.READ;
        if (selectedEntities != null) selectedEntities.clear();
        noneHasChecklists = false; 
        allHaveChecklists = false; 
    }

    
    /**
     * When a set of rows is selected
     * 
     */
    public void onRowSelect() {
        if (selectedEntities == null || selectedEntities.isEmpty()) {
            noneHasChecklists = false;
            allHaveChecklists = false;
        } else {
            noneHasChecklists = lcEJB.noneHasChecklists(ENTITY_TYPE, selectedEntities);
            allHaveChecklists = ! noneHasChecklists; //ToDo: this is not right but ok for now
        } 
        
        if (defaultChecklist == null) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "There is no default checklist to assign.", "Default checklist must be defined first"); 
        }
        // LOGGER.log(Level.INFO, "has checklists: {0}", noneHasChecklists);
    }

    public void onAddCommand(ActionEvent event) {
        inputAction = InputAction.CREATE;
    }

    public void onDeleteCommand(ActionEvent event) {
        inputAction = InputAction.DELETE;
    }
    
    /**
     * Validates input
     * 
     * @return 
     */
    private boolean inputIsValid() {
        if (defaultChecklist == null) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "There is no default checklist to assign.", "Default checklist must be defined first"); 
            return false;
        }
        return true;
    }

    /**
     * Check authorization
     * 
     * @return 
     */
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
            LOGGER.log(Level.INFO, "Assigning checklists to {0} entities", selectedEntities.size());
            lcEJB.assignChecklist(ENTITY_TYPE, selectedEntities);
            resetInput();
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Success", "Checklist Assigned");
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Error in assigning checklists", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            // System.out.println(e);
        }
    }

    /**
     * Unassign checklist from selected entities
     * 
     */
    public void unassignChecklist() {
        try {
            LOGGER.log(Level.INFO, "Unassigning checklists from {0} slots", selectedEntities.size());
            lcEJB.unassignChecklist(ENTITY_TYPE, selectedEntities);
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

    /**
     * Find assignments of a slot
     * 
     * @param device
     * @return 
     */
    public String assignedChecklists(Device device) {
        return lcEJB.findAssignments(device).stream().map(a -> a.getChecklist().getName()).collect(Collectors.joining());
    }
    //-- Getters/Setters 

    public List<Device> getEntities() {
        return entities;
    }

    public List<Device> getSelectedEntities() {
        return selectedEntities;
    }

    public void setSelectedEntities(List<Device> selectedEntities) {
        this.selectedEntities = selectedEntities;
    }

    public List<Device> getFilteredEntities() {
        return filteredEntities;
    }

    public void setFilteredEntities(List<Device> filteredEntities) {
        this.filteredEntities = filteredEntities;
    }

    public InputAction getInputAction() {
        return inputAction;
    }

    public void setInputAction(InputAction inputAction) {
        this.inputAction = inputAction;
    }

    public Checklist getDefaultChecklist() {
        return defaultChecklist;
    }
     
    public Boolean getNoneHasChecklists() {
        return noneHasChecklists;
    }

    public Boolean getAllHaveChecklists() {
        return allHaveChecklists;
    }
 
}
