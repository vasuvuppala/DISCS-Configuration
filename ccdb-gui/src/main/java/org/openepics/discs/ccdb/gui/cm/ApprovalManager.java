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
package org.openepics.discs.ccdb.gui.cm;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.openepics.discs.ccdb.core.ejb.LifecycleEJB;
import org.openepics.discs.ccdb.core.security.SecurityPolicy;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.auth.User;
import org.openepics.discs.ccdb.model.cm.PhaseApproval;
import org.openepics.discs.ccdb.model.cm.PhaseGroup;
import org.openepics.discs.ccdb.model.cm.StatusOption;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

/**
 * Description: State for Manage Phase View
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
public class ApprovalManager implements Serializable {
//    @EJB
//    private AuthEJB authEJB;

    @EJB
    private LifecycleEJB lcEJB;
    @Inject
    private SecurityPolicy securityPolicy;

    private static final Logger LOGGER = Logger.getLogger(ApprovalManager.class.getName());
//    @Inject
//    UserSession userSession;

    private List<PhaseApproval> entities;
    private List<PhaseApproval> filteredEntities;
    private List<StatusOption> statusOptions;
    private PhaseApproval inputEntity;
    private PhaseApproval selectedEntity;
    private InputAction inputAction;
    private StatusOption inputStatus;
    private String selectedType;
    private String inputComment;

    private List<PhaseApproval> selectedApprovals;

    public ApprovalManager() {
    }

    @PostConstruct
    public void init() {
        resetInput();
    }

    /**
     * Initialize data in view
     *
     * @return
     */
    public String initialize() {
        String nextView = null;
        PhaseGroup stype = null;

        if (selectedType != null) {
            stype = lcEJB.findPhaseGroup(selectedType);
        }

        if (stype == null) {
            entities = lcEJB.findAllApprovals();
        } else {
            entities = lcEJB.findApprovals(stype);
            statusOptions = lcEJB.findStatusOptions(stype);
        }
        return nextView;
    }

    private void resetInput() {
        inputAction = InputAction.READ;
        inputComment = null;
        if (statusOptions != null) {
            inputStatus = statusOptions.get(0);
        }
    }

    public void onRowSelect(SelectEvent event) {
        PhaseApproval approval = (PhaseApproval) event.getObject();
        inputComment = approval.getComment();
        inputStatus = approval.getStatus();
        // inputRole = selectedRole;
        // Utility.showMessage(FacesMessage.SEVERITY_INFO, "Role Selected", "");
    }

    public void onAddCommand(ActionEvent event) {
        inputEntity = new PhaseApproval();
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
                lcEJB.saveApproval(inputEntity);
                entities.add(inputEntity);
            } else {
                lcEJB.saveApproval(selectedEntity);
            }
            resetInput();
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Saved", "");
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Could not save", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);
        }
    }

    public void deleteEntity() {
        try {
            lcEJB.deleteApproval(selectedEntity);
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

    public void onApprove() {
        try {
            Preconditions.checkNotNull(selectedApprovals);
            String userId = securityPolicy.getUserId();
            if (userId == null) {
                UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Update Failed",
                        "You are not authorized. User Id is null.");
                RequestContext.getCurrentInstance().addCallbackParam("success", false);
                return;
            }
            User user = new User(userId);

            for (PhaseApproval selectedApproval : selectedApprovals) {
                if (selectedApproval.getAssignedApprover() != null && !selectedApproval.getAssignedApprover().equals(user)) {
                    UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Update Failed",
                            "You are not assigned to one or more of the selected assignments.");
                    RequestContext.getCurrentInstance().addCallbackParam("success", false);
                    return;
                }
            }

            for (PhaseApproval selectedApproval : selectedApprovals) {
                selectedApproval.setApproved_at(new Date());
                selectedApproval.setApproved_by(user);
                selectedApproval.setApproved(true);
                selectedApproval.setStatus(inputStatus);
                selectedApproval.setComment(inputComment);
                lcEJB.saveApproval(selectedApproval);
            }
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, UiUtility.MESSAGE_SUMMARY_SUCCESS,
                    "Update successful.");
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Could not complete approval", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);

        } finally {
//            selectedApproval = null;           
        }
    }

    public void onDisapprove() {
        try {
            Preconditions.checkNotNull(selectedApprovals);
            String userId = securityPolicy.getUserId();
            if (userId == null) {
                UiUtility.showMessage(FacesMessage.SEVERITY_INFO, UiUtility.MESSAGE_SUMMARY_SUCCESS,
                        "Not authorized. User Id is null.");
                RequestContext.getCurrentInstance().addCallbackParam("success", false);
                return;
            }
            User user = new User(userId);
            for (PhaseApproval selectedApproval : selectedApprovals) {
                selectedApproval.setApproved_at(new Date());
                selectedApproval.setApproved_by(user);
                selectedApproval.setApproved(false);
                lcEJB.saveApproval(selectedApproval);
            }
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, UiUtility.MESSAGE_SUMMARY_SUCCESS,
                    "Disappproval successful.");
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Could not complete disapproval", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);
        } finally {
//            selectedApproval = null;           
        }
    }
    //-- Getters/Setters 

    public InputAction getInputAction() {
        return inputAction;
    }

    public List<PhaseApproval> getEntities() {
        return entities;
    }

    public List<PhaseApproval> getFilteredEntities() {
        return filteredEntities;
    }

    public void setFilteredEntities(List<PhaseApproval> filteredEntities) {
        this.filteredEntities = filteredEntities;
    }

    public PhaseApproval getInputEntity() {
        return inputEntity;
    }

    public void setInputEntity(PhaseApproval inputEntity) {
        this.inputEntity = inputEntity;
    }

    public PhaseApproval getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(PhaseApproval selectedEntity) {
        this.selectedEntity = selectedEntity;
    }

    public List<PhaseApproval> getSelectedApprovals() {
        return selectedApprovals;
    }

    public void setSelectedApprovals(List<PhaseApproval> selectedApprovals) {
        this.selectedApprovals = selectedApprovals;
    }

    public StatusOption getInputStatus() {
        return inputStatus;
    }

    public void setInputStatus(StatusOption inputStatus) {
        this.inputStatus = inputStatus;
    }

    public String getInputComment() {
        return inputComment;
    }

    public void setInputComment(String inputComment) {
        this.inputComment = inputComment;
    }

    public List<StatusOption> getStatusOptions() {
        return statusOptions;
    }

    public String getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
    }

}