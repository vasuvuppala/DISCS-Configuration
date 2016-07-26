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
package org.openepics.discs.ccdb.gui.auth;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.openepics.discs.ccdb.core.auth.AnAEJB;
import org.openepics.discs.ccdb.gui.ui.util.InputAction;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.auth.AuthOperation;
import org.openepics.discs.ccdb.model.auth.AuthPermission;
import org.openepics.discs.ccdb.model.auth.AuthResource;
import org.openepics.discs.ccdb.model.auth.AuthRole;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

/**
 * Description: State for Manage auth Permissions View
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
public class ManagePermissionView implements Serializable {

    @EJB
    private AnAEJB authEJB;

    @Inject
    UserSession userSession;
    private static final Logger logger = Logger.getLogger(ManagePermissionView.class.getName());

    private List<AuthPermission> permissions;
    private AuthPermission selectedPermission;
    private AuthPermission inputPermission;
    private InputAction inputAction;

    private List<AuthRole> roles;
    private List<AuthOperation> operations;
    private List<AuthResource> resources;

    public ManagePermissionView() {
    }

    @PostConstruct
    public void init() {
        roles = authEJB.findRoles();
        operations = authEJB.findAuthOperations();
        resources = authEJB.findAuthResources();
        permissions = authEJB.findAuthPermissions();
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
        inputPermission = new AuthPermission();
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
                authEJB.saveAuthPermission(inputPermission);
                permissions.add(inputPermission);
            } else {
                authEJB.saveAuthPermission(selectedPermission);
            }
            resetInput();
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Role saved", "");
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Could not save role", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);
        }
    }

    public void deleteEntity() {
        try {
            authEJB.deleteAuthPermission(selectedPermission);
            permissions.remove(selectedPermission);
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Role deleted", "");
            resetInput();
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Could not delete role", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);
        }
    }

    // ----- Getters/Setters
    public List<AuthPermission> getPermissions() {
        return permissions;
    }

    public AuthPermission getSelectedPermission() {
        return selectedPermission;
    }

    public void setSelectedPermission(AuthPermission selectedPermission) {
        this.selectedPermission = selectedPermission;
    }

    public AuthPermission getInputPermission() {
        return inputPermission;
    }

    public void setInputPermission(AuthPermission inputPermission) {
        this.inputPermission = inputPermission;
    }

    public InputAction getInputAction() {
        return inputAction;
    }

    public List<AuthRole> getRoles() {
        return roles;
    }

    public List<AuthOperation> getOperations() {
        return operations;
    }

    public List<AuthResource> getResources() {
        return resources;
    }

}
