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
package org.openepics.discs.ccdb.gui.cl;

import org.openepics.discs.ccdb.model.cl.ChecklistEntity;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.openepics.discs.ccdb.core.auth.LocalAuthEJB;
import org.openepics.discs.ccdb.core.ejb.ChecklistEJB;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.cl.Process;
import org.openepics.discs.ccdb.model.cl.Assignment;
import org.openepics.discs.ccdb.model.cl.Checklist;
import org.openepics.discs.ccdb.model.cl.ProcessStatus;
import org.openepics.discs.ccdb.model.cl.StatusOption;

/**
 * Description: State for Manage Phase View
 *
 * Methods:
 * <p>
 * Init: to initialize the state
 * <p>
 * resetInput: reset all inputs on the view
 * <p>
 * onRowSelect: things to do when an item is selected
 * <p>
 * onAddCommand: things to do before adding an item
 * <p>
 * onEditCommand: things to do before editing an item
 * <p>
 * onDeleteCommand: things to do before deleting an item
 * <p>
 * saveXXXX: save the input or edited item
 * <p>
 * deleteXXXX: delete the selected item
 *
 * @author vuppala
 *
 */
@Named
@ViewScoped
public class AssignmentStatusReport implements Serializable {

    @EJB
    private ChecklistEJB lcEJB;

    private static final Logger LOGGER = Logger.getLogger(AssignmentStatusReport.class.getName());

    // request parameters
    private ChecklistEntity entityType = ChecklistEntity.SLOT;

    // view data
    private List<Process> phases;
    private List<Assignment> entities;
    private List<Assignment> filteredEntities;

    public AssignmentStatusReport() {
    }

    @PostConstruct
    public void init() {
       
    }

    /**
     * Initialize data in view
     *
     * 
     * @return
     */
    public String initialize() {
        String nextView = null;

        switch (entityType) {
            case SLOT:
                entities = lcEJB.findGroupSlotAssignments();              
                break;
            case DEVICE:
                UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Invalid type", "You cannot use this for Devices");
                // entities = lcEJB.findDeviceAssignments();
                break;
            default:
                entities = lcEJB.findGroupSlotAssignments();
        }
        if (entities == null || entities.isEmpty()) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "No assignments were found", "Did you forget to assign checklists?");
        }

        Checklist checklist = lcEJB.findDefaultChecklist(entityType);
        if (checklist == null) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "No default checklist found", "Make sure checklists are configured properly.");
            return nextView;
        } else {
            phases = lcEJB.findPhases(checklist);
        }

        return nextView;
    }
    
    /**
     * Find status record for the given assignment and phase
     *
     * @param assignment
     * @param phase
     * @return
     */
    public ProcessStatus getStatusRec(Assignment assignment, Process phase) {

        if (assignment == null || phase == null) {
            LOGGER.log(Level.WARNING, "assignment or phase is null");
            return null;
        }
        for (ProcessStatus status : assignment.getStatuses()) {
            // LOGGER.log(Level.INFO, "phase {0}", status.getGroupMember().getPhase());
            if (phase.equals(status.getField().getProcess())) {
                return status;
            }
        }

        return null;
    }

   
    //-- Getters/Setters 
    
    public List<Assignment> getEntities() {
        return entities;
    }

    public void setEntities(List<Assignment> entities) {
        this.entities = entities;
    }

    public List<Assignment> getFilteredEntities() {
        return filteredEntities;
    }

    public void setFilteredEntities(List<Assignment> filteredEntities) {
        this.filteredEntities = filteredEntities;
    }

    public ChecklistEntity getEntityType() {
        return entityType;
    }

    public void setEntityType(ChecklistEntity entityType) {
        this.entityType = entityType;
    }

    public List<Process> getPhases() {
        return phases;
    }
    
}
