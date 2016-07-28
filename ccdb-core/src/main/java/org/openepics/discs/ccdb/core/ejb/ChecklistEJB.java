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
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.discs.ccdb.core.auth.LocalAuthEJB;
import org.openepics.discs.ccdb.model.ConfigurationEntity;
import org.openepics.discs.ccdb.model.Device;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.auth.AuthUser;
import org.openepics.discs.ccdb.model.cl.Approval;
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
//    @Inject
//    private SecurityPolicy securityPolicy;

    @EJB
    private LocalAuthEJB authEJB;
    
    private static final Logger LOGGER = Logger.getLogger(ChecklistEJB.class.getName());   
    @PersistenceContext private EntityManager em;
    
    
    /**
     * All reviews
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Process> findAllPhases() {
        return em.createNamedQuery("Process.findAll", Process.class).getResultList();
    } 
    
     /**
     * Reviews with a given tag
     * 
     * @param checklist
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Process> findPhases(Checklist checklist) {
        return em.createNamedQuery("ChecklistField.findPhasesByChecklist", Process.class).setParameter("checklist", checklist).getResultList();
    }
    
    /**
     * Reviews with a given tag
     * 
     * @param checklist
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Process> findOptionalFields(Checklist checklist) {
        return em.createNamedQuery("ChecklistField.findOptionalFields", Process.class).setParameter("checklist", checklist).getResultList();
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
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public Process findPhase(Long id) {
        return em.find(Process.class, id);
    }
    
    /**
     * find a process given its id
     *
     * @param name
     * @return the process
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public Process findPhaseByName(String name) {
        List<Process> processes = em.createNamedQuery("Process.findByName", Process.class).setParameter("name", name).getResultList();
        if (processes == null || processes.isEmpty()) {
            return null;
        }
        return processes.get(0);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public Approval findApproval(Process process, Slot slot) {
        List<Approval> approvals = em.createNamedQuery("Approval.findBySlotProc", Approval.class).setParameter("slot", slot).setParameter("process", process).getResultList();
        if (approvals == null || approvals.isEmpty()) {
            return null;
        }
        return approvals.get(0);
    }
    // ----------------- Assignments 
    /**
     * All assignments
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Assignment> findGroupAssignments() {
        return em.createNamedQuery("Assignment.findGroupAssignments", Assignment.class).getResultList();
    }
    
    /**
     * All slot assignments
     * 
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Assignment> findSlotAssignments() {
        return em.createNamedQuery("Assignment.findSlotAssignments", Assignment.class).getResultList();
    }
    
    /**
     * All device assignments
     * 
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Assignment> findDeviceAssignments() {
        return em.createNamedQuery("Assignment.findDeviceAssignments", Assignment.class).getResultList();
    }
    
    /**
     * All assignments of a device
     * 
     * @param device
     * @return list of checklist assignments
     */ 
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Assignment> findAssignments(Device device) {
        return em.createNamedQuery("Assignment.findByDevice", Assignment.class).setParameter("device", device).getResultList();      
    }
    
    /**
     * All assignments of a group
     * 
     * @param group
     * @return list of checklist assignments
     */  
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Assignment> findAssignments(SlotGroup group) {
        return em.createNamedQuery("Assignment.findBySlotGroup", Assignment.class).setParameter("group", group).getResultList();      
    }

    /**
     * Find all assignments of a slot
     * 
     * @param slot
     * @return list of checklist assignments
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Assignment> findAssignments(Slot slot) {
        return em.createNamedQuery("Assignment.findBySlot", Assignment.class).setParameter("slot", slot).getResultList();        
    }
    
    /**
     * ToDo: either make a named query or move to another module
     * 
     * @param groups
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Slot> findSlots(Set<SlotGroup> groups) {
        return em.createQuery("SELECT s FROM Slot s where s.cmGroup IN :groups", Slot.class).setParameter("groups", groups).getResultList();
    }
    
   
    /**
     * save the given  assignment
     *
     * @param assignment
     */
//    public void saveAssignment(Assignment assignment) {
//        if (assignment.getId() == null) {
//            em.persist(assignment);
//        } else {
//            em.merge(assignment);
//        }
//        LOGGER.log(Level.FINE, "phase assignment  saved - {0}", assignment.getId());
//    }

    /**
     * List of assigned slots whose name begin with a given prefix
     * ToDo: Temporary. refactor.
     * 
     * @param prefix
     * @return 
     */
