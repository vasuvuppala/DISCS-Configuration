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
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.openepics.discs.ccdb.core.ejb.ChecklistEJB;
import org.openepics.discs.ccdb.core.ejb.SlotEJB;
import org.openepics.discs.ccdb.core.security.SecurityPolicy;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.cl.SlotGroup;

import org.primefaces.context.RequestContext;

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
public class CmSlotManager implements Serializable {
//    @EJB
//    private AuthEJB authEJB;

    @EJB
    private SlotEJB slotEJB;
    @EJB
    private ChecklistEJB lcEJB;
    @Inject
    private SecurityPolicy securityPolicy;

    private static final Logger LOGGER = Logger.getLogger(CmSlotManager.class.getName());

    // view data
    private List<Slot> entities;
    private List<Slot> selectedEntities;
    private List<Slot> filteredEntities;
    private InputAction inputAction;
    private List<SlotGroup> slotGroups;

    // input data
    private SlotGroup inputGroup;

    // state
    private Boolean noneHasGroup = false; // none of the selected entities has checklists 
    private Boolean allHaveGroup = false; // all of the selected entities have checklists 

    public CmSlotManager() {
    }

    @PostConstruct
    public void init() {
        entities = slotEJB.findNonSystemSlots();
        slotGroups = lcEJB.findAllSlotGroups();
        resetInput();
    }

    private void resetInput() {
        inputAction = InputAction.READ;
        if (selectedEntities != null) selectedEntities.clear();
        noneHasGroup = false;
        allHaveGroup = false;
    }

    /**
     * When a set of rows is selected
     *
     */
    public void onRowSelect() {
        if (selectedEntities == null || selectedEntities.isEmpty()) {
            noneHasGroup = false;
            allHaveGroup = false;
        } else {
            noneHasGroup = selectedEntities.stream().allMatch(s -> s.getCmGroup() == null);
            allHaveGroup = selectedEntities.stream().noneMatch(s -> s.getCmGroup() == null);
        }
        LOGGER.log(Level.INFO, "none has group: {0}", noneHasGroup);
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

//    public void onGroupChange(ValueChangeEvent event) {
//        SlotGroup oldGroup = (SlotGroup) event.getOldValue();
//        SlotGroup newGroup = (SlotGroup) event.getNewValue();
//
//        if (newGroup == null) {
//            UiUtility.showMessage(FacesMessage.SEVERITY_WARN, "Removed from Group", "Make sure that the slots' checklists are updated.");
//        } else if (lcEJB.haveAssignments(selectedEntities)) {
//            UiUtility.showMessage(FacesMessage.SEVERITY_WARN, "Some of the slots already have a checklist", "Their checklists will be masked by the group's checklist.");
//        }
//    }

    /**
     * Assign the input group to the selected slots
     * 
     */
    public void assignGroup() {
        try {
            if (!isAuthorized()) {
                RequestContext.getCurrentInstance().addCallbackParam("success", false);
                return;
            }
            if (!inputIsValid()) {
                RequestContext.getCurrentInstance().addCallbackParam("success", false);
                return;
            }
            LOGGER.log(Level.INFO, "Assigning slots to groups to {0} ", selectedEntities.size());
            lcEJB.assignGroup(selectedEntities, inputGroup);
            lcEJB.refreshVersion(Slot.class, entities); // ToDo: update the entitiy and version in the same transaction. possible?
            selectedEntities.stream().forEach(s -> s.setCmGroup(inputGroup));
            
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Success", "Assigned slots to the chosen group");
            if (lcEJB.haveAssignments(selectedEntities)) {
                UiUtility.showMessage(FacesMessage.SEVERITY_WARN, "Success", "Some of the selcted slots have a checklist assigned. Their checklists will be masked by the group's checklist.");
            }
            resetInput();
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Could not save ", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);
        }
    }

    /**
     * unassign group from the selected slots
     * 
     */
    public void unassignGroup() {
        try {
            LOGGER.log(Level.INFO, "Unassigning {0} slots from group", selectedEntities.size());
            lcEJB.unassignGroup(selectedEntities);
            lcEJB.refreshVersion(Slot.class, entities); // ToDo: update the entitiy and version in the same transaction. possible?
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_WARN, "Success", "However, make sure that the slots' checklists are updated.");
            // UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Deletion successful", "You may have to refresh the page.");
            resetInput();
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Could not complete deletion", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);
        }
    }

    //-- Getters/Setters 
    public InputAction getInputAction() {
        return inputAction;
    }

    public List<Slot> getEntities() {
        return entities;
    }

    public List<Slot> getFilteredEntities() {
        return filteredEntities;
    }

    public void setFilteredEntities(List<Slot> filteredEntities) {
        this.filteredEntities = filteredEntities;
    }

    public List<Slot> getSelectedEntities() {
        return selectedEntities;
    }

    public void setSelectedEntities(List<Slot> selectedEntities) {
        this.selectedEntities = selectedEntities;
    }

    public SlotGroup getInputGroup() {
        return inputGroup;
    }

    public void setInputGroup(SlotGroup inputGroup) {
        this.inputGroup = inputGroup;
    }

    public List<SlotGroup> getSlotGroups() {
        return slotGroups;
    }

    public Boolean getNoneHasGroup() {
        return noneHasGroup;
    }

    public Boolean getAllHaveGroup() {
        return allHaveGroup;
    }
}
