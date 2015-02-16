/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 *
 * Controls Configuration Database is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or any newer
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */

package org.openepics.discs.conf.ui;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.openepics.discs.conf.ejb.ComptypeEJB;
import org.openepics.discs.conf.ejb.DeviceEJB;
import org.openepics.discs.conf.ejb.InstallationEJB;
import org.openepics.discs.conf.ent.ComponentType;
import org.openepics.discs.conf.ent.Device;
import org.openepics.discs.conf.ent.DeviceStatus;
import org.openepics.discs.conf.ent.InstallationRecord;
import org.openepics.discs.conf.util.Utility;
import org.openepics.discs.conf.views.DeviceView;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


/**
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 *
 */
@Named
@ViewScoped
public class DevicesByTypeManager implements Serializable {
    private static final long serialVersionUID = 3236468538191653638L;

    @Inject private transient ComptypeEJB componentTypesEJB;
    @Inject private transient DeviceEJB deviceEJB;
    @Inject private transient InstallationEJB installationEJB;

    private ComponentType selectedComponentType;

    private List<DeviceView> devices;
    private transient List<DeviceView> filteredDevices;
    private Device selectedDevice;

    private List<SelectItem> statusLabels;

    private String serialNumber;
    private String description;

    public DevicesByTypeManager() {
    }

