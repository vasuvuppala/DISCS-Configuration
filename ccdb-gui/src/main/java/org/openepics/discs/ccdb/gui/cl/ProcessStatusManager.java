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

import org.openepics.discs.ccdb.gui.ui.util.InputAction;
import org.openepics.discs.ccdb.model.cl.ChecklistEntity;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.openepics.discs.ccdb.core.auth.LocalAuthEJB;
import org.openepics.discs.ccdb.core.ejb.ChecklistEJB;
import org.openepics.discs.ccdb.gui.ui.util.UiUtility;
import org.openepics.discs.ccdb.model.auth.AuthUser;
import org.openepics.discs.ccdb.model.auth.AuthUserRole;
import org.openepics.discs.ccdb.model.auth.User;
import org.openepics.discs.ccdb.model.cl.Process;
import org.openepics.discs.ccdb.model.cl.Assignment;
import org.openepics.discs.ccdb.model.cl.Checklist;
import org.openepics.discs.ccdb.model.cl.ProcessStatus;
import org.openepics.discs.ccdb.model.cl.SlotGroup;
import org.openepics.discs.ccdb.model.cl.StatusOption;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

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
public class ProcessStatusManager implements Serializable {

    public static class SelectablePhase implements Serializable {

        private Boolean selected = false;
        private Process phase;

        private SelectablePhase() {
        }

        public SelectablePhase(Process phase) {
            this.phase = phase;
        }

        public Boolean getSelected() {
            return selected;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }

        public Process getPhase() {
            return phase;
        }

        public void setPhase(Process phase) {
            this.phase = phase;
        }
    }

    @EJB
    private ChecklistEJB lcEJB;
    @EJB
    private LocalAuthEJB authEJB;
//    @Inject
//    private SecurityPolicy securityPolicy;
    @Inject
    private AuthorizationManager authManager;

    private static final Logger LOGGER = Logger.getLogger(ProcessStatusManager.class.getName());

    // request parameters
    private ChecklistEntity entityType = ChecklistEntity.GROUP;
    // private String selectedPhaseGroup;

    // view data
    private List<SlotGroup> slotGroups;
    private List<Process> phases;
    private List<SelectablePhase> selectablePhases;
    private List<Assignment> entities;
    private List<Assignment> filteredEntities;
    private List<StatusOption> statusOptions;
    private List<AuthUser> users;
    private Boolean allPhasesOptional = false; // are all selected phases optional? or is there a mandatory phase?
    private Boolean phaseSelected = false; // is at least one phase selected?
    private List<Assignment> selectedEntities = new ArrayList<>();

    // input data
    private Assignment inputEntity;
    private InputAction inputAction;
    private StatusOption inputStatus;
    private String inputComment;
    private AuthUser inputSME;

    public ProcessStatusManager() {
    }

    @PostConstruct
    public void init() {
        slotGroups = lcEJB.findAllSlotGroups();
        users = authEJB.findAllUsers();
    }

