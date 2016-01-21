/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 *
 * Controls Configuration Database is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.conf.dl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ApplyScriptAfter;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openepics.discs.conf.dl.common.DataLoaderResult;
import org.openepics.discs.conf.dl.common.ErrorMessage;
import org.openepics.discs.conf.dl.common.ValidationMessage;
import org.openepics.discs.conf.ejb.InstallationEJB;
import org.openepics.discs.conf.ejb.SlotEJB;
import org.openepics.discs.conf.ent.InstallationRecord;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ent.SlotPair;
import org.openepics.discs.conf.ent.SlotPropertyValue;
import org.openepics.discs.conf.ent.SlotRelationName;
import org.openepics.discs.conf.testutil.SlotsDataLoaderHelper;
import org.openepics.discs.conf.testutil.TestUtility;

/**
 * Integration tests for {@link SlotsDataLoader}
 *
 * @author <a href="mailto:andraz.pozar@cosylab.com">Andraž Požar</a>
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
@RunWith(Arquillian.class)
@UsingDataSet(value= {"unit.xml", "property.xml", "basic_component_types.xml", "component_type.xml", "basic_slot.xml",
        "basic_comptype_property_value.xml", "device.xml"})
@ApplyScriptAfter(value= "truncate_database.sql")
public class SlotsDataLoaderIT {

    @Inject private SlotsDataLoaderHelper dataLoaderHelper;
    @Inject private SlotEJB slotEJB;
    @Inject private InstallationEJB installationEJB;
    @Inject private TestUtility testUtility;

    @Deployment
    public static WebArchive createDeployment() {
        return TestUtility.createWebArchive();
    }

    @Before
    public void setUpBeforeTest() {
        testUtility.loginForTests();
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void slotsImportSuccess() throws IOException {
        final String slotsImportFileName = "slots-import-creation.xlsx";
        final DataLoaderResult loaderResult = dataLoaderHelper.importSlots(slotsImportFileName);
        Assert.assertFalse("Errors: " + loaderResult.toString(), loaderResult.isError());
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void slotsImportCreationFails() throws IOException {
        final String slotsImportFileName = "slots-import-creation-fails.xlsx";

        final List<ValidationMessage> expectedValidationMessages = new ArrayList<>();
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 23,
                                                            SlotsDataLoader.HDR_ENTITY_TYPE, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 24,
                                                            SlotsDataLoader.HDR_ENTITY_NAME, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 25,
                                                            SlotsDataLoader.HDR_ENTITY_DESCRIPTION, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 26,
                                                            SlotsDataLoader.HDR_ENTITY_DEVICE_TYPE, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, 27,
                                                            SlotsDataLoader.HDR_ENTITY_DEVICE_TYPE, "NOTHING"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 28,
                                                            SlotsDataLoader.HDR_ENTITY_PARENT, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.NAME_ALREADY_EXISTS, 29,
                                                            SlotsDataLoader.HDR_ENTITY_NAME, "Slot1"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.INSTALL_CANT_CONTAIN_CONTAINER, 30,
                                                            null, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.PROPERTY_NOT_FOUND, 31,
                                                            SlotsDataLoader.HDR_PROP_NAME, "DOC01"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 32,
                                                            SlotsDataLoader.HDR_PROP_VALUE, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 33,
                                                            SlotsDataLoader.HDR_PROP_NAME, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.CREATE_VALUE_EXISTS, 34,
                                                            SlotsDataLoader.HDR_PROP_NAME, "ALIAS"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.CREATE_VALUE_EXISTS, 35,
                                                            SlotsDataLoader.HDR_PROP_NAME, "FIELDPOLY"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.CONVERSION_ERROR, 36,
                                                            SlotsDataLoader.HDR_PROP_VALUE, "Fail"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.PROPERTY_NOT_FOUND, 37,
                                                            SlotsDataLoader.HDR_PROP_NAME, "ACENPOS"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, 38,
                                                            SlotsDataLoader.HDR_PROP_NAME, "NOT_THERE"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.CONVERSION_ERROR, 39,
                                                            SlotsDataLoader.HDR_PROP_VALUE, "Fail"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 40,
                                                            SlotsDataLoader.HDR_RELATION_TYPE, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 41,
                                                            SlotsDataLoader.HDR_RELATION_ENTITY_NAME, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.INSTALL_CANT_CONTAIN_CONTAINER, 42,
                                                            SlotsDataLoader.HDR_RELATION_ENTITY_NAME, "IMPORT_TEST_2"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.CONTROL_RELATIONSHIP_RESTRICTIONS, 43,
                                                            SlotsDataLoader.HDR_RELATION_TYPE, "CONTROLS"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.POWER_RELATIONSHIP_RESTRICTIONS, 44,
                                                            SlotsDataLoader.HDR_RELATION_TYPE, "POWERS"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.SAME_CHILD_AND_PARENT, 45,
                                                            SlotsDataLoader.HDR_RELATION_ENTITY_NAME, "IMPORT_TEST_2"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 46,
                                                            SlotsDataLoader.HDR_ENTITY_PARENT, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, 47,
                                                            SlotsDataLoader.HDR_ENTITY_PARENT, "NON_EXISTING"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.AMBIGUOUS_PARENT_SLOT, 48,
                                                            SlotsDataLoader.HDR_ENTITY_PARENT, "IMPORT_TEST_4"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, 49,
                                                            SlotsDataLoader.HDR_ENTITY_PARENT, "IMPORT_TEST_5"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 50,
                                                            SlotsDataLoader.HDR_INSTALLATION, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.INSTALLATION_EXISTING, 51,
                                                            SlotsDataLoader.HDR_INSTALLATION, "BPM1-02"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, 52,
                                                            SlotsDataLoader.HDR_INSTALLATION, "BPM1-03"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.DEVICE_TYPE_ERROR, 53,
                                                            SlotsDataLoader.HDR_INSTALLATION, "BPM2-01"));

        final DataLoaderResult loaderResult = dataLoaderHelper.importSlots(slotsImportFileName);
        Assert.assertEquals("Error:\n" + loaderResult.toString(), expectedValidationMessages, loaderResult.getMessages());
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void slotsImportUpdateSuccess() throws IOException {
        final String slotsImportFileName = "slots-import-update.xlsx";
        final DataLoaderResult loaderResult = dataLoaderHelper.importSlots(slotsImportFileName);

        Assert.assertFalse("Errors: " + loaderResult.toString(), loaderResult.isError());

        final Slot slot2 = slotEJB.findByName("Slot2");
        Assert.assertEquals("BPM2", slot2.getComponentType().getName());
        Assert.assertEquals("Slot 2 New Desc", slot2.getDescription());
        final Slot afterParent = slot2.getPairsInWhichThisSlotIsAChildList().get(0).getParentSlot();
        Assert.assertEquals("IMPORT_TEST_2", afterParent.getName());

        final Slot slot1 = slotEJB.findByName("Slot1");
        SlotPropertyValue propVal = null;
        for (final SlotPropertyValue pv : slot1.getSlotPropertyList()) {
            if ("FIELDPOLY".equals(pv.getProperty().getName())) {
                propVal = pv;
                break;
            }
        }
        Assert.assertNotNull("Property value FIELDPOLY not found.", propVal);
        Assert.assertEquals("NewValue", propVal.getPropValue().toString());
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void slotsImportUpdateFails() throws IOException {
        final String slotsImportFileName = "slots-import-update-fails.xlsx";

        final List<ValidationMessage> expectedValidationMessages = new ArrayList<>();
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ORPHAN_SLOT, 23,
                                                            SlotsDataLoader.HDR_ENTITY_NAME, "Slot2"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.INSTALLATION_EXISTING, 24,
                                                            SlotsDataLoader.HDR_ENTITY_DEVICE_TYPE, "BPM2"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, 25,
                                                            SlotsDataLoader.HDR_ENTITY_NAME, "Slot3"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.VALUE_NOT_IN_DATABASE, 26,
                                                            SlotsDataLoader.HDR_ENTITY_TYPE, "SLOT"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, 27,
                                                            SlotsDataLoader.HDR_ENTITY_DEVICE_TYPE, "NON_EXISTING"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.AMBIGUOUS_PARENT_SLOT, 28,
                                                            SlotsDataLoader.HDR_ENTITY_PARENT, "IMPORT_TEST_4"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.PROPERTY_NOT_FOUND, 29,
                                                            SlotsDataLoader.HDR_PROP_NAME, "DOC01"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.CONVERSION_ERROR, 30,
                                                            SlotsDataLoader.HDR_PROP_VALUE, "Fail"));

        final DataLoaderResult loaderResult = dataLoaderHelper.importSlots(slotsImportFileName);
        Assert.assertEquals("Error:\n" + loaderResult.toString(), expectedValidationMessages, loaderResult.getMessages());
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void slotsImportDeleteSuccess() throws IOException {
        final String slotsImportFileName = "slots-import-delete.xlsx";
        final DataLoaderResult loaderResult = dataLoaderHelper.importSlots(slotsImportFileName);

        Assert.assertFalse("Errors: " + loaderResult.toString(), loaderResult.isError());

        final Slot cntnr3 = slotEJB.findByName("IMPORT_TEST_3");
        Assert.assertTrue("IMPORT_TEST_3 -> IMPORT_TEST_4 not deleted",
                cntnr3.getPairsInWhichThisSlotIsAParentList().isEmpty());

        final Slot slot1 = slotEJB.findByName("Slot1");
        SlotPropertyValue propVal = null;
        for (final SlotPropertyValue pv : slot1.getSlotPropertyList()) {
            if ("FIELDPOLY".equals(pv.getProperty().getName())) {
                propVal = pv;
                break;
            }
        }
        Assert.assertNotNull("Property value FIELDPOLY not found.", propVal);
        Assert.assertNull("Property FIELDPOLY still has a value", propVal.getPropValue());

        for (final SlotPair slotPair : slot1.getPairsInWhichThisSlotIsAParentList()) {
            if (slotPair.getSlotRelation().getName() == SlotRelationName.POWERS) {
                Assert.fail("POWERS relationship not deleted.");
            }
        }

        final Slot cntnr0 = slotEJB.findByName("IMPORT_TEST");
        Assert.assertTrue("IMPORT_TEST still has properties", cntnr0.getSlotPropertyList().isEmpty());

        final InstallationRecord record = installationEJB.getLastInstallationRecordForSlot(slot1);
        Assert.assertNotNull(record);
        Assert.assertNotNull(record.getUninstallDate());
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void slotsImportDeleteFails() throws IOException {
        final String slotsImportFileName = "slots-import-delete-fails.xlsx";

        final List<ValidationMessage> expectedValidationMessages = new ArrayList<>();
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, 23,
                                                            SlotsDataLoader.HDR_ENTITY_NAME, "IMPORT_TEST_4"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ORPHAN_CREATED, 24, null, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 25,
                                                            SlotsDataLoader.HDR_PROP_NAME, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 26,
                                                            SlotsDataLoader.HDR_RELATION_TYPE, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 27,
                                                            SlotsDataLoader.HDR_RELATION_ENTITY_NAME, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ORPHAN_CREATED, 28,
                                                            SlotsDataLoader.HDR_RELATION_ENTITY_NAME, "IMPORT_TEST_3"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, 29,
                                                            SlotsDataLoader.HDR_RELATION_ENTITY_NAME, "IMPORT_TEST"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, 30,
                                                            SlotsDataLoader.HDR_INSTALLATION, null));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, 31,
                                                            SlotsDataLoader.HDR_INSTALLATION, "BPM1-01"));
        expectedValidationMessages.add(new ValidationMessage(ErrorMessage.VALUE_NOT_IN_DATABASE, 32,
                                                            SlotsDataLoader.HDR_INSTALLATION, "BPM1-02"));

        final DataLoaderResult loaderResult = dataLoaderHelper.importSlots(slotsImportFileName);
        Assert.assertEquals("Error:\n" + loaderResult.toString(), expectedValidationMessages, loaderResult.getMessages());
    }
}
