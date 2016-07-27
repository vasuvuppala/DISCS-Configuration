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
    ALIGNMENT_RECORD("Alignment record"),
    AUTHORIZATIONS("Authorizations"),
    CHECKLIST("Checklist"),
    COMPONENT_TYPE("Device type"),
    DATA_TYPE("Enumeration"),
    DEVICE("Device"),  
    INSTALLATION_RECORD("Installation record"),
    MENU("Menu"),
    PROPERTY("Property"),
    SLOT("Slot"),
     SLOT_GROUP("Slot Group"),
    UNIT("Unit"),
    USER("User");

    private final String label;

    private AuthResource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
