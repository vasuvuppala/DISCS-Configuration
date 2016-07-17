/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2012.
 *
 * You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *       http://www.gnu.org/licenses/gpl.txt
 *
 * Contact Information:
 *   Facilitty for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 *
 */
package org.openepics.discs.ccdb.core.ejb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.discs.ccdb.core.security.SecurityPolicy;
import org.openepics.discs.ccdb.model.ConfigurationEntity;
import org.openepics.discs.ccdb.model.Device;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.auth.User;
import org.openepics.discs.ccdb.model.cl.ChecklistEntity;
import org.openepics.discs.ccdb.model.cl.Process;
import org.openepics.discs.ccdb.model.cl.Assignment;
import org.openepics.discs.ccdb.model.cl.ProcessStatus;
import org.openepics.discs.ccdb.model.cl.Checklist;
import org.openepics.discs.ccdb.model.cl.ChecklistField;
import org.openepics.discs.ccdb.model.cl.SlotGroup;
import org.openepics.discs.ccdb.model.cl.StatusOption;

/**
 *
 *  Status of life cycle phases of slots or devices
 * 
 * @author vuppala
 *
 */
@Stateless
public class ChecklistEJB {       
    @Inject
    private SecurityPolicy securityPolicy;

    private static final Logger LOGGER = Logger.getLogger(ChecklistEJB.class.getName());   
    @PersistenceContext private EntityManager em;
    /**
     * All reviews
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    public List<Process> findAllPhases() {
        return em.createNamedQuery("Process.findAll", Process.class).getResultList();
    } 
    
     /**
     * Reviews with a given tag
     * 
     * @param group
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    public List<Process> findPhases(Checklist group) {
        return em.createNamedQuery("ChecklistField.findPhasesByGroup", Process.class).setParameter("group", group).getResultList();
    }
    
    
    /**
     * save a process
     *
     * @param phase
     */
    public void savePhase(Process phase) {
        if (phase.getId() == null) {
            em.persist(phase);
        } else {
            em.merge(phase);
        }
        LOGGER.log(Level.FINE, "process saved - {0}", phase.getId());
    }

    /**
     * delete a given process
     *
     * @param phase
     */
    public void deletePhase(Process phase) {
        Process src = em.find(Process.class, phase.getId());
        em.remove(src);
    }

    /**
     * find a process given its id
     *
     * @param id
     * @return the process
     */
    public Process findPhase(Long id) {
        return em.find(Process.class, id);
    }
    
    // ----------------- Assignments 
    /**
     * All assignments
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
//    public List<Assignment> findAllAssignments() {
//        return em.createNamedQuery("Assignment.findAll", Assignment.class).getResultList();
//    }
    
    public List<Assignment> findGroupAssignments() {
        return em.createNamedQuery("Assignment.findGroupAssignments", Assignment.class).getResultList();
    }
    
    public List<Assignment> findSlotAssignments() {
        return em.createNamedQuery("Assignment.findSlotAssignments", Assignment.class).getResultList();
    }
    
    public List<Assignment> findDeviceAssignments() {
        return em.createNamedQuery("Assignment.findDeviceAssignments", Assignment.class).getResultList();
    }
    
    /**
     * All assignments of a device
     * 
     * @param device
     * @return list of checklist assignments
     */    
    public List<Assignment> findAssignments(Device device) {
        return em.createNamedQuery("Assignment.findByDevice", Assignment.class).setParameter("device", device).getResultList();      
    }
    
    /**
     * All assignments of a group
     * 
     * @param group
     * @return list of checklist assignments
     */    
    public List<Assignment> findAssignments(SlotGroup group) {
        return em.createNamedQuery("Assignment.findBySlotGroup", Assignment.class).setParameter("group", group).getResultList();      
    }