    /**
     * Java EE post construct life-cycle method.
     */
    @PostConstruct
    public void init() {
        if (((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter("id")
                != null) {
            final Long id = Long.parseLong(
                    ((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).
                    getParameter("id"));
            selectedComponentType = componentTypesEJB.findById(id);
            prepareDevicesForDisplay();
            prepareStatusLabels();
        }
    }

    /**
     * Creates a new device instance and adds all properties to it which are defined by device type.
     */
    public void onDeviceAdd() {
        final Device newDevice = new Device(serialNumber);
        newDevice.setComponentType(selectedComponentType);
        newDevice.setDescription(description);

        try {
            deviceEJB.addDeviceAndPropertyDefs(newDevice);
            Utility.showMessage(FacesMessage.SEVERITY_INFO, "Device saved.", null);
        } finally {
            resetDeviceDialogFields();
            prepareDevicesForDisplay();
        }
    }

    /**
     * Event handler which handles the device delete
     */
    public void onDeviceDelete() {
        Preconditions.checkNotNull(selectedDevice);

        if (installationEJB.getActiveInstallationRecordForDevice(selectedDevice) == null) {
            try {
                deviceEJB.delete(selectedDevice);

                selectedDevice = null;
                prepareDevicesForDisplay();

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Device deleted.", null));
            } catch (Exception e) {
                if (Utility.causedByPersistenceException(e)) {
                    Utility.showMessage(FacesMessage.SEVERITY_ERROR, Utility.MESSAGE_SUMMARY_DELETE_FAIL,
                            "The property could not be deleted because it is used.");
                } else {
                    throw e;
                }
            }
        } else {
            Utility.showMessage(FacesMessage.SEVERITY_ERROR, Utility.MESSAGE_SUMMARY_DELETE_FAIL,
                    "Device instance could not be deleted because it is installed.");
        }
    }

    private void resetDeviceDialogFields() {
        serialNumber = null;
        description = null;
    }

    /** Called when the user clicks the "pencil" icon in the table listing the devices. The user is redirected
     * to the attribute manager screen.
     * @param id The primary key of the {@link Device} entity
     * @return The URL to redirect to
     */
    public String deviceDetailsRedirect(Long id) {
        return "device-details.xhtml?faces-redirect=true&id=" + id;
    }

    /** Called when the user clicks the "Close" button on the device details screen. The user is redirected
     * to the list of all device instances of the current device type.
     * @param id The primary key of the {@link ComponentType} entity
     * @return The URL to redirect to
     */
    public String redirectToAllDevices(Long id) {
        return "index.html?faces-redirect=true&id=" + id;
    }

    /**
     * @return The device type the user selected in the left hand side table listing all available device type
     */
    public ComponentType getSelectedComponentType() {
        return selectedComponentType;
    }
    /**
     * @param selectedComponentType The device type the user selected in the left hand side table listing all available
     * device type
     */
    public void setSelectedComponentType(ComponentType selectedComponentType) {
        this.selectedComponentType = selectedComponentType;
    }

    /**
     * The method prepares the list of {@link Device} instances to show to the user when he selects a device type in
     * the left hand side table listing all available device type.
     */
    public void prepareDevicesForDisplay() {
        final List<Device> deviceList = deviceEJB.findDevicesByComponentType(selectedComponentType);

        // transform the list of Unit into a list of UnitView
        devices = ImmutableList.copyOf(Lists.transform(deviceList, new Function<Device, DeviceView>() {
                                                @Override
                                                public DeviceView apply(Device input) {
                                                    return new DeviceView(input, getInstalledSlotForDevice(input));
                                                }}));
    }

    private String getInstalledSlotForDevice(Device device) {
        final InstallationRecord record = installationEJB.getActiveInstallationRecordForDevice(device);
        return record == null ? "-" : record.getSlot().getName();
    }

    private void prepareStatusLabels() {
        if (statusLabels == null) {
            statusLabels = Lists.newArrayList();
            statusLabels.add(new SelectItem("", "Select one"));
            for (DeviceStatus status : DeviceStatus.values()) {
                statusLabels.add(new SelectItem(status.getLabel(), status.getLabel()));
            }
        }
    }

    /** @return the list of labels for the filter */
    public List<SelectItem> getStatusLabels() {
        prepareStatusLabels();
        return statusLabels;
    }

    /**
     * @return The list of all {@link Device} instances to display to the the user
     */
    public List<DeviceView> getDevices() {
        return devices;
    }

    /**
     * @return The reference to the device instance displayed in the row the user clicked the action for.
     */
    public Device getSelectedDevice() {
        return selectedDevice;
    }
    /**
     * @param selectedDevice The reference to the device instance displayed in the row the user clicked the action for.
     */
    public void setSelectedDevice(Device selectedDevice) {
        this.selectedDevice = selectedDevice;
    }

    /**
     * @return The inventory ID (see {@link Device#getSerialNumber()}) of the {@link Device}
     */
    public String getSerialNumber() {
        return serialNumber;
    }
    /**
     * @param serialNumber The inventory ID (see {@link Device#getSerialNumber()}) of the {@link Device}
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * @return The description (see {@link Device#getDescription()}) of the {@link Device}
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description The description (see {@link Device#getDescription()}) of the {@link Device}
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** The validator for the inventory ID input field. Called when creating a new device {@link Device}
     * @param ctx {@link javax.faces.context.FacesContext}
     * @param component {@link javax.faces.component.UIComponent}
     * @param value The value
     * @throws ValidatorException {@link javax.faces.validator.ValidatorException}
     */
    public void newDeviceSerialNoValidator(FacesContext ctx, UIComponent component, Object value)
            throws ValidatorException {
        if (value == null) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, Utility.MESSAGE_SUMMARY_ERROR,
                                                                    "No value to parse."));
        }
        final String strValue = value.toString();

        // empty input value is handled by PrimeFaces and configured from xhtml
        if (!strValue.isEmpty()) {
            final Device existingDevice = deviceEJB.findDeviceBySerialNumber(strValue);
            if (existingDevice != null) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            Utility.MESSAGE_SUMMARY_ERROR, "Device instance with this inventory ID already exists."));
            }
        }
    }

    /** @return the filteredDevices */
    public List<DeviceView> getFilteredDevices() {
        return filteredDevices;
    }

    /** @param filteredDevices the filteredDevices to set */
    public void setFilteredDevices(List<DeviceView> filteredDevices) {
        this.filteredDevices = filteredDevices;
    }
}
