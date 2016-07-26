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
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.openepics.discs.ccdb.core.auth.AnAEJB;
import org.openepics.discs.ccdb.model.auth.AuthOperation;
import org.openepics.discs.ccdb.model.auth.AuthPermission;
import org.openepics.discs.ccdb.model.auth.AuthResource;
import org.openepics.discs.ccdb.model.auth.AuthRole;
import org.openepics.discs.ccdb.model.auth.AuthUserRole;


/**
 *
 * Stores the state for Authorizations view (Administrative pages)
 *
 * @author vuppala
 *
 */
// ToDo: make it request scoped?
@Named
@ViewScoped
public class AdminAuthView implements Serializable {

    @EJB
    private AnAEJB authEJB;
    private static final Logger logger = Logger.getLogger(AdminAuthView.class.getName());
    @Inject
    UserSession userSession;

    private List<AuthRole> roles;
    private List<AuthUserRole> userRoles;
    private List<AuthOperation> operations;

    private List<AuthResource> resources;
    private List<AuthPermission> permissions;

    private AuthRole selectedRole;

    public AdminAuthView() {
    }

    /**
     * Initialize the view state.
     *
     */
    @PostConstruct
    public void init() {

        roles = authEJB.findRoles();
        userRoles = authEJB.findAuthUserRoles();
        operations = authEJB.findAuthOperations();
        resources = authEJB.findAuthResources();

        permissions = authEJB.findAuthPermissions();
    }

    // --- Getters and setters 
    public List<AuthRole> getRoles() {
        return roles;
    }

    public List<AuthUserRole> getAuthUserRoles() {
        return userRoles;
    }

    public List<AuthUserRole> getUserRoles() {
        return userRoles;
    }

    public List<AuthResource> getResources() {
        return resources;
    }

    public List<AuthPermission> getPermissions() {
        return permissions;
    }

    public List<AuthOperation> getOperations() {
        return operations;
    }

    public AuthRole getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(AuthRole selectedRole) {
        this.selectedRole = selectedRole;
    }

}