    /**
     * Find all assignments of a slot
     * 
     * @param slot
     * @return list of checklist assignments
     */
    public List<Assignment> findAssignments(Slot slot) {
        return em.createNamedQuery("Assignment.findBySlot", Assignment.class).setParameter("slot", slot).getResultList();        
    }
//    public List<Assignment> findAssignments(Checklist type) {
//        return em.createNamedQuery("Assignment.findByGroup", Assignment.class).setParameter("group", type).getResultList();
//    }
//    
//    public Assignment findAssignment(SlotGroup group) {
//        List<Assignment> result = em.createNamedQuery("Assignment.findBySlotGroup", Assignment.class).setParameter("group", group).getResultList();
//        if (result == null || result.isEmpty()) {
//            return null;
//        } else {
//            return result.get(0);
//        }
//    }
//    
//    public Assignment findAssignment(Slot slot) {
//        List<Assignment> result = em.createNamedQuery("Assignment.findBySlot", Assignment.class).setParameter("slot", slot).getResultList();
//        if (result == null || result.isEmpty()) {
//            return null;
//        } else {
//            return result.get(0);
//        }
//    }
    
    /**
     * ToDo: either make a named query or move to another module
     * 
     * @param groups
     * @return 
     */
    public List<Slot> findSlots(Set<SlotGroup> groups) {
        return em.createQuery("SELECT s FROM Slot s where s.cmGroup IN :groups", Slot.class).setParameter("groups", groups).getResultList();
    }
    /**
     * save a process
     *
     * @param assignment
     */
    public void saveAssignment(Assignment assignment) {
        if (assignment.getId() == null) {
            em.persist(assignment);
        } else {
            em.merge(assignment);
        }
        LOGGER.log(Level.FINE, "phase assignment  saved - {0}", assignment.getId());
    }

    /**
     * 
     * @param approvals
     * @param approver
     * @return 
     */
//    private boolean isAnApprover(List<PhaseApproval> approvals, User approver) {
//        if (approvals == null || approver == null ) {
//            return false;
//        }
//        for (PhaseApproval approval : approvals) {
//            if (approver.equals(approval.getAssignedApprover())) {
//                return true;
//            }
//        }
//        return false;
//    }
    
    /**
     * ToDo: Improve the code with set operations.
     * 
     * @param assignment
     * @param approvers 
     */
    public void saveAssignment(Assignment assignment, List<User> approvers) {       
        List<ProcessStatus> statuses = new ArrayList<>();
        
        for(ChecklistField pog : assignment.getPhaseGroup().getPhases()) {
            ProcessStatus phaseStatus = new ProcessStatus();
            phaseStatus.setAssignment(assignment);
            phaseStatus.setGroupMember(pog);
            phaseStatus.setStatus(pog.getDefaultStatus());
            // em.persist(phaseStatus);
            statuses.add(phaseStatus);
        }

        assignment.setStatuses(statuses);
        if (assignment.getId() == null) {
            em.persist(assignment);
        } else {
            em.merge(assignment);
        }
    }
    
    /**
     * Default process list for the given entity type
     * 
     * @param type
     * @return 
     */
    public Checklist findDefaultChecklist(ChecklistEntity type) {
        // due to different class loaders, it is safer get the type explicitely rather than inferring it from class
        List<Checklist> checklist;

        switch (type) {
            case SLOT:
            case GROUP:
                checklist = em.createNamedQuery("Checklist.findDefaultForSlots", Checklist.class).getResultList();
                break;
            case DEVICE:
                checklist = em.createNamedQuery("Checklist.findDefaultForDevices", Checklist.class).getResultList();
                break;
            default:
                LOGGER.log(Level.SEVERE, "Invalid CM entity type {0}", type);
                checklist = em.createNamedQuery("Checklist.findDefaultForSlots", Checklist.class).getResultList();
                break;
        }

        if (checklist == null || checklist.isEmpty()) {
            LOGGER.log(Level.SEVERE, "No checklists defined");
            return null;
        } else {
            return checklist.get(0);
        }
    }
    
    /**
     * find slots not assigned any checklists.
     * 
     * @return 
     */
//    public List<Slot> findUnassignedSlots() {
//        return em.createNamedQuery("Assignment.findUnassignedSlots").getResultList();
//    }
    
    /**
     * find slots not assigned any checklists.
     * 
     * @return 
     */
//    public List<Slot> findAssignedSlots() {
//        return em.createNamedQuery("Assignment.findAssignedSlots").getResultList();
//    }
    
    /**
     * Find slots that are not internal system slots
     * 
     * @return 
     */
    public List<Slot> findUngroupedSlots() {
        return em.createNamedQuery("Slot.findUngroupedSlots").getResultList();
    }
    
