/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of CCDB System.
 *
 * CCDB is free software: you can redistribute it
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
package org.openepics.discs.ccdb.model.auth;

import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openepics.discs.ccdb.model.ConfigurationEntity;

/**
 * A user
 * 
 * @author vuppala
 */
@Entity
@Table(name = "auth_user")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AuthUser.findAll", query = "SELECT u FROM AuthUser u"),
    @NamedQuery(name = "AuthUser.findByUserId", query = "SELECT u FROM AuthUser u WHERE u.userId = :userId"),
    @NamedQuery(name = "AuthUser.findByName", query = "SELECT u FROM AuthUser u WHERE u.name = :name"),
    @NamedQuery(name = "AuthUser.findByEmail", query = "SELECT u FROM AuthUser u WHERE u.email = :email"),
    @NamedQuery(name = "AuthUser.findByComment", query = "SELECT u FROM AuthUser u WHERE u.comment = :comment")
})
public class AuthUser extends ConfigurationEntity {
    private static final long serialVersionUID = 1L;
   
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "user_id", unique=true)
    private String userId;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "name")
    private String name; // short or nickname
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "last_name")
    private String lastName;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "first_name")
    private String firstName;
    
    @Basic(optional=false)
    @Size(max = 64)
    @NotNull
    @Column(name = "email")
    private String email;

    @Size(max = 255)
    @Column(name = "comment")
    private String comment;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<UserRole> userRoles;

    public AuthUser() {
    }

    /** Constructs a new user
     * @param userId the ID of the user
     */
    public AuthUser(String userId) {
        this.userId = userId;
    }

    /** Constructs a new user
     * @param userId the ID of the user
     * @param name the name of the user
     */
    public AuthUser(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public AuthUser(String userId, String name, String last_name, String first_name, String email, String comment) {
        this.userId = userId;
        this.name = name;
        this.lastName = last_name;
        this.firstName = first_name;
        this.email = email;
        this.comment = comment;
    }
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @XmlTransient
    @JsonIgnore
    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoleList(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }
}
