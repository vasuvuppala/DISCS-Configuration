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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.openepics.discs.ccdb.core.auth.AnAEJB;
import org.openepics.discs.ccdb.model.auth.AuthUser;
import org.openepics.discs.ccdb.model.auth.AuthRole;


/**
 * The session.
 * 
 * @author vuppala
 */
@Named
@SessionScoped
public class UserSession implements Serializable {      
    @EJB
    private AnAEJB authEJB;
    
    private static final Logger LOGGER = Logger.getLogger(UserSession.class.getName());

    private String userId; // user unique login name
    private String token;   // auth token
    private AuthRole role; // current role   
    private AuthUser user; // the user record. ToDo:  keep this in the session?
    private String currentTheme; // current GUI theme

    /**
     * Initialize user session.
     *
     * @author vuppala
     *
     */
    @PostConstruct
    public void init() {
        try {
            LOGGER.log(Level.INFO, "UserSession: PostConstruct: initializing");
            // currentTheme = prefEJB.defaultThemeName();                        
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "UserSession: Can not initialize: {0}", e.getMessage());
        }
    }

    /**
     * Start user session
     *
     * @author vuppala
     * @param userId User login id
     * @param role User role
     *
     */
    public void start(String userId, AuthRole role)  {
        this.userId = userId;
        this.role = role;
        user = authEJB.findUser(userId);
        
        if (user != null) {
            //UserPreference pref = prefEJB.findPreference(user, PreferenceName.DEFAULT_THEME);
//            if (pref != null) {
//                currentTheme = pref.getPrefValue();
//            }
        } else {
            LOGGER.log(Level.WARNING, "User not defined in the CCDB database: {0}", userId);
            // Utility.showMessage(FacesMessage.SEVERITY_FATAL, "You are not registered as Hour Log user", "Please contact Hour Log administrator.");
        }
        
//        if (currentTheme == null) {
//            currentTheme = prefEJB.defaultThemeName();
//        }       
    }

    
    /**
     * end user session
     *
     * @author vuppala
     *
     */
    public void end() {
        userId = null;
        user = null;
        role = null;
        // loggedIn = false;
        token = null;
    }

    // -- getters/setters 
    
    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AuthUser getUser() {
        return user;
    }
    
    public AuthRole getRole() {
        return role;
    }

    public void setRole(AuthRole role) {
        this.role = role;
    }

    public String getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(String currentTheme) {
        this.currentTheme = currentTheme;
    }
}