    /**
     * Initialize data in view
     *
     * @param onlyOptionalFields initialize with optional processes only
     * @return
     */
    public String initialize(Boolean onlyOptionalFields) {
        String nextView = null;

        switch (entityType) {
            case GROUP:
                entities = lcEJB.findGroupAssignments();
                break;
            case SLOT:
                entities = lcEJB.findSlotAssignments();
                break;
            case DEVICE:
                entities = lcEJB.findDeviceAssignments();
                break;
            default:
                entities = lcEJB.findGroupAssignments();
        }
        if (entities == null || entities.isEmpty()) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "No assignments were found", "Did you forget to assign checklists?");
        }

        Checklist checklist = lcEJB.findDefaultChecklist(entityType);
        if (checklist == null) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "No default checklist found", "Make sure checklists are configured properly.");
            return nextView;
        } else {
            phases = onlyOptionalFields ? lcEJB.findOptionalFields(checklist) : lcEJB.findPhases(checklist);
            statusOptions = lcEJB.findStatusOptions(checklist);
        }
        selectablePhases = phases == null ? Collections.<SelectablePhase>emptyList() : phases.stream().map(p -> new SelectablePhase(p)).collect(Collectors.toList());
        updatePhaseSelected();

        return nextView;
    }

    /**
     * Is an assignment locked/frozen i.e. its summary phase is completed (set
     * to Y or YC?
     *
     * @param assignment
     * @return
     */
    public Boolean lockedAssignment(Assignment assignment) {
        if (assignment == null) {
            return false;
        }
        // return assignment.getStatuses().stream().filter(s -> s.getGroupMember().getSummaryPhase()).allMatch(s -> s.getStatus() == null? false: s.getStatus().getCompleted());
        Boolean hasSummary = false;
        for (ProcessStatus status : assignment.getStatuses()) {
            if (status.getField().getSummaryProcess() && status.getStatus() != null) {
                hasSummary = true;
                if (!status.getStatus().getCompleted()) {
                    return false;
                }
            }
        }
        return hasSummary;
    }

    /**
     * Is a process of an assignment locked/frozen? A process is locked if the
     * assignment is locked unless it is a summary process.
     *
     * @param assignment
     * @param phase
     * @return
     */
    public Boolean lockedAssignment(Assignment assignment, Process phase) {
        ProcessStatus phaseStatus = getStatusRec(assignment, phase);
        if (phaseStatus == null) {
            return true;
        }
//        if (! authManager.canUpdateStatus(phaseStatus)) {
//            return true;
//        }
        if (phaseStatus.getField().getSummaryProcess()) {
            return false;
        }
        return lockedAssignment(assignment);
    }

    /**
     * Is a process for an assignment required?
     *
     * @param assignment
     * @param phase
     * @return
     */
    public Boolean notRequired(Assignment assignment, Process phase) {
        ProcessStatus processStatus = getStatusRec(assignment, phase);
        if (processStatus == null) {
            return true;
        }
        return processStatus.getStatus() == null;
    }

    /**
     * Can the user edit the given field?
     * 
     * @param processStatus
     * @return 
     */
    public Boolean canEditField(ProcessStatus processStatus) {      
        if (processStatus == null) {
            return false;
        }
        if (processStatus.getStatus() == null) {
            return false; // Field has been excluded from the checklist
        }

        if (!authManager.canUpdateStatus(processStatus)) { // is user authorized?
            LOGGER.log(Level.WARNING, "User not authorized to edit the field/process");
            return false;
        }
        if (processStatus.getField().getSummaryProcess()) { // simmary processes are always editable
            return true;
        }
        if (lockedAssignment(processStatus.getAssignment())) { // is the entire assignment locked
            return false;
        }
        return true;
    }
    
    /**
     * Can the user edit the given assignment and process?
     * 
     * @param assignment
     * @param phase
     * @return 
     */
    public Boolean canEditField(Assignment assignment, Process phase) {
        ProcessStatus processStatus = getStatusRec(assignment, phase);
        return canEditField(processStatus);
    }

