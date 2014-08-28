package org.openepics.discs.conf.ejb;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;

import org.openepics.discs.conf.auditlog.Audit;
import org.openepics.discs.conf.ent.EntityTypeOperation;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ent.SlotArtifact;
import org.openepics.discs.conf.ent.SlotPair;
import org.openepics.discs.conf.ent.SlotPropertyValue;
import org.openepics.discs.conf.ent.SlotRelation;
import org.openepics.discs.conf.ent.SlotRelationName;
import org.openepics.discs.conf.security.Authorized;
import org.openepics.discs.conf.util.CRUDOperation;

/**
 *
 * @author vuppala
 * @author Miroslav Pavleski <miroslav.pavleski@cosylab.com>
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 */
@Stateless public class SlotEJB {
    private static final Logger logger = Logger.getLogger(SlotEJB.class.getCanonicalName());
    @PersistenceContext private EntityManager em;
    @Inject private ConfigurationEntityUtility entityUtility;

    // ---------------- Layout Slot -------------------------

    public List<Slot> findLayoutSlot() {
        final CriteriaQuery<Slot> cq = em.getCriteriaBuilder().createQuery(Slot.class);
        cq.from(Slot.class);

        final List<Slot> slots = em.createQuery(cq).getResultList();
        logger.log(Level.FINE, "Number of installation slots: {0}", slots.size());

        return slots;
    }

    public Slot findSlotByName(String name) {
        Slot slot;
        try {
            slot = em.createNamedQuery("Slot.findByName", Slot.class).setParameter("name", name).getSingleResult();
        } catch (NoResultException e) {
            slot = null;
        }
        return slot;
    }

    public List<Slot> findSlotByNameContainingString(String namePart) {
        return em.createNamedQuery("Slot.findByNameContaining", Slot.class).setParameter("name", namePart).getResultList();
    }

    public SlotRelation findSlotRelationByName(SlotRelationName name) {
        SlotRelation slotRelation;
        try {
            slotRelation = em.createNamedQuery("SlotRelation.findByName", SlotRelation.class).setParameter("name", name).getSingleResult();
        } catch (NoResultException e) {
            slotRelation = null;
        }
        return slotRelation;
    }

    public Slot findLayoutSlot(Long id) {
        return em.find(Slot.class, id);
    }

    public List<SlotPair> findSlotPairsByParentChildRelation(String childName, String parentName, SlotRelationName relationName) {
        return em.createNamedQuery("SlotPair.findByParentChildRelation", SlotPair.class).setParameter("childName", childName).setParameter("parentName", parentName).setParameter("relationName", relationName).getResultList();
    }

    @CRUDOperation(operation=EntityTypeOperation.CREATE)
    @Audit
    @Authorized
    public void addSlot(Slot slot) {
        entityUtility.setModified(slot);
        em.persist(slot);
    }

    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void saveLayoutSlot(Slot slot) {
        entityUtility.setModified(slot);
        em.merge(slot);
    }

    /** Deletes the slot.
     * @param slot - the slot to delete.
     */
    @CRUDOperation(operation=EntityTypeOperation.DELETE)
    @Audit
    @Authorized
    public void deleteLayoutSlot(Slot slot) {
        final Slot slotToDelete = em.find(Slot.class, slot.getId());
        em.remove(slotToDelete);
    }


    // ------------------ Slot Property ---------------

    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void addSlotProperty(SlotPropertyValue propertyValue) {
        final Slot parent = propertyValue.getSlot();

        entityUtility.setModified(parent, propertyValue);

        parent.getSlotPropertyList().add(propertyValue);
        em.merge(parent);
    }

    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void saveSlotProp(SlotPropertyValue propertyValue) {
        final SlotPropertyValue mergedPropertyValue = em.merge(propertyValue);

        entityUtility.setModified(mergedPropertyValue.getSlot(), mergedPropertyValue);

        logger.log(Level.FINE, "Slot Property: id " + mergedPropertyValue.getId() + " name " + mergedPropertyValue.getProperty().getName());
    }