//    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
//    public List<Assignment> assignedSlots(String prefix) {
//        return em.createQuery("SELECT a FROM Assignment a WHERE a.slot.name LIKE :prefix", Assignment.class)
//                .setParameter("prefix", prefix)
//                .getResultList();
//    }
    
    /**
     * Number of assigned slots whose names begin with a prefix
     * ToDo: Remove prefix and use containment relationship
     * ToDo: appears to be a EclipseLink bug on how the query is translated
     * 
     * @param prefix
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public Long numberOfAssignedSlots(String prefix) {
        return em.createQuery("SELECT COUNT(DISTINCT s.id) FROM Slot s, Assignment a WHERE s.name LIKE :prefix AND (a.slot = s OR a.slotGroup = s.cmGroup)", Long.class)
                .setParameter("prefix", prefix)
                .getSingleResult();
    }
    
    /**
     * Number of approved slots whose names begin with a prefix
     * ToDo: Remove prefix and use containment relationship
     * 
     * @param prefix
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
     public Long numberOfApprovedSlots(String prefix) {
        return em.createQuery("SELECT COUNT(DISTINCT s.id) FROM Slot s, Assignment a JOIN a.statuses p WHERE s.name LIKE :prefix AND (a.slot = s OR a.slotGroup = s.cmGroup) AND p.field.summaryProcess = TRUE AND p.status.completed = TRUE" , Long.class)
                .setParameter("prefix", prefix)
                .getSingleResult();
    }
    
    /**
     * List of sub slots whose name begin with a given prefix
     * ToDo: Temporary. refactor.
     * 
     * @param prefix
     * @return 
     */
     @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public Long  numberOfHostingSlots(String prefix) {
        return em.createQuery("SELECT COUNT(a) FROM Slot a WHERE a.name LIKE :prefix AND a.isHostingSlot = TRUE", Long.class)
                .setParameter("prefix", prefix)
                .getSingleResult();
    }
    
    /**
     * ToDo: Improve the code with set operations.
     * 
     * @param assignment
     * @param approvers 
     */
