/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.discs.ccdb.model.auth;

/**
 *
 * @author vuppala
 */
public enum AuthOperation {
    MANAGE("Add, modfiy, delete the resource"),
    ASSIGN_CHECKLISTS("Assign checklists to slots, groups, and devices"),  
    APPROVE_DRR("Approve Device Readiness Review"),  // ToDo: lab specific. how to make it generic?
    APPROVE_ARR("Approve Accelerator Readines Review"),  // ToDo: lab specific. how to make it generic?    
    ;

    private final String description;

    AuthOperation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
