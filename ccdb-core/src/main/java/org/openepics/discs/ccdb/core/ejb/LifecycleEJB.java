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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.discs.ccdb.model.ConfigurationEntity;
import org.openepics.discs.ccdb.model.Device;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.auth.User;
import org.openepics.discs.ccdb.model.cm.Phase;
import org.openepics.discs.ccdb.model.cm.PhaseApproval;
import org.openepics.discs.ccdb.model.cm.PhaseAssignment;
import org.openepics.discs.ccdb.model.cm.PhaseStatus;
import org.openepics.discs.ccdb.model.cm.PhaseGroup;
import org.openepics.discs.ccdb.model.cm.PhaseGroupMember;
import org.openepics.discs.ccdb.model.cm.SlotGroup;
import org.openepics.discs.ccdb.model.cm.StatusOption;

/**
 *
 *  Status of life cycle phases of slots or devices
 * 
 * @author vuppala
 *
 */
@Stateless
public class LifecycleEJB {    
    private static final Logger LOGGER = Logger.getLogger(LifecycleEJB.class.getName());   
    @PersistenceContext private EntityManager em;
    

    /**
     * All reviews
     * 
     * @return a list of all {@link Phase}s ordered by name.
     */
    public List<Phase> findAllPhases() {
        return em.createNamedQuery("Phase.findAll", Phase.class).getResultList();
    } 
    
