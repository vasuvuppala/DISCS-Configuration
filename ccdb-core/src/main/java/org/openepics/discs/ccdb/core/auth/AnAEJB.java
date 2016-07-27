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
package org.openepics.discs.ccdb.core.auth;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.openepics.discs.ccdb.model.auth.AuthPermission;
import org.openepics.discs.ccdb.model.auth.AuthResource;
import org.openepics.discs.ccdb.model.auth.AuthUserRole;
import org.openepics.discs.ccdb.model.auth.AuthOperation;
import org.openepics.discs.ccdb.model.auth.AuthRole;
import org.openepics.discs.ccdb.model.auth.AuthUser;

/**
 * Manage authentication and authorizations
 * 
 * @author vuppala
 */
@Stateless
public class AnAEJB {

//    @Inject
//    private UserSession userSession;
    private static final Logger LOGGER = Logger.getLogger(AnAEJB.class.getName());
    @PersistenceContext
    private EntityManager em;
    
    /**
     * Find auth roles
     * 
     * @author vuppala 
     * @return all roles
     */
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<AuthRole> findRoles() {
        List<AuthRole> roles;
        TypedQuery<AuthRole> query = em.createNamedQuery("AuthRole.findAll", AuthRole.class);
        roles = query.getResultList();
        LOGGER.log(Level.FINE, "roles found: {0}", roles.size());
        return roles;
    }
    
    /**
     * Find user roles
     * 
     * @author vuppala 
     * @return all user roles
     */
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<AuthUserRole> findAuthUserRoles() {
        List<AuthUserRole> uroles;
        TypedQuery<AuthUserRole> query = em.createNamedQuery("AuthUserRole.findAll", AuthUserRole.class);
        uroles = query.getResultList();
        LOGGER.log(Level.FINE, "user roles found: {0}", uroles.size());
        return uroles;
    }  
    
     /**
     * Find operations
     * 
     * @author vuppala 
     * @return all operations roles
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<AuthOperation> findAuthOperations() {
        return Arrays.asList(AuthOperation.values());
    }
       
    /**
     * Find resources
     * 
     * @author vuppala 
     * @return all resources
     */
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<AuthResource> findAuthResources() {
        return Arrays.asList(AuthResource.values());
    }
    
    /**
     * Find permissions
     * 
     * @author vuppala 
     * @return all resources
     */
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<AuthPermission> findAuthPermissions() {
        List<AuthPermission> perms;
        TypedQuery<AuthPermission> query = em.createNamedQuery("AuthPermission.findAll", AuthPermission.class);
        perms = query.getResultList();
        LOGGER.log(Level.FINE, "AuthPermissions found: {0}", perms.size());
        return perms;
    }
    
    /**
     * Save the given role
     * 
     * @param role 
     */
    public void saveAuthRole(AuthRole role) {
        if (role.getId() == null) {
            em.persist(role);
        } else {
            em.merge(role);
        }
        LOGGER.log(Level.FINE, "AuthEJB#saveAuthRole: role saved - {0}", role.getName());
    }
    
    /**
     * Delete the given role
     * 
     * @param role 
     */
    public void deleteAuthRole(AuthRole role) {
        AuthRole authRole = em.find(AuthRole.class, role.getId());
        em.remove(authRole);
    }
    
    /**
     * Save the given permission
     * 
     * @param perm 
     */
    public void saveAuthPermission(AuthPermission perm) {
        if (perm.getId() == null) {
            em.persist(perm);
        } else {
            em.merge(perm);
        }
        // logger.log(Level.FINE, "AuthEJB#saveAuthResource: role saved - {0}", resource.getName());
    }
    
    /**
     * Delete the given permission
     * 
     * @param perm 
     */
    public void deleteAuthPermission(AuthPermission perm) {
        AuthPermission authPerm = em.find(AuthPermission.class, perm.getId());
        em.remove(authPerm);
    }
    
    /**
     * save the given user-role 
     * 
     * @param urole 
     */
    public void saveAuthUserRole(AuthUserRole urole) {
        if (urole.getId() == null) {
            em.persist(urole);
        } else {
            em.merge(urole);
        }
        // logger.log(Level.FINE, "AuthEJB#saveAuthResource: role saved - {0}", resource.getName());
    }
    
    /**
     * delete the given auth-role
     * 
     * @param urole 
     */
    public void deleteAuthUserRole(AuthUserRole urole) {
        AuthUserRole authUserRole = em.find(AuthUserRole.class, urole.getId());
        em.remove(authUserRole);
    }
    
    /**
     * find a role given its id
     * 
     * @param id
     * @return the role
     */
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public AuthRole findAuthRole(Long id) {
        return em.find(AuthRole.class, id);
    }
    
    /**
     * find a resource given its id
     * 
     * @param id
     * @return the resource 
     */
//    public AuthResource findAuthResource(int id) {
//        return em.find(AuthResource.class, id);
//    }
    
    /**
     * find an operation given its id
     * 
     * @param id
     * @return the operation
     */
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public AuthOperation findAuthOperation(Long id) {
        return em.find(AuthOperation.class, id);
    }
    
    // ------------------- Users -------------------
    /**
     * find all users in the system
     * 
     * @return list of users
     */
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<AuthUser> findUsers() {
        List<AuthUser> users;
        TypedQuery<AuthUser> query = em.createQuery("SELECT u FROM AuthUser u ORDER BY u.lastName, u.firstName", AuthUser.class);
        users = query.getResultList();
        LOGGER.log(Level.FINE, "AuthEJB#findUsers:  found users {0}", users.size());
        return users;
    }

    /**
     * find all users who are current employees
     * 
     * @return list of users
     */
//     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
//    public List<AuthUser> findCurrentUsers() {
//        List<AuthUser> users;
//        TypedQuery<AuthUser> query = em.createQuery("SELECT u FROM AuthUser u WHERE u.currentEmployee = TRUE ORDER BY u.lastName, u.firstName", AuthUser.class);
//        users = query.getResultList();
//        LOGGER.log(Level.FINE, "AuthEJB#findCurrentUsers:  found users {0}", users.size());
//        return users;
//    }
//    
    /** 
     * find a user given its id
     * 
     * @param id
     * @return the user 
     */
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public AuthUser findUser(Long id) {
        return em.find(AuthUser.class, id);
    }

    /**
     * Finds user given its login id.
     * 
     *
     * @param loginId  Id of the desired user
     * @return User for the given login id
     */
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public AuthUser findUser(String loginId) {
        TypedQuery<AuthUser> query = em.createNamedQuery("Sysuser.findByLoginId", AuthUser.class).setParameter("loginId", loginId);
        List<AuthUser> emps = query.getResultList();

        if (emps == null || emps.isEmpty()) {
            LOGGER.log(Level.WARNING, "UserEJB: No user found with id {0}", loginId);
            return null;
        }

        if (emps.size() > 1) {
            LOGGER.log(Level.WARNING, "UserEJB: there are more than 1 employee with the same login id {0}", loginId);
        }
        return emps.get(0);
    }
    
    /**
     * save the given user
     * 
     * @param user 
     */
    public void saveUser(AuthUser user) {
        if (user == null) {
            return;
        }
        
        if (user.getId() == null) {
            em.persist(user);
        } else {
            em.merge(user);
        }
        LOGGER.log(Level.FINE, "HourLogEJB#saveUser: User saved - {0}", user.getUserId());
    }

    /**
     * delete the given user
     * 
     * @param user 
     */
    public void deleteUser(AuthUser user) {
        AuthUser muser = em.find(AuthUser.class, user.getId());
        em.remove(muser);
    }
     
}
