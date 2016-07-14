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
import org.openepics.discs.ccdb.core.ejb.ChecklistEJB;
import org.openepics.discs.ccdb.core.ejb.AuthEJB;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.auth.Role;
import org.openepics.discs.ccdb.model.cl.Process;
import org.openepics.discs.ccdb.model.cl.Checklist;
import org.openepics.discs.ccdb.model.cl.ChecklistField;
import org.openepics.discs.ccdb.model.cl.StatusOption;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

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
public class PhaseMemberManager implements Serializable {
//    @EJB
//    private AuthEJB authEJB;
    @EJB
    private ChecklistEJB lcEJB;
    @EJB
    private AuthEJB authEJB;
            
    private static final Logger LOGGER = Logger.getLogger(PhaseMemberManager.class.getName());
//    @Inject
//    UserSession userSession;
      
    private List<ChecklistField> entities;    
    private List<ChecklistField> filteredEntities;    
    private ChecklistField inputEntity;
    private ChecklistField selectedEntity;
    private InputAction inputAction;
    private List<Checklist> phaseGroups;
    private List<Process> phases;
    private List<Role> roles ;
    private List<StatusOption> statusOptions;
    
    public PhaseMemberManager() {
    }
    
    @PostConstruct
    public void init() {      
        entities = lcEJB.findAllPhaseGroupMembers();    
        phaseGroups = lcEJB.findAllPhaseGroups();
        phases = lcEJB.findAllPhases();
        roles = authEJB.findAllRoles();
        if (phaseGroups == null || phaseGroups.isEmpty()) {
            LOGGER.log(Level.SEVERE, "There are no phase groups!");
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "No Phase Groups", "You must first add one ore more phase groups");
        } else {
            statusOptions = lcEJB.findStatusOptions(phaseGroups.get(0));
        }
        resetInput();
    }
    
    private void resetInput() {                
        inputAction = InputAction.READ;
    }
    
    public void onRowSelect(SelectEvent event) {
        // inputRole = selectedRole;
        // Utility.showMessage(FacesMessage.SEVERITY_INFO, "Role Selected", "");
    }
    
    public void onAddCommand(ActionEvent event) {
        inputEntity = new ChecklistField();
        inputAction = InputAction.CREATE;       
    }
    
    public void onEditCommand(ActionEvent event) {
        inputAction = InputAction.UPDATE;
    }
    
    public void onDeleteCommand(ActionEvent event) {
        inputAction = InputAction.DELETE;
    }
    
    public void saveEntity() {
        try {                      
            if (inputAction == InputAction.CREATE) {
                lcEJB.savePhaseGroupMember(inputEntity);
                entities.add(inputEntity);                
            } else {
                lcEJB.savePhaseGroupMember(selectedEntity);
            }
            resetInput();
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Saved", "");
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Could not save ", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);
        }
    }
    
    public void deleteEntity() {
        try {
            lcEJB.deletePhaseGroupMember(selectedEntity);
            entities.remove(selectedEntity);  
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Deletion successful", "You may have to refresh the page.");
            resetInput();
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Could not complete deletion", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);
        }
    }
    
    /**
     * when a new group is selected
     * 
     */
    public void onGroupChange() {
        Checklist phaseGroup = inputAction == InputAction.CREATE? inputEntity.getPhaseGroup() : selectedEntity.getPhaseGroup();
        if( phaseGroup != null) {
            statusOptions = lcEJB.findStatusOptions(phaseGroup);
            if (statusOptions == null || statusOptions.isEmpty()) {
                UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "No status options defined for this group", "You must first define status options for this group");
            }
        } else {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "No Phase Group selected", "You must first select a phase group");
            statusOptions = lcEJB.findAllStatusOptions();
        }
    }
    
    //-- Getters/Setters 
    
    public InputAction getInputAction() {
        return inputAction;
    }

    public List<ChecklistField> getEntities() {
        return entities;
    }

    public List<ChecklistField> getFilteredEntities() {
        return filteredEntities;
    }

    public void setFilteredEntities(List<ChecklistField> filteredEntities) {
        this.filteredEntities = filteredEntities;
    }

    public ChecklistField getInputEntity() {
        return inputEntity;
    }

    public void setInputEntity(ChecklistField inputEntity) {
        this.inputEntity = inputEntity;
    }

    public ChecklistField getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(ChecklistField selectedEntity) {
        this.selectedEntity = selectedEntity;
    }    

    public List<Checklist> getPhaseGroups() {
        return phaseGroups;
    }

    public List<Process> getPhases() {
        return phases;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public List<StatusOption> getStatusOptions() {
        return statusOptions;
    }
    
}
