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

/**
 *
 * @author vuppala
 */

public enum AuthResource   {
    DEVICE("Device"),
    SLOT("Slot"),
    COMPONENT_TYPE("Device type"),
    USER("User"),
    INSTALLATION_RECORD("Installation record"),
    ALIGNMENT_RECORD("Alignment record"),
    MENU("Menu"),
    UNIT("Unit"),
    PROPERTY("Property"),
    DATA_TYPE("Enumeration");

    private final String label;

    private AuthResource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
