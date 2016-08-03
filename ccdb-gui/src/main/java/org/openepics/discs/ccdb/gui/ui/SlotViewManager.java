/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.discs.ccdb.gui.ui;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.openepics.discs.ccdb.core.ejb.PropertyEJB;
import org.openepics.discs.ccdb.core.ejb.SlotEJB;
import org.openepics.discs.ccdb.gui.ui.common.ViewType;
import org.openepics.discs.ccdb.model.Property;
import org.openepics.discs.ccdb.model.Slot;
import org.openepics.discs.ccdb.model.SlotPropertyValue;

/**
 *
 * @author vuppala
 */
@Named
@ViewScoped
public class SlotViewManager implements Serializable {
    
    @EJB private SlotEJB slotEJB;
    @EJB private PropertyEJB propEJB;
    
    private static final Logger LOGGER = Logger.getLogger(SlotListView.class.getName());

    // request parameters
    private ViewType viewType = ViewType.CUSTOM;
    
    private List<Slot> slots;
    private List<Property> selectedProperties;
    private List<Property> properties;
    private static final String latticeProperties[] = {"AccumulatedLengthC2C", "AccumulatedLengthE2E", "BeamlinePosition", "EffectiveLength", "GlobalX", "GlobalY", "GlobalZ"};
    private static final String cmProperties[] = {"LevelOfCare", "AssociatedDHR", "AssociatedARR", "AreaManager", "DHRApprover", "ARRApprover", "AssociatedArea", "MachineModes"};
    
    public SlotViewManager() {
    }

    @PostConstruct
    public void init() {
        properties = propEJB.findAllOrderedByName();
        slots = slotEJB.findByIsHostingSlot(true);
        initialize();
        LOGGER.log(Level.FINE, "Found number of slots: {0}", slots.size());       
    }
    
    public void initialize() {
        switch (viewType) {
            case LATTICE: 
                selectedProperties = Arrays.stream(latticeProperties).map(n -> propEJB.findByName(n)).collect(Collectors.toList());
                break;
            case CM: 
                selectedProperties = Arrays.stream(cmProperties).map(n -> propEJB.findByName(n)).collect(Collectors.toList());
                break;
            case CUSTOM:
                selectedProperties = Arrays.stream(latticeProperties).map(n -> propEJB.findByName(n)).collect(Collectors.toList());
                break;
            default:
                selectedProperties = Arrays.stream(latticeProperties).map(n -> propEJB.findByName(n)).collect(Collectors.toList());
                LOGGER.log(Level.WARNING, "Invalid view type {0}", viewType);
                break;            
        }
    }
 
    /**
     * Return value of a slot property
     * 
     * @param slot
     * @param property
     * @return 
     */
     public String slotProperty(Slot slot,  Property property) {
        SlotPropertyValue value = slotEJB.getPropertyValue(slot, property);
        return value == null? "": (value.getPropValue() == null? "": value.getPropValue().toString());
    }
    
    
    
    // --- getters/setters

    public List<Slot> getSlots() {
        return slots;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<Property> getSelectedProperties() {
        return selectedProperties;
    }

    public void setSelectedProperties(List<Property> selectedProperties) {
        this.selectedProperties = selectedProperties;
    }

    public ViewType getViewType() {
        return viewType;
    }

    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
    }

}
