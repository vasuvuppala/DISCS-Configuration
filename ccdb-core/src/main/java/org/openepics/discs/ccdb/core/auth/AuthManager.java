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

import javax.ejb.Local;
import javax.management.relation.Role;
import org.openepics.discs.ccdb.model.auth.AuthUser;


/**
 * Interface to authorization system. 
 * The user is assumed to be the current user.
 * 
 * TODO: Add methods to check for a given user
 * 
 * @author vuppala
 */
@Local
public interface AuthManager {
    /**
     * Is the current user a valid user?
     * 
     * @return 
     */
    boolean isValidUser();
    
    /**
     * Can the current user edit the given log entry?
     * 
     * @param entry
     * @return 
     */
    // boolean canEditLog(LogEntry entry);
    
    /**
     * can the current user create log entries?
     * 
     * @return 
     */
    boolean canCreateLog(); 
    
    /**
     * can the current user update facility status?
     * @return 
     */
    boolean canUpdateStatus();  
    
    /**
     * can the current user change shift information?
     * 
     * @return 
     */
    boolean canOperateShift();
    
    /**
     * can the current user manage facilities?
     * @return 
     */
    boolean canManageFacility();
    
    /**
     * Can the given user manage the given role?
     * 
     * @param user
     * @param role
     * @return 
     */
    boolean canManageRole(AuthUser user, Role role);
    
    /** 
     * Can the current user generate reports?
     * @return 
     */
    boolean canGenerateReports();
    
    /**
     * can the current user execute this application?
     * 
     * @return 
     */
    boolean canExecuteApp();
    
    /**
     * Can the current user set a log entry's date to any date?
     * 
     * @return 
     */
    boolean canSetAnyDate();
}
