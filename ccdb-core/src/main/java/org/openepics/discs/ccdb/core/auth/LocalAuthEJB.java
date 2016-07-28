/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 *
 * Controls Configuration Database is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.ccdb.core.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import org.openepics.discs.ccdb.model.Device;
import org.openepics.discs.ccdb.model.Property;
import org.openepics.discs.ccdb.model.PropertyValue;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.auth.AuthOperation;
import org.openepics.discs.ccdb.model.auth.AuthPermission;
import org.openepics.discs.ccdb.model.auth.AuthResource;
import org.openepics.discs.ccdb.model.auth.AuthRole;
import org.openepics.discs.ccdb.model.auth.AuthUser;
import org.openepics.discs.ccdb.model.auth.AuthUserRole;

/**
 * 
 * ToDo: Temporary authorization. Integrate with Security Policy
 *
 * @author <a href="mailto:vuppala@frib.msu.edu">Vasu Vuppala</a>
 */
@Stateless
public class LocalAuthEJB implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(LocalAuthEJB.class.getCanonicalName());

    @PersistenceContext private transient EntityManager em;

    @Inject private transient HttpServletRequest servletRequest;

    public LocalAuthEJB() {}
    
    /**
     * the current user
     * 
     * @return 
     */
    public AuthUser getCurrentUser() {
        String userId = servletRequest.getUserPrincipal() != null ? servletRequest.getUserPrincipal().getName() : null;
        if (userId == null) {
            return null;
        }
        List<AuthUser> users = em.createNamedQuery("AuthUser.findByUserId", AuthUser.class)
                .setParameter("userid", userId)
                .getResultList();
        
        if (users == null || users.isEmpty()) {
            return null;
        }
        
        return users.get(0);
    }

    /**
     * Find the role specified in the property 
     * 
     * @param slot
     * @param property
     * @return 
     */
//    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
//    private AuthRole indirectRole(Slot slot, Property property) {
//        List<SlotPropertyValue> props = em.createQuery("SELECT pv FROM Slot s JOIN s.slotPropertyList pv WHERE s = :slot AND pv.property = :property")
//                .setParameter("slot", slot)
//                .setParameter("property", property)
//                .getResultList();
//        
//        if (props == null || props.isEmpty()) {           
//           return null;
//        }
//        
//        String roleName = props.get(0).getPropValue().toString();
//        
//        List<AuthRole> roles = em.createNamedQuery("AuthRole.findByName",AuthRole.class)
//                .setParameter("name", roleName)
//                .getResultList();
//        
//        if (roles == null || roles.isEmpty()) {
//            return null;
//        }
//        
//        return roles.get(0);
//    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    private <T> AuthRole indirectRole(T entity, Property property) {
        List<PropertyValue> props = null;
        if (entity instanceof Slot) {
            props = em.createQuery("SELECT pv FROM Slot s JOIN s.slotPropertyList pv WHERE s = :slot AND pv.property = :property")
                    .setParameter("slot", entity)
                    .setParameter("property", property)
                    .getResultList();
        } else if (entity instanceof Device) {
            props = em.createQuery("SELECT pv FROM Device s JOIN s.devicePropertyList pv WHERE s = :device AND pv.property = :property")
                    .setParameter("device", entity)
                    .setParameter("property", property)
                    .getResultList();
        } else {
            LOGGER.log(Level.SEVERE, "Invalid entity type");
            return null;
        }
        if (props == null || props.isEmpty()) {            
            return null;
        }
        
        String roleName = props.get(0).getPropValue().toString();
        
        List<AuthRole> roles = em.createNamedQuery("AuthRole.findByName", AuthRole.class)
                .setParameter("name", roleName)
                .getResultList();
        
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        
        return roles.get(0);
    }
    
    /**
     * Find the roles that are permitted to execute given operation on the given resource
     * 
     * @param resource
     * @param slot
     * @param operation
     * @return 
     */