    /**
     * Does none of the given slots have assignments?
     * 
     * @param type
     * @param entities
     * @return 
     */
    public Boolean noneHasChecklists(ChecklistEntity type, List<?> entities) {
        if (entities == null || entities.isEmpty()) {
            return false;
        }
        Boolean result = false;
        switch (type) {
            case GROUP:
                result = 0 == em.createNamedQuery("Assignment.numberOfAssignedGroups", Long.class).setParameter("groups", entities).getSingleResult();
                break;
            case SLOT:
                result = 0 == em.createNamedQuery("Assignment.numberOfAssignedSlots", Long.class).setParameter("slots", entities).getSingleResult();
                break;
            case DEVICE:
                result = 0 == em.createNamedQuery("Assignment.numberOfAssignedDevices", Long.class).setParameter("devices", entities).getSingleResult();
                break;
            default:
                LOGGER.log(Level.SEVERE, "Invalid CM entity type {0}", type);
                break;
        }
        return result;
    }
    
    /**
     * Assign checklist to a set of slots, devices or groups
     * 
     * @param <T>
     * @param entities
     * @param checklist 
     */
    public <T> void assignChecklist(List<T> entities, Checklist checklist) {   
        LOGGER.log(Level.INFO, "Creating checklist for {0} entities", entities.size());
        String userId = securityPolicy.getUserId();
        if (userId == null) {
            throw new SecurityException();
        }
        User currentUser = new User(userId); // ToDo: Improve the security/authentication code
        
        for (T entity : entities) {
            Assignment assignment = new Assignment();
            if (entity instanceof Slot) { // may not be safe due to different class loaders across the application
                assignment.setSlot((Slot) entity);
            } else if (entity instanceof Device) {
                assignment.setDevice((Device) entity);
            } else if (entity instanceof SlotGroup) {
                assignment.setSlotGroup((SlotGroup) entity);
            }
            assignment.setRequestor(currentUser);
            assignment.setPhaseGroup(checklist);
            List<ProcessStatus> statuses = new ArrayList<>();
            for (ChecklistField field : checklist.getPhases()) {
                ProcessStatus phaseStatus = new ProcessStatus();
                phaseStatus.setAssignment(assignment);
                phaseStatus.setGroupMember(field);
                phaseStatus.setStatus(field.getDefaultStatus());
                statuses.add(phaseStatus);
            }
            assignment.setStatuses(statuses);
            em.persist(assignment);
            LOGGER.log(Level.INFO, "Assigned checklist to {0}", entity.toString());
        }
    }

    /**
     * Assign the default checklist to given slots, devices, or groups
     * 
     * @param type
     * @param entities 
     */
    public void assignChecklist(ChecklistEntity type, List<?> entities) {
       Checklist checklist = findDefaultChecklist(type); 
       if (checklist == null) {
           throw new IllegalStateException("Checklists are missing or not configured properly. Inform Configuration Manager about this error.");
       }
       assignChecklist(entities, checklist);
    }
    
     /**
     * Assign checklist to a set of slots, devices or groups
     * 
     * @param <T>
     * @param type
     * @param entities
     * @param checklist 
     */
    public <T> void unassignChecklist(ChecklistEntity type, List<T> entities, Checklist checklist) {   
        LOGGER.log(Level.INFO, "Removing checklist for {0} entities", entities.size());
        // ToDo: check authorization
        List<Assignment> assignments = Collections.EMPTY_LIST;
        
        switch (type) {
            case SLOT:
                assignments = em.createNamedQuery("Assignment.findBySlotChecklist", Assignment.class)
                        .setParameter("slots", entities)
                        .setParameter("checklist", checklist)
                        .getResultList();
                break;
            case GROUP:
                assignments = em.createNamedQuery("Assignment.findByGroupChecklist", Assignment.class)
                        .setParameter("groups", entities)
                        .setParameter("checklist", checklist)
                        .getResultList();
                break;
            case DEVICE:
                assignments = em.createNamedQuery("Assignment.findByDeviceChecklist", Assignment.class)
                        .setParameter("devices", entities)
                        .setParameter("checklist", checklist)
                        .getResultList();
                break;
            default:
                LOGGER.log(Level.SEVERE, "Invalid CM entity type {0}", type);              
                break;
        }
        LOGGER.log(Level.SEVERE, "Removing {0} assignments", assignments.size());  
        for(Assignment assignment: assignments) {
            em.remove(assignment);
        }
    }
    
