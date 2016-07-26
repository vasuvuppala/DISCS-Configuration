/*
 * This software is Copyright by the Board of Trustees of Michigan
 *  State University (c) Copyright 2014, 2015.
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
package org.openepics.discs.ccdb.model.auth;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import org.openepics.discs.ccdb.model.ConfigurationEntity;

/**
 *
 * @author vuppala
 */
@Entity
@Table(name = "auth_permission")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AuthPermission.findAll", query = "SELECT a FROM AuthPermission a"),
    @NamedQuery(name = "AuthPermission.findByResource", query = "SELECT a FROM AuthPermission a WHERE a.resource = :resource"),
    @NamedQuery(name = "AuthPermission.findByRole", query = "SELECT a FROM AuthPermission a WHERE a.role = :role"),
    @NamedQuery(name = "AuthPermission.findByOperation", query = "SELECT a FROM AuthPermission a WHERE a.operation = :operation")
})
public class AuthPermission extends ConfigurationEntity {

    private static final long serialVersionUID = 1L;

    @Basic(optional=false)
    @NotNull
    @Column(name="resource")
    @Enumerated(EnumType.STRING)
    private AuthResource resource;

    @Basic(optional=false)
    @NotNull
    @Column(name="operation")
    @Enumerated(EnumType.STRING)
    private AuthOperation operation;

    @JoinColumn(name = "role", referencedColumnName = "role_id")
    @ManyToOne(optional = false)
    @NotNull
    private AuthRole role;

    public AuthPermission() {
    }

    public AuthResource getResource() {
        return resource;
    }

    public void setResource(AuthResource resource) {
        this.resource = resource;
    }

    public AuthOperation getOperation() {
        return operation;
    }

    public void setOperation(AuthOperation operation) {
        this.operation = operation;
    }

    public AuthRole getRole() {
        return role;
    }

    public void setRole(AuthRole role) {
        this.role = role;
    }

}