//    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
//    private List<AuthRole> permittedRoles(AuthResource resource, Slot slot, AuthOperation operation) {
//        List<AuthPermission> perms = em.createNamedQuery("AuthPermission.findByResourceOp", AuthPermission.class)
//                .setParameter("resource", resource)
//                .setParameter("operation", operation)
//                .getResultList();
//        List<AuthRole> roles = new ArrayList<>();
//
//        AuthRole indRole;
//        for (AuthPermission perm : perms) {
//            if (perm.getRole() != null) {
//                roles.add(perm.getRole());
//                continue;
//            }
//            if (perm.getProperty() == null) {
//                LOGGER.log(Level.SEVERE, "Both drirect and indirect roles are null! {0}", resource);
//                continue;
//            }
//            if (slot != null) {
//                indRole = indirectRoleT(slot, perm.getProperty());
//                if (indRole == null) {
//                    LOGGER.log(Level.SEVERE, "The indirect role does not have a value {0}", slot.getName() + ":" + perm.getProperty().getName());
//                } else {
//                    roles.add(indRole);
//                }
//            }
//        }
//
//        return roles;
//    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    private <T> List<AuthRole> permittedRoles(AuthResource resource, T entity, AuthOperation operation) {
        List<AuthPermission> perms = em.createNamedQuery("AuthPermission.findByResourceOp", AuthPermission.class)
                .setParameter("resource", resource)
                .setParameter("operation", operation)
                .getResultList();
        List<AuthRole> roles = new ArrayList<>();

        AuthRole indRole;
        for (AuthPermission perm : perms) {
            if (perm.getRole() != null) {
                roles.add(perm.getRole());
                continue;
            }
            if (perm.getProperty() == null) {
                LOGGER.log(Level.SEVERE, "Both drirect and indirect roles are null! {0}", resource);
                continue;
            }
            if (entity != null) {
                indRole = indirectRole(entity, perm.getProperty());
                if (indRole == null) {
                    LOGGER.log(Level.SEVERE, "The indirect role does not have a value {0}", entity.toString() + ":" + perm.getProperty().getName());
                } else {
                    roles.add(indRole);
                }
            }
        }

        return roles;
    }
    
    /**
     * Does the current user belong to the given roles?
     * 
     * @param roles
     * @return 
     */
    public boolean belongsTo(List<AuthRole> roles) {
        AuthUser user = getCurrentUser();

        if (roles == null || roles.isEmpty() || user == null) {
            return false;
        }

        List<AuthUserRole> userRoles = em.createNamedQuery("AuthUserRole.findByUserRole", AuthUserRole.class)
                .setParameter("roles", roles)
                .setParameter("user", user)
                .getResultList();

        return (userRoles != null && !userRoles.isEmpty());
    }
    
    /**
     * Does the current user belong to the given role?
     * 
     * @param role
     * @return 
     */
    public boolean belongsTo(AuthRole role) {
        if (role == null) return false;
        List<AuthRole> roles = new ArrayList<>();
        roles.add(role);
        return belongsTo(roles);
    }
    
    /**
     * Does the given resource have permissions for the given operation?
     * 
     * @param resource
     * @param entity
     * @param operation
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public <T> boolean hasPermission(AuthResource resource, T entity, AuthOperation operation) {
         List<AuthRole> roles = permittedRoles(resource, entity, operation);
         if (roles.isEmpty()) {
             return false;
         }
         
         return belongsTo(roles);
    }
    
    
    /**
     * Is the current user logged in?
     * 
     * @return 
     */
    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }
    
    /**
     * All users
     * 
     * @return 
     */
    public List<AuthUser> findAllUsers() {
        return em.createNamedQuery("AuthUser.findAll", AuthUser.class).getResultList();
    }
    
    /**
     * Are there no users defined? If so, authorization needs to be boostrapped.
     * 
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public boolean needAuthBoostrap() {
        String userId = servletRequest.getUserPrincipal() != null ? servletRequest.getUserPrincipal().getName() : null;
        if (userId == null) return false; // not logged in at all      
        Long num = em.createNamedQuery("AuthUser.countUsers", Long.class).getSingleResult();
        return num <= 1;
    }
}
