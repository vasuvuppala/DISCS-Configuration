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
import org.openepics.discs.ccdb.core.ejb.ChecklistEJB;
import org.openepics.discs.ccdb.core.ejb.SlotEJB;
import org.openepics.discs.ccdb.core.security.SecurityPolicy;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.SlotPropertyValue;
import org.openepics.discs.ccdb.model.cl.Approval;
import org.openepics.discs.ccdb.model.cl.Checklist;
import org.openepics.discs.ccdb.model.cl.Process;
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
public class SlotApprovalBean implements Serializable {
//    @EJB
//    private AuthEJB authEJB;

    @EJB
    private ChecklistEJB lcEJB;
    @EJB
    private SlotEJB slotEJB;
    @Inject
    private SecurityPolicy securityPolicy;

    private static final Logger LOGGER = Logger.getLogger(SlotApprovalBean.class.getName());
    private final ChecklistEntity ENTITY_TYPE = ChecklistEntity.SLOT;

    // request data
    private String processName;
    
    // view data
    private List<Slot> entities;
    private List<Slot> selectedEntities;
    private List<Slot> filteredEntities;
    private InputAction inputAction;
    
    private Process selectedProcess;

    public SlotApprovalBean() {
    }

    @PostConstruct
    public void init() {
        entities = lcEJB.findUngroupedSlots();  
        resetInput();
    }

    private void resetInput() {
        inputAction = InputAction.READ;
        if (selectedEntities != null) selectedEntities.clear();
    }

    /**
     * Initialize data in view
     * 
     * @return 
     */
    public String initialize() {
        String nextView = null;
        
        selectedProcess = lcEJB.findPhaseByName(processName);
        if (selectedProcess == null) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "No process selected for approval", "Check workflow");
            LOGGER.log(Level.SEVERE, "No process selected for approval");
        }
        
        return nextView;
    
    }
    
    /**
     * When a set of rows is selected
     * 
     */
    public void onRowSelect() {
        
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
     
    /**
     * Assign checklists to selected entities
     *
     */
    public void approve() {
        LOGGER.log(Level.INFO, "Approve");
        try {
            if (!isAuthorized()) {
                RequestContext.getCurrentInstance().addCallbackParam("success", false);
                return;
            }
            if (!inputIsValid()) {
                RequestContext.getCurrentInstance().addCallbackParam("success", false);
                return;
            }
            LOGGER.log(Level.INFO, "Approving  {0} slots", selectedEntities.size());
            lcEJB.setApprovals(selectedProcess, selectedEntities, inputAction == InputAction.CREATE);
            resetInput();
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Success", "Approvals modified");
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Error in setting approvals", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            // System.out.println(e);
        }
    }

    /**
     * approval status of a slot
     * 
     * @param slot
     * @return 
     */
    public String approvalStatus(Slot slot) {
        Approval approval = lcEJB.findApproval(selectedProcess, slot);
        if (approval == null) return "Not Approved";
        return approval.getApproved() ? "Approved" : "Not Approved";  
    }

    /**
     * property of a slot
     * 
     * @param slot
     * @param property
     * @return 
     */
    public String slotProperty(Slot slot, String property) {
        SlotPropertyValue value = slotEJB.getPropertyValue(slot, property);
        return value == null? "": value.getPropValue().toString();
    }
    
    public List<Slot> getSelectedEntities() {
        return selectedEntities;
    }

    public void setSelectedEntities(List<Slot> selectedEntities) {
        this.selectedEntities = selectedEntities;
    }

    public List<Slot> getFilteredEntities() {
        return filteredEntities;
    }

    public void setFilteredEntities(List<Slot> filteredEntities) {
        this.filteredEntities = filteredEntities;
    }

    public List<Slot> getEntities() {
        return entities;
    }

    public InputAction getInputAction() {
        return inputAction;
    }

    public void setInputAction(InputAction inputAction) {
        this.inputAction = inputAction;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    
}