    /** Deletes the slot property value.
     * @param propertyValue - the slot property value to delete.
     */
    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void deleteSlotProp(SlotPropertyValue propertyValue) {
        logger.log(Level.FINE, "deleting slot property id " + propertyValue.getId() + " name " + propertyValue.getProperty().getName());

        final SlotPropertyValue propertyValueToDelete = em.find(SlotPropertyValue.class, propertyValue.getId());
        final Slot parent = propertyValueToDelete.getSlot();

        entityUtility.setModified(parent);

        parent.getSlotPropertyList().remove(propertyValueToDelete);
        em.remove(propertyValueToDelete);
    }


    // ---------------- Slot Artifact ---------------------
    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void addSlotArtifact(SlotArtifact artifact) {
        final Slot parent = artifact.getSlot();

        entityUtility.setModified(parent, artifact);

        parent.getSlotArtifactList().add(artifact);
        em.merge(parent);
    }

    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void saveSlotArtifact(SlotArtifact artifact) {
        final SlotArtifact mergedArtifact = em.merge(artifact);

        entityUtility.setModified(mergedArtifact.getSlot(), mergedArtifact);

        logger.log(Level.FINE, "Slot Artifact: name " + mergedArtifact.getName() + " description " + mergedArtifact.getDescription() + " uri " + mergedArtifact.getUri() + "is int " + mergedArtifact.isInternal());
    }

    /** Deletes the slot artifact.
     * @param artifact - the slot artifact to delete.
     */
    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void deleteSlotArtifact(SlotArtifact artifact) {
        final SlotArtifact artifactToDelete = em.find(SlotArtifact.class, artifact.getId());
        final Slot parent = artifactToDelete.getSlot();

        entityUtility.setModified(parent);

        parent.getSlotArtifactList().remove(artifactToDelete);
        em.remove(artifactToDelete);
    }


    // ---------------- Related Slots ---------------------
    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void addSlotPair(SlotPair slotPair) {
        em.persist(slotPair);
    }

    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void saveSlotPair(SlotPair slotPair) {
        em.merge(slotPair);

        logger.log(Level.FINE, "saved slot pair: child " + slotPair.getChildSlot().getName() + " parent " + slotPair.getParentSlot().getName() + " relation ");
    }

    /** Deletes the slot pair.
     * @param slotPair - the slot pair to delete.
     */
    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void deleteSlotPair(SlotPair slotPair) {
        final SlotPair slotPairToDelete = em.find(SlotPair.class, slotPair.getId());
        em.remove(slotPairToDelete);
    }


    private final String ROOT_COMPONENT_TYPE = "_ROOT"; // ToDo: Get the root
                                                        // type from
                                                        // configuration (JNDI,
                                                        // config table etc)

    public List<Slot> getRootNodes(SlotRelationName relationName) {
        return em.createQuery("SELECT cp.childSlot FROM SlotPair cp "
                + "WHERE cp.parentSlot.componentType.name = :ctype AND cp.slotRelation.name = :relname "
                + "ORDER BY cp.childSlot.name",
                Slot.class).
                setParameter("ctype", ROOT_COMPONENT_TYPE).
                setParameter("relname", relationName).getResultList();
    }

    public List<Slot> getRootNodes() {
        return getRootNodes(SlotRelationName.CONTAINS); // ToDo: get the relation name from
                                         // configuration
    }

    public List<Slot> relatedChildren(String compName) {
        return em.createQuery("SELECT cp.childSlot FROM SlotPair cp "
                + "WHERE cp.parentSlot.name = :compname AND cp.slotRelation.name = :relname", Slot.class).
                setParameter("compname", compName).
                setParameter("relname", SlotRelationName.CONTAINS).getResultList();
    }
}