//    public Boolean lockedOrOptionalAssignment(Assignment assignment, Process phase) {
//        ProcessStatus phaseStatus = getStatusRec(assignment, phase);
//        if (phaseStatus == null) {
//            return false;
//        }
//        if (phaseStatus.getStatus() == null) { // is the process optional (not required)?
//            return true;
//        }
//        if (phaseStatus.getField().getSummaryProcess()) {
//            return false;
//        }
//        return lockedAssignment(assignment);
//    }
    /**
     * reset all input fields
     *
     */
    public void resetInput() {
        inputAction = InputAction.READ;
        inputComment = null;
        if (statusOptions != null) {
            inputStatus = statusOptions.get(0);
        }
        selectedEntities.clear();
        selectablePhases.forEach(p -> {
            p.setSelected(false);
        });
        updatePhaseSelected();
    }

    /**
     * when a row is selected
     *
     * @param event
     */
    public void onRowSelect(SelectEvent event) {
    }

    /**
     * Are all the selected phases optional?
     *
     * @return
     */
    private boolean findOptional() {
        // LOGGER.log(Level.INFO, "get all optional", "enter");
        for (SelectablePhase selectedPhase : selectablePhases) {
            if (!selectedPhase.selected) {
                continue;
            }
            for (Assignment record : selectedEntities) {
                for (ProcessStatus status : record.getStatuses()) {
                    // LOGGER.log(Level.INFO, "checking {0}", status.getGroupMember().getPhase().getName());
                    if (selectedPhase.phase.equals(status.getField().getProcess()) && status.getField().getOptional() == false) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * check if at least one phase is selected.
     *
     */
    private void updatePhaseSelected() {
        phaseSelected = selectablePhases.stream().anyMatch(p -> p.selected == true);
    }

    /**
     * when 'add' button is activated
     *
     * @param event
     */
    public void onAddCommand(ActionEvent event) {
        inputEntity = new Assignment();
        inputAction = InputAction.CREATE;
    }

    /**
     * when Edit button is activated
     *
     * @param event
     */
    public void onEditCommand(ActionEvent event) {
        inputAction = InputAction.UPDATE;
        allPhasesOptional = findOptional();
        LOGGER.log(Level.INFO, "allphaseOptional {0}", allPhasesOptional);
    }

    /**
     * when delete button is activated
     *
     * @param event
     */
    public void onDeleteCommand(ActionEvent event) {
        inputAction = InputAction.DELETE;
    }

    /**
     * is input valid?
     *
     * @return
     */
    private boolean inputValid() {
        for (Assignment record : selectedEntities) {
            for (ProcessStatus status : record.getStatuses()) {
                if (status.getField().getSummaryProcess() && !isValid(status)) {
                    UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Invalid summary status",
                            "Make sure status for summary (eg AM OK) is valid.");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Convert status to
     *
     * @param status
     * @return
     */
    private Integer toInt(StatusOption status) {
        return status == null ? 0 : status.getWeight();
    }

    /**
     * Weight of a given phase status. If it is selected then use the input
     * status, otherwise use the stored status.
     *
     * @param status
     * @return
     */
    private Integer toInt(ProcessStatus status) {
        for (SelectablePhase selPhase : selectablePhases) {
            if (selPhase.getSelected() && selPhase.getPhase().equals(status.getField().getProcess())) {
                return toInt(inputStatus);
            }
        }
        return toInt(status.getStatus());
    }

    /**
     * is the status of the given phase valid?
     *
     * @param summaryStatus
     * @return
     */
    private boolean isValid(ProcessStatus summaryStatus) {
        Integer summaryWeight = summaryWeight(summaryStatus);
        Integer inputWeight = toInt(summaryStatus);

        return inputWeight <= summaryWeight;
    }

    /**
     * Get the weight of the summary status
     *
     * @param status
     * @return
     */
    private Integer summaryWeight(ProcessStatus status) {
        List<ProcessStatus> statuses = lcEJB.findAllStatuses(status.getAssignment());
        Integer minWeight = statuses.stream()
                .filter(stat -> stat.getField().getSummaryProcess() == false)
                .filter(stat -> stat.getStatus() != null)
                .map(stat -> toInt(stat.getStatus()))
                .min(Integer::compare)
                .get();

        return minWeight;
    }

    /**
     * when a phase is selected/unselected
     *
     * @param phase
     */
    public void onTogglePhase(Process phase) {
        // selectablePhases.stream().filter(p -> p.phase.equals(phase)).forEach(u -> u.setSelected(!u.selected));
        updatePhaseSelected();
        // UiUtility.showMessage(FacesMessage.SEVERITY_INFO, "Toggle phase ", phase.getName());
    }

    /**
     *
     * @param status
     * @return
     */
//    public String summaryStatus(PhaseStatus status) {
//        if (!status.getGroupMember().getSummaryPhase()) {
//            return "";
//        }
//
//        for (PhaseStatus stat : lcEJB.findAllStatuses(status.getAssignment())) {
//            if (!stat.getGroupMember().getSummaryPhase() && stat.getStatus() != null && "N".equals(stat.getStatus().getName())) {
//                return "N";
//            }
//        }
//
//        for (PhaseStatus stat : lcEJB.findAllStatuses(status.getAssignment())) {
//            if (!stat.getGroupMember().getSummaryPhase() && stat.getStatus() != null && "YC".equals(stat.getStatus().getName())) {
//                return "YC";
//            }
//        }
//
//        return "Y";
//    }
    /**
     * When a cell is clicked for edits
     *
     * @param assignment
     * @param phase
     */
    public void onCellEdit(Assignment assignment, Process phase) {
        ProcessStatus statusRec = getStatusRec(assignment, phase);
        inputStatus = null;
        inputSME = null;
        if (statusRec != null) {
            inputStatus = statusRec.getStatus();
            inputSME = statusRec.getAssignedSME();
            inputComment = statusRec.getComment();
        } else {
            LOGGER.log(Level.WARNING, "Status record not found!");
        }
        selectedEntities.clear();
        selectedEntities.add(assignment);

        for (SelectablePhase selectedPhase : selectablePhases) {
            selectedPhase.setSelected(selectedPhase.phase.equals(phase));
        }
        allPhasesOptional = findOptional();
    }

    /**
     * Check if user is authorized to update the status
     *
     * ToDo: Remove 'view' or make is enum
     *
     * @param status
     * @param user
     * @return
     */
    private boolean isAuthorized(String view, ProcessStatus status) {
        switch (view) {
            case "sme":
                return authManager.canAssignSME(status.getAssignment());
            case "status":
                return canEditField(status);
            default:
                LOGGER.log(Level.SEVERE, "Invallid view {0}", view);
                break;
        }
        return false;
    }

    /**
     * Update status of the selected entities and phases ToDo: Remove 'view'
     * parameter
     *
     * @param view
     */
    public void saveEntity(String view) {
        try {
            Preconditions.checkNotNull(selectedEntities);
            if (!inputValid()) {
                UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Fix validation errors", "");
                RequestContext.getCurrentInstance().addCallbackParam("success", false);
                return;
            }

            // ToDo: Improve. really bad code
            for (SelectablePhase selectedPhase : selectablePhases) {
                if (!selectedPhase.selected) {
                    continue;
                }
                for (Assignment record : selectedEntities) {
                    for (ProcessStatus status : record.getStatuses()) {
                        if (selectedPhase.phase.equals(status.getField().getProcess())) {
                            if (!isAuthorized(view, status)) {
                                UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Update Failed",
                                        "You are not authorized to update one or more of status fields.");
                                RequestContext.getCurrentInstance().addCallbackParam("success", false);
                                return;
                            }
                        }
                    }
                }
            }

            AuthUser currentUser = authEJB.getCurrentUser();
//            if (currentUser == null) {
//                UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Update Failed",
//                        "You are not authorized. User Id is null.");
//                RequestContext.getCurrentInstance().addCallbackParam("success", false);
//                return;
//            }     
            // ToDo: Improve. really bad code
            for (SelectablePhase selectedPhase : selectablePhases) {
                if (!selectedPhase.selected) {
                    continue;
                }
                for (Assignment record : selectedEntities) {
                    for (ProcessStatus status : record.getStatuses()) {
                        if (selectedPhase.phase.equals(status.getField().getProcess())) {
                            status.setModifiedAt(new Date());
                            status.setModifiedBy(currentUser.getUserId());
                            status.setStatus(inputStatus);
                            status.setComment(inputComment);
                            status.setAssignedSME(inputSME);
                            lcEJB.saveProcessStatus(status);
                            lcEJB.refreshVersion(ProcessStatus.class, status); // ToDo: to update the version field
                            // lcEJB.refreshVersion(status); // ToDo: to update the version field
                        }
                    }
                }
            }
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, UiUtility.MESSAGE_SUMMARY_SUCCESS,
                    "Update successful.");
            resetInput();
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Error. Could not update status.", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);

        } finally {
//            resetInput();
        }
    }

    /**
     * Include/exclude processes to/from selected assignments ToDo: should be
     * combined with saveEntity()
     *
     * @param exclude
     */
    public void assignProcesses(Boolean exclude) {
        try {
            Preconditions.checkNotNull(selectedEntities);
            if (!authManager.canAssignProcesses(selectedEntities)) {
                UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Authorization Failure", "You are not authorized to assign checklists to one or more of the selected entities");
                return;
            }
            AuthUser currentUser = authEJB.getCurrentUser();
            if (currentUser == null) {
                UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Update Failed",
                        "You are not authorized. User Id is null.");
                RequestContext.getCurrentInstance().addCallbackParam("success", false);
                return;
            }

            // ToDo: Improve. really bad code
            for (SelectablePhase selectedPhase : selectablePhases) {
                if (!selectedPhase.selected) {
                    continue;
                }
                for (Assignment record : selectedEntities) {
                    for (ProcessStatus status : record.getStatuses()) {
                        if (selectedPhase.phase.equals(status.getField().getProcess())) {
                            status.setModifiedAt(new Date());
                            status.setModifiedBy(currentUser.getUserId());
                            status.setStatus(exclude ? null : status.getField().getDefaultStatus());
                            // status.setComment(inputComment);                        
                            lcEJB.saveProcessStatus(status);
                            lcEJB.refreshVersion(ProcessStatus.class, status); // ToDo: to update the version field
                            // lcEJB.refreshVersion(status); // ToDo: to update the version field
                        }
                    }
                }
            }
            RequestContext.getCurrentInstance().addCallbackParam("success", true);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, UiUtility.MESSAGE_SUMMARY_SUCCESS,
                    "Update successful.");
            resetInput();
        } catch (Exception e) {
            UiUtility.showMessage(FacesMessage.SEVERITY_ERROR, "Error. Could not update status.", e.getMessage());
            RequestContext.getCurrentInstance().addCallbackParam("success", false);
            System.out.println(e);

        } finally {
//            resetInput();
        }
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

    /**
     * Default SMEs of a checklist field
     *
     * @param status
     * @return
     */
    public String defaultSME(ProcessStatus status) {
        List<AuthUserRole> userRoles = status.getField().getSme() == null ? null : status.getField().getSme().getUserRoleList();
        if (userRoles == null || userRoles.isEmpty()) {
            return "None";
        } else {
//            return userRoles.get(0).getUser().getUserId();
            StringJoiner smes = new StringJoiner(", ", "", "");
            for (AuthUserRole userRole : userRoles) {
                smes.add(userRole.getUser().getName());
            }
            return smes.toString();
        }
    }

    //-- Getters/Setters 
    public InputAction getInputAction() {
        return inputAction;
    }

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

    public Assignment getInputEntity() {
        return inputEntity;
    }

    public void setInputEntity(Assignment inputEntity) {
        this.inputEntity = inputEntity;
    }

    public List<Assignment> getSelectedEntities() {
        return selectedEntities;
    }

    public void setSelectedEntities(List<Assignment> selectedEntities) {
        this.selectedEntities = selectedEntities;
    }

    public List<SlotGroup> getSlotGroups() {
        return slotGroups;
    }

    public StatusOption getInputStatus() {
        return inputStatus;
    }

    public void setInputStatus(StatusOption inputStatus) {
        this.inputStatus = inputStatus;
    }

    public String getInputComment() {
        return inputComment;
    }

    public void setInputComment(String inputComment) {
        this.inputComment = inputComment;
    }

    public List<StatusOption> getStatusOptions() {
        return statusOptions;
    }

    public Boolean getAllPhasesOptional() {
        return allPhasesOptional;
    }

    public List<SelectablePhase> getSelectablePhases() {
        return selectablePhases;
    }

    public Boolean getPhaseSelected() {
        return phaseSelected;
    }

    public AuthUser getInputSME() {
        return inputSME;
    }

    public void setInputSME(AuthUser inputSME) {
        this.inputSME = inputSME;
    }

    public List<AuthUser> getUsers() {
        return users;
    }

    public ChecklistEntity getEntityType() {
        return entityType;
    }

    public void setEntityType(ChecklistEntity entityType) {
        this.entityType = entityType;
    }
}