    /**
     * Unassign the default checklist from given slots, devices, or groups
     * 
     * @param type
     * @param entities 
     */
    public void unassignChecklist(ChecklistEntity type, List<?> entities) {
       Checklist checklist = findDefaultChecklist(type); 
       if (checklist == null) {
           throw new IllegalStateException("Checklists are missing or not configured properly. Inform Configuration Manager about this error.");
       }
       unassignChecklist(type, entities, checklist);
    }
    
    /**
     * delete a given process
     *
     * @param assignment
     */
    public void deleteAssignment(Assignment assignment) {
        Assignment src = em.find(Assignment.class, assignment.getId());
        em.remove(src);
    }

    /**
     * find a process given its id
     *
     * @param id
     * @return the process
     */
    public Assignment findRequirement(Long id) {
        return em.find(Assignment.class, id);
    }
    
    // -------------------- Approvals
    /**
     * All approvals
     * 
     * @return a list of all {@link PhaseApproval}s ordered by name.
     */
//    public List<PhaseApproval> findAllApprovals() {
//        return em.createNamedQuery("PhaseApproval.findAll", PhaseApproval.class).getResultList();
//    }   
    
    /**
     * All approvals
     * 
     * @param group
     * @return a list of all {@link PhaseApproval}s ordered by name.
     */
//    public List<PhaseApproval> findApprovals(Checklist group) {
//        return em.createNamedQuery("PhaseApproval.findByGroup", PhaseApproval.class).setParameter("group", group).getResultList();
//    } 
    
    /**
     * find a approval given its id
     *
     * @param id
     * @return the process
     */
//    public PhaseApproval findPhaseApproval(Long id) {
//        return em.find(PhaseApproval.class, id);
//    }
    
    /**
     * save a phase approval
     *
     * @param approval
     */
//    public void saveApproval(PhaseApproval approval) {
//        if (approval.getId() == null) {
//            em.persist(approval);
//        } else {
//            em.merge(approval);
//        }
//        LOGGER.log(Level.INFO, "phase approval  saved - {0}", approval.getId());
//    }
    
    /**
     * delete a given approval
     *
     * @param approval
     */
//    public void deleteApproval(PhaseApproval approval) {
//        PhaseApproval src = em.find(PhaseApproval.class, approval.getId());
//        em.remove(src);
//    }
    
//    // ------------------ Status type
//    /**
//     * All types
//     * 
//     * @return a list of all {@link Phase}s ordered by name.
//     */
//    public List<Checklist> findAllChecklists() {
//        return em.createNamedQuery("Checklist.findAll", Checklist.class).getResultList();
//    } 
//    
//    /**
//     * PHase type 
//     * 
//     * @param name
//     * @return a list of all {@link Phase}s ordered by name.
//     */
//    public Checklist findChecklist(String name) {
//        return em.createNamedQuery("Checklist.findByName", Checklist.class).setParameter("name", name).getSingleResult();
//    } 
//    
//    /**
//     * save a status type
//     *
//     * @param group type
//     */
//    public void saveChecklist(Checklist group) {
//        if (group.getId() == null) {
//            em.persist(group);
//        } else {
//            em.merge(group);
//        }
//        LOGGER.log(Level.FINE, "status type saved - {0}", group.getId());
//    }
//
//    /**
//     * delete a given process
//     *
//     * @param group
//     */
//    public void deleteChecklist(Checklist group) {
//        Checklist src = em.find(Checklist.class, group.getId());
//        em.remove(src);
//    }
//
//    /**
//     * find a status type given its id
//     *
//     * @param id
//     * @return the status type
//     */
//    public Checklist findChecklist(Long id) {
//        return em.find(Checklist.class, id);
//    }
    
