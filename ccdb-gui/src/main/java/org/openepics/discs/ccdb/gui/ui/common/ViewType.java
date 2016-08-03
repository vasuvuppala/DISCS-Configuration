/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.discs.ccdb.gui.ui.common;

/**
 * Views for slots
 * 
 * @author vuppala
 */
public enum ViewType {
    LATTICE("Lattice"), CM("Configuration Management"), CUSTOM("Custom");
    
    private final String name;
    
    private ViewType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