//    public void saveAssignment(Assignment assignment, List<User> approvers) {       
//        List<ProcessStatus> statuses = new ArrayList<>();
//        
//        for(ChecklistField field : assignment.getChecklist().getFields()) {
//            ProcessStatus phaseStatus = new ProcessStatus();
//            phaseStatus.setAssignment(assignment);
//            phaseStatus.setField(field);
//            phaseStatus.setStatus(field.getDefaultStatus());
//            // em.persist(phaseStatus);
//            statuses.add(phaseStatus);
//        }
//
//        assignment.setStatuses(statuses);
//        if (assignment.getId() == null) {
//            em.persist(assignment);
//        } else {
//            em.merge(assignment);
//        }
//    }
    
    /**
     * Default process list for the given entity type
     * 
     * @param type
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
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
     * Installation checklist: the default checklist for devices
     * 
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public Checklist findInstalaltionChecklist() {
        return findDefaultChecklist(ChecklistEntity.DEVICE);
    }
    
    /**
     * Hazard Review  checklist: the default checklist for slots or groups
     * 
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public Checklist findReviewChecklist() {
        return findDefaultChecklist(ChecklistEntity.SLOT);
    }
    
    /**
     * Find slots that do not belong to a group and are not internal system slots
     * 
     * @param allSlots find only device-hosting slots
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Slot> findUngroupedSlots(boolean allSlots) {
        if (allSlots) {
            return em.createNamedQuery("Slot.findUngroupedSlots").getResultList();
        }
        return em.createNamedQuery("Slot.findUngroupedNonContainerSlots").getResultList();       
    }
    
    /**
     * Does none of the given slots have assignments?
     * 
     * @param type
     * @param entities
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
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
     * Do the given slots have any assignments?
     * 
     * @param slots
     * @return true if they do. 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public Boolean haveAssignments(List<Slot> slots) {
        return 0 != em.createNamedQuery("Assignment.numberOfAssignedSlots", Long.class).setParameter("slots", slots).getSingleResult();
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
        AuthUser currentUser = authEJB.getCurrentUser();
        
        if (currentUser == null) {
            throw new SecurityException();
        }
                
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
            assignment.setChecklist(checklist);
            List<ProcessStatus> statuses = new ArrayList<>();
            for (ChecklistField field : checklist.getFields()) {
                ProcessStatus phaseStatus = new ProcessStatus();
                phaseStatus.setAssignment(assignment);
                phaseStatus.setField(field);
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
     * set approvals
     * ToDo: really inefficient. temporary. improve.
     * 
     * @param process
     * @param slots
     * @param approved 
     */
    public void setApprovals(Process process, List<Slot> slots, Boolean approved) {
        List<Approval> approvals = em.createNamedQuery("Approval.findBySlots", Approval.class)
                .setParameter("slots", slots)
                .setParameter("process", process).
                getResultList();
        boolean newApproval;
        
        for (Slot slot : slots) {
            newApproval = true;
            for (Approval approval : approvals) { // ToDo: not the right way
                if (slot.equals(approval.getSlot())) {
                    approval.setApproved(approved);
                    newApproval = false;
                    break;
                }
            }
            if (newApproval) {
                Approval approval = new Approval();
                approval.setSlot(slot);
                approval.setProcess(process);
                approval.setApproved(approved);
                em.persist(approval);
            }
        }
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
    
    
    // ------------------ Slot Groups
    /**
     * All groups
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<SlotGroup> findAllSlotGroups() {
        return em.createNamedQuery("SlotGroup.findAll", SlotGroup.class).getResultList();
    } 
    
    /**
     * Find a slot group 
     * 
     * @param name
     * @return a list of all {@link Process}s ordered by name.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public SlotGroup findSlotGroup(String name) {
        return em.createNamedQuery("SlotGroup.findByName", SlotGroup.class).setParameter("name", name).getSingleResult();
    } 
    
    /**
     * Assign the given group to a set of slots
     * 
     * 
     * @param slots
     * @param group 
     */
    public void assignGroup(List<Slot> slots, SlotGroup group) {
//        em.createNamedQuery("Slot.updateGroup")
//                .setParameter("group", group)
//                .setParameter("slots", slots)
//                .executeUpdate();
        for(Slot slot: slots) {
            slot.setCmGroup(group);
            em.merge(slot);
        }
    }
    
     /**
     * Remove group from a set of slots
     * 
     * @param slots
     */
    public void unassignGroup(List<Slot> slots) {
        assignGroup(slots, null);
    }
    
    /**
     * save a group
     *
     * @param status type
     */
    public void saveSlotGroup(SlotGroup status) {
        if (status.getId() == null) {
            em.persist(status);
        } else {
            em.merge(status);
        }
        LOGGER.log(Level.FINE, "group saved - {0}", status.getId());
    }

    /**
     * delete a given group
     *
     * @param status
     */
    public void deleteSlotGroup(SlotGroup status) {
        SlotGroup src = em.find(SlotGroup.class, status.getId());
        em.remove(src);
    }

    /**
     * find a group given its id
     *
     * @param id
     * @return the status type
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public SlotGroup findSlotGroup(Long id) {
        return em.find(SlotGroup.class, id);
    }
    
    // ------------------ Checklists
    /**
     * All groups
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<Checklist> findAllChecklists() {
        return em.createNamedQuery("Checklist.findAll", Checklist.class).getResultList();
    } 
    
    /**
     * find checklist by name 
     * 
     * @param name
     * @return a  {@link Checklist}
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
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
     * find a checklist given its id
     *
     * @param id
     * @return the status type
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public Checklist findChecklist(Long id) {
        return em.find(Checklist.class, id);
    }
    
    // ------------------------------------
    // ------------------ Phase Group Members
    /**
     * All fields
     * 
     * @return a list of all {@link Process}s ordered by name.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<ChecklistField> findAllChecklistFields() {
        return em.createNamedQuery("ChecklistField.findAll", ChecklistField.class).getResultList();
    } 
    
    /**
     * fields of the given checklist 
     * 
     * @param checklist
     * @return a list of all {@link Process}s ordered by name.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public ChecklistField findChecklistField(Checklist checklist) {
        return em.createNamedQuery("ChecklistField.findByChecklist", ChecklistField.class).setParameter("checklist", checklist).getSingleResult();
    } 
    
    /**
     * save a checklist field
     *
     * @param group type
     */
    public void saveChecklistField(ChecklistField group) {
        if (group.getId() == null) {
            em.persist(group);
        } else {
            em.merge(group);
        }
        LOGGER.log(Level.FINE, "checklist field saved - {0}", group.getId());
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
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public ChecklistField findChecklistField(Long id) {
        return em.find(ChecklistField.class, id);
    }
    // ------------------------------------
  
    
    /**
     * All status options
     * 
     * @return a list of all {@link StatusOption}s .
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<StatusOption> findAllStatusOptions() {
        return em.createNamedQuery("StatusOption.findAll", StatusOption.class).getResultList();
    } 
    
    /**
     * All status options for a checklist
     * 
     * @param checklist given status type
     * @return a list of all {@link StatusOption}s.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<StatusOption> findStatusOptions(Checklist checklist) {
        return em.createNamedQuery("StatusOption.findByChecklist", StatusOption.class)
                .setParameter("checklist", checklist)
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
     * delete a given status option
     *
     * @param option
     */
    public void deleteStatusOption(StatusOption option) {
        StatusOption src = em.find(StatusOption.class, option.getId());
        em.remove(src);
    }

    /**
     * find a status option given its id
     *
     * @param id
     * @return the status type
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public StatusOption findStatusOption(Long id) {
        return em.find(StatusOption.class, id);
    }
    
    
    //----------------- process status
    /**
     * All statuses
     *
     * 
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<ProcessStatus> findAllStatuses() {
        return em.createNamedQuery("ProcessStatus.findAll", ProcessStatus.class).getResultList();
    }  
    
    
//    public List<ProcessStatus> findGroupStatus() {
//        return em.createNamedQuery("ProcessStatus.findGroupStatus", ProcessStatus.class).getResultList();
//    }
//    public List<ProcessStatus> findSlotStatus() {
//        return em.createNamedQuery("ProcessStatus.findSlotStatus", ProcessStatus.class).getResultList();
//    }
//    public List<ProcessStatus> findDeviceStatus() {
//        return em.createNamedQuery("ProcessStatus.findDeviceStatus", ProcessStatus.class).getResultList();
//    }
//    
//    public List<ProcessStatus> findAllValidStatuses() {
//        return em.createNamedQuery("ProcessStatus.findValid", ProcessStatus.class).getResultList();
//    } 
    
    
    /**
     * All status records for the given assignment
     * 
     * @param assignment
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<ProcessStatus> findAllStatuses(Assignment assignment) {
        return em.createNamedQuery("ProcessStatus.findByAssignment", ProcessStatus.class)
                .setParameter("assignment", assignment)
                .getResultList();
    }
    
    /**
     * All status records for the give checklist
     * 
     * @param checklist
     * @return 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // read-only transaction
    public List<ProcessStatus> findAllStatuses(Checklist checklist) {
        return em.createNamedQuery("ProcessStatus.findByChecklist", ProcessStatus.class)
                .setParameter("checklist", checklist)
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
     * Sync the version of an entity
     * 
     * @param <T>
     * @param entityClass
     * @param entity 
     */
    public <T extends ConfigurationEntity> void refreshVersion(Class<T> entityClass, T entity) {
        if (entity.getId() != null) {        
            T current = em.find(entityClass, entity.getId());
            entity.updateVersion(current);
        }
        // LOGGER.log(Level.FINE, "version refreshed - {0}", status.getVersion());
    }
    
    /**
     * Update the version of a set of  entities
     * 
     * @param <T>
     * @param entityClass
     * @param entities
     */
    public <T extends ConfigurationEntity> void refreshVersion(Class<T> entityClass, List<T> entities) {
        for(T entity: entities) {
            refreshVersion(entityClass, entity);
        }
    }
    
    /**
     * delete a given process
     *
     * @param status
     */
//    public void deleteProcessStatus(ProcessStatus status) {
//        ProcessStatus src = em.find(ProcessStatus.class, status.getId());
//        em.remove(src);
//    }

    /**
     * find a process status given its id
     *
     * @param id
     * @return the status type
     */
//    public ProcessStatus findProcessStatus(Long id) {
//        return em.find(ProcessStatus.class, id);
//    }
    
}