    // ------------------ Slot Groups
    /**
     * All groups
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    public List<SlotGroup> findAllSlotGroups() {
        return em.createNamedQuery("SlotGroup.findAll", SlotGroup.class).getResultList();
    } 
    
    public List<SlotGroup> findUnassignedGroups() {
        return em.createNamedQuery("Assignment.findUnassignedGroups", SlotGroup.class).getResultList();
    }
    
    public List<Device> findUnassignedDevices() {
        return em.createNamedQuery("Assignment.findUnassignedDevices", Device.class).getResultList();
    }
    /**
     * PHase type 
     * 
     * @param name
     * @return a list of all {@link Process}s ordered by name.
     */
    public SlotGroup findSlotGroup(String name) {
        return em.createNamedQuery("SlotGroup.findByName", SlotGroup.class).setParameter("name", name).getSingleResult();
    } 
    
    /**
     * save a status type
     *
     * @param status type
     */
    public void saveSlotGroup(SlotGroup status) {
        if (status.getId() == null) {
            em.persist(status);
        } else {
            em.merge(status);
        }
        LOGGER.log(Level.FINE, "status type saved - {0}", status.getId());
    }

    /**
     * delete a given process
     *
     * @param status
     */
    public void deleteSlotGroup(SlotGroup status) {
        SlotGroup src = em.find(SlotGroup.class, status.getId());
        em.remove(src);
    }

    /**
     * find a status type given its id
     *
     * @param id
     * @return the status type
     */
    public SlotGroup findSlotGroup(Long id) {
        return em.find(SlotGroup.class, id);
    }
    
    // ------------------ Phase Groups
    /**
     * All groups
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    public List<Checklist> findAllChecklists() {
        return em.createNamedQuery("Checklist.findAll", Checklist.class).getResultList();
    } 
    
    /**
     * PHase type 
     * 
     * @param name
     * @return a list of all {@link Process}s ordered by name.
     */
    public Checklist findChecklist(String name) {
        List<Checklist> lists = em.createNamedQuery("Checklist.findByName", Checklist.class).setParameter("name", name).getResultList();
        if (lists == null || lists.isEmpty()) {
            return null;
        } else {
            return lists.get(0);
        }
    } 
    
    /**
     * save a status type
     *
     * @param group type
     */
    public void saveChecklist(Checklist group) {
        if (group.getId() == null) {
            em.persist(group);
        } else {
            em.merge(group);
        }
        LOGGER.log(Level.FINE, "status type saved - {0}", group.getId());
    }

    /**
     * delete a given process
     *
     * @param group
     */
    public void deleteChecklist(Checklist group) {
        Checklist src = em.find(Checklist.class, group.getId());
        em.remove(src);
    }

    /**
     * find a status type given its id
     *
     * @param id
     * @return the status type
     */
    public Checklist findChecklist(Long id) {
        return em.find(Checklist.class, id);
    }
    
    // ------------------------------------
    // ------------------ Phase Group Members
    /**
     * All groups
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    public List<ChecklistField> findAllChecklistFields() {
        return em.createNamedQuery("ChecklistField.findAll", ChecklistField.class).getResultList();
    } 
    
    /**
     * PHase type 
     * 
     * @param group
     * @return a list of all {@link Process}s ordered by name.
     */
    public ChecklistField findChecklistField(Checklist group) {
        return em.createNamedQuery("ChecklistField.findByGroup", ChecklistField.class).setParameter("group", group).getSingleResult();
    } 
    
    /**
     * save a status type
     *
     * @param group type
     */
    public void saveChecklistField(ChecklistField group) {
        if (group.getId() == null) {
            em.persist(group);
        } else {
            em.merge(group);
        }
        LOGGER.log(Level.FINE, "phase group member saved - {0}", group.getId());
    }

    /**
     * delete a given process
     *
     * @param group
     */
    public void deleteChecklistField(ChecklistField group) {
        ChecklistField src = em.find(ChecklistField.class, group.getId());
        em.remove(src);
    }

    /**
     * find a status type given its id
     *
     * @param id
     * @return the status type
     */
    public ChecklistField findChecklistField(Long id) {
        return em.find(ChecklistField.class, id);
    }
    // ------------------------------------
  
    
    /**
     * All status options
     * 
     * @return a list of all {@link StatusOption}s .
     */
    public List<StatusOption> findAllStatusOptions() {
        return em.createNamedQuery("StatusOption.findAll", StatusOption.class).getResultList();
    } 
    