     /**
     * Reviews with a given tag
     * 
     * @param group
     * 
     * @return a list of all {@link Phase}s ordered by name.
     */
    public List<Phase> findPhases(PhaseGroup group) {
        return em.createNamedQuery("PhaseGroupMember.findPhasesByGroup", Phase.class).setParameter("group", group).getResultList();
    }
    
    
    /**
     * save a process
     *
     * @param phase
     */
    public void savePhase(Phase phase) {
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
    public void deletePhase(Phase phase) {
        Phase src = em.find(Phase.class, phase.getId());
        em.remove(src);
    }

    /**
     * find a process given its id
     *
     * @param id
     * @return the process
     */
    public Phase findPhase(Long id) {
        return em.find(Phase.class, id);
    }
    
    // ----------------- Assignments 
    /**
     * All assignments
     * 
     * @return a list of all {@link Phase}s ordered by name.
     */
    public List<PhaseAssignment> findAllAssignments() {
        return em.createNamedQuery("PhaseAssignment.findAll", PhaseAssignment.class).getResultList();
    }
    
    public List<PhaseAssignment> findGroupAssignments() {
        return em.createNamedQuery("PhaseAssignment.findGroupAssignments", PhaseAssignment.class).getResultList();
    }
    
    public List<PhaseAssignment> findSlotAssignments() {
        return em.createNamedQuery("PhaseAssignment.findSlotAssignments", PhaseAssignment.class).getResultList();
    }
    
    public List<PhaseAssignment> findDeviceAssignments() {
        return em.createNamedQuery("PhaseAssignment.findDeviceAssignments", PhaseAssignment.class).getResultList();
    }
    
    /**
     * All assignments
     * 
     * @param type
     * @return a list of all {@link Phase}s ordered by name.
     */
    public List<PhaseAssignment> findAssignments(PhaseGroup type) {
        return em.createNamedQuery("PhaseAssignment.findByGroup", PhaseAssignment.class).setParameter("group", type).getResultList();
    }
    
    public PhaseAssignment findAssignment(SlotGroup group) {
        List<PhaseAssignment> result = em.createNamedQuery("PhaseAssignment.findBySlotGroup", PhaseAssignment.class).setParameter("group", group).getResultList();
        if (result == null || result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }
    
    public PhaseAssignment findAssignment(Slot slot) {
        List<PhaseAssignment> result = em.createNamedQuery("PhaseAssignment.findBySlot", PhaseAssignment.class).setParameter("slot", slot).getResultList();
        if (result == null || result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }
    
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
    public void saveAssignment(PhaseAssignment assignment) {
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
    private boolean isAnApprover(List<PhaseApproval> approvals, User approver) {
        if (approvals == null || approver == null ) {
            return false;
        }
        for (PhaseApproval approval : approvals) {
            if (approver.equals(approval.getAssignedApprover())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * ToDo: Improve the code with set operations.
     * 
     * @param assignment
     * @param approvers 
     */
    public void saveAssignment(PhaseAssignment assignment, List<User> approvers) {       
        List<PhaseStatus> statuses = new ArrayList<>();
        
        for(PhaseGroupMember pog : assignment.getPhaseGroup().getPhases()) {
            PhaseStatus phaseStatus = new PhaseStatus();
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
     * delete a given process
     *
     * @param assignment
     */
    public void deleteAssignment(PhaseAssignment assignment) {
        PhaseAssignment src = em.find(PhaseAssignment.class, assignment.getId());
        em.remove(src);
    }

    /**
     * find a process given its id
     *
     * @param id
     * @return the process
     */
    public PhaseAssignment findRequirement(Long id) {
        return em.find(PhaseAssignment.class, id);
    }
    
    // -------------------- Approvals
    /**
     * All approvals
     * 
     * @return a list of all {@link PhaseApproval}s ordered by name.
     */
    public List<PhaseApproval> findAllApprovals() {
        return em.createNamedQuery("PhaseApproval.findAll", PhaseApproval.class).getResultList();
    }   
    
    /**
     * All approvals
     * 
     * @param group
     * @return a list of all {@link PhaseApproval}s ordered by name.
     */
    public List<PhaseApproval> findApprovals(PhaseGroup group) {
        return em.createNamedQuery("PhaseApproval.findByGroup", PhaseApproval.class).setParameter("group", group).getResultList();
    } 
    
    /**
     * find a approval given its id
     *
     * @param id
     * @return the process
     */
    public PhaseApproval findPhaseApproval(Long id) {
        return em.find(PhaseApproval.class, id);
    }
    
    /**
     * save a phase approval
     *
     * @param approval
     */
    public void saveApproval(PhaseApproval approval) {
        if (approval.getId() == null) {
            em.persist(approval);
        } else {
            em.merge(approval);
        }
        LOGGER.log(Level.INFO, "phase approval  saved - {0}", approval.getId());
    }
    
    /**
     * delete a given approval
     *
     * @param approval
     */
    public void deleteApproval(PhaseApproval approval) {
        PhaseApproval src = em.find(PhaseApproval.class, approval.getId());
        em.remove(src);
    }
    
//    // ------------------ Status type
//    /**
//     * All types
//     * 
//     * @return a list of all {@link Phase}s ordered by name.
//     */
//    public List<PhaseGroup> findAllPhaseGroups() {
//        return em.createNamedQuery("PhaseGroup.findAll", PhaseGroup.class).getResultList();
//    } 
//    
//    /**
//     * PHase type 
//     * 
//     * @param name
//     * @return a list of all {@link Phase}s ordered by name.
//     */
//    public PhaseGroup findPhaseGroup(String name) {
//        return em.createNamedQuery("PhaseGroup.findByName", PhaseGroup.class).setParameter("name", name).getSingleResult();
//    } 
//    
//    /**
//     * save a status type
//     *
//     * @param group type
//     */
//    public void savePhaseGroup(PhaseGroup group) {
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
//    public void deletePhaseGroup(PhaseGroup group) {
//        PhaseGroup src = em.find(PhaseGroup.class, group.getId());
//        em.remove(src);
//    }
//
//    /**
//     * find a status type given its id
//     *
//     * @param id
//     * @return the status type
//     */
//    public PhaseGroup findPhaseGroup(Long id) {
//        return em.find(PhaseGroup.class, id);
//    }
    
    // ------------------ Slot Groups
    /**
     * All groups
     * 
     * @return a list of all {@link Phase}s ordered by name.
     */
    public List<SlotGroup> findAllSlotGroups() {
        return em.createNamedQuery("SlotGroup.findAll", SlotGroup.class).getResultList();
    } 
    
    public List<SlotGroup> findUnassignedGroups() {
        return em.createNamedQuery("PhaseAssignment.findUnassignedGroups", SlotGroup.class).getResultList();
    }
    
    public List<Slot> findUnassignedSlots() {
        return em.createNamedQuery("PhaseAssignment.findUnassignedSlots", Slot.class).getResultList();
    }
    
    public List<Device> findUnassignedDevices() {
        return em.createNamedQuery("PhaseAssignment.findUnassignedDevices", Device.class).getResultList();
    }
    /**
     * PHase type 
     * 
     * @param name
     * @return a list of all {@link Phase}s ordered by name.
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
     * @return a list of all {@link Phase}s ordered by name.
     */
    public List<PhaseGroup> findAllPhaseGroups() {
        return em.createNamedQuery("PhaseGroup.findAll", PhaseGroup.class).getResultList();
    } 
    
    /**
     * PHase type 
     * 
     * @param name
     * @return a list of all {@link Phase}s ordered by name.
     */
    public PhaseGroup findPhaseGroup(String name) {
        return em.createNamedQuery("PhaseGroup.findByName", PhaseGroup.class).setParameter("name", name).getSingleResult();
    } 
    
    /**
     * save a status type
     *
     * @param group type
     */
    public void savePhaseGroup(PhaseGroup group) {
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
    public void deletePhaseGroup(PhaseGroup group) {
        PhaseGroup src = em.find(PhaseGroup.class, group.getId());
        em.remove(src);
    }

    /**
     * find a status type given its id
     *
     * @param id
     * @return the status type
     */
    public PhaseGroup findPhaseGroup(Long id) {
        return em.find(PhaseGroup.class, id);
    }
    
    // ------------------------------------
    // ------------------ Phase Group Members
    /**
     * All groups
     * 
     * @return a list of all {@link Phase}s ordered by name.
     */
    public List<PhaseGroupMember> findAllPhaseGroupMembers() {
        return em.createNamedQuery("PhaseGroupMember.findAll", PhaseGroupMember.class).getResultList();
    } 
    
    /**
     * PHase type 
     * 
     * @param group
     * @return a list of all {@link Phase}s ordered by name.
     */
    public PhaseGroupMember findPhaseGroupMember(PhaseGroup group) {
        return em.createNamedQuery("PhaseGroupMember.findByGroup", PhaseGroupMember.class).setParameter("group", group).getSingleResult();
    } 
    
    /**
     * save a status type
     *
     * @param group type
     */
    public void savePhaseGroupMember(PhaseGroupMember group) {
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
    public void deletePhaseGroupMember(PhaseGroupMember group) {
        PhaseGroupMember src = em.find(PhaseGroupMember.class, group.getId());
        em.remove(src);
    }

    /**
     * find a status type given its id
     *
     * @param id
     * @return the status type
     */
    public PhaseGroupMember findPhaseGroupMember(Long id) {
        return em.find(PhaseGroupMember.class, id);
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
    public List<StatusOption> findStatusOptions(PhaseGroup group) {
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
    public List<PhaseStatus> findAllStatuses() {
        return em.createNamedQuery("PhaseStatus.findAll", PhaseStatus.class).getResultList();
    }  
    public List<PhaseStatus> findGroupStatus() {
        return em.createNamedQuery("PhaseStatus.findGroupStatus", PhaseStatus.class).getResultList();
    }
    public List<PhaseStatus> findSlotStatus() {
        return em.createNamedQuery("PhaseStatus.findSlotStatus", PhaseStatus.class).getResultList();
    }
    public List<PhaseStatus> findDeviceStatus() {
        return em.createNamedQuery("PhaseStatus.findDeviceStatus", PhaseStatus.class).getResultList();
    }
    
    public List<PhaseStatus> findAllValidStatuses() {
        return em.createNamedQuery("PhaseStatus.findValid", PhaseStatus.class).getResultList();
    } 
    /**
     * 
     * @param assignment
     * @return 
     */
    public List<PhaseStatus> findAllStatuses(PhaseAssignment assignment) {
        return em.createNamedQuery("PhaseStatus.findByAssignment", PhaseStatus.class)
                .setParameter("assignment", assignment)
                .getResultList();
    }
    
    public List<PhaseStatus> findAllStatuses(PhaseGroup group) {
        return em.createNamedQuery("PhaseStatus.findByGroup", PhaseStatus.class)
                .setParameter("group", group)
                .getResultList();
    }
    
     /**
     * save a status type
     *
     * @param status type
     */
    public void savePhaseStatus(PhaseStatus status) {
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
//    public void refreshVersion(PhaseStatus status) {
//        if (status.getId() != null) {        
//            PhaseStatus current = em.find(PhaseStatus.class, status.getId());
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
    public void deletePhaseStatus(PhaseStatus status) {
        PhaseStatus src = em.find(PhaseStatus.class, status.getId());
        em.remove(src);
    }

    /**
     * find a status type given its id
     *
     * @param id
     * @return the status type
     */
    public PhaseStatus findPhaseStatus(Long id) {
        return em.find(PhaseStatus.class, id);
    }
    
}