    /**
     * All status options for a type
     * 
     * @param group given status type
     * @return a list of all {@link StatusOption}s.
     */
    public List<StatusOption> findStatusOptions(Checklist group) {
        return em.createNamedQuery("StatusOption.findByGroup", StatusOption.class)
                .setParameter("group", group)
                .getResultList();
    } 
    
     /**
     * save a status option
     *
     * @param option 
     */
    public void saveStatusOption(StatusOption option) {
        if (option.getId() == null) {
            em.persist(option);
        } else {
            em.merge(option);
        }
        LOGGER.log(Level.FINE, "Status Option saved - {0}", option.getId());
    }

    /**
     * delete a given process
     *
     * @param option
     */
    public void deleteStatusOption(StatusOption option) {
        StatusOption src = em.find(StatusOption.class, option.getId());
        em.remove(src);
    }

    /**
     * find a status type given its id
     *
     * @param id
     * @return the status type
     */
    public StatusOption findStatusOption(Long id) {
        return em.find(StatusOption.class, id);
    }
    //----------------- phase status
    /**
     * 
     * @return 
     */
    public List<ProcessStatus> findAllStatuses() {
        return em.createNamedQuery("ProcessStatus.findAll", ProcessStatus.class).getResultList();
    }  
    public List<ProcessStatus> findGroupStatus() {
        return em.createNamedQuery("ProcessStatus.findGroupStatus", ProcessStatus.class).getResultList();
    }
    public List<ProcessStatus> findSlotStatus() {
        return em.createNamedQuery("ProcessStatus.findSlotStatus", ProcessStatus.class).getResultList();
    }
    public List<ProcessStatus> findDeviceStatus() {
        return em.createNamedQuery("ProcessStatus.findDeviceStatus", ProcessStatus.class).getResultList();
    }
    
    public List<ProcessStatus> findAllValidStatuses() {
        return em.createNamedQuery("ProcessStatus.findValid", ProcessStatus.class).getResultList();
    } 
    /**
     * 
     * @param assignment
     * @return 
     */
    public List<ProcessStatus> findAllStatuses(Assignment assignment) {
        return em.createNamedQuery("ProcessStatus.findByAssignment", ProcessStatus.class)
                .setParameter("assignment", assignment)
                .getResultList();
    }
    
    public List<ProcessStatus> findAllStatuses(Checklist group) {
        return em.createNamedQuery("ProcessStatus.findByGroup", ProcessStatus.class)
                .setParameter("group", group)
                .getResultList();
    }
    
     /**
     * save a status type
     *
     * @param status type
     */
    public void saveProcessStatus(ProcessStatus status) {
        if (status.getId() == null) {
            em.persist(status);
        } else {
            em.merge(status);
        }
        // LOGGER.log(Level.FINE, "status  saved - {0}", status.getId());
    }
    
    /**
     * ToDo: Make it generic. not straightforward due to em.find() will have to use DAO<T>. 
     * 
     * @param status 
     */
//    public void refreshVersion(ProcessStatus status) {
//        if (status.getId() != null) {        
//            ProcessStatus current = em.find(ProcessStatus.class, status.getId());
//            status.updateVersion(current);
//        }
//        // LOGGER.log(Level.FINE, "version refreshed - {0}", status.getVersion());
//    }

    /**
     * Update the version of the object
     * 
     * @param <T>
     * @param entityClass
     * @param status 
     */
    public <T extends ConfigurationEntity> void refreshVersion(Class<T> entityClass, T status) {
        if (status.getId() != null) {        
            T current = em.find(entityClass, status.getId());
            status.updateVersion(current);
        }
        // LOGGER.log(Level.FINE, "version refreshed - {0}", status.getVersion());
    }
    
    /**
     * delete a given process
     *
     * @param status
     */
    public void deleteProcessStatus(ProcessStatus status) {
        ProcessStatus src = em.find(ProcessStatus.class, status.getId());
        em.remove(src);
    }

    /**
     * find a status type given its id
     *
     * @param id
     * @return the status type
     */
    public ProcessStatus findProcessStatus(Long id) {
        return em.find(ProcessStatus.class, id);
    }
    
}
