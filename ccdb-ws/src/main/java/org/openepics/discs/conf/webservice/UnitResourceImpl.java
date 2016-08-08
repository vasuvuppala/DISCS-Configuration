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
package org.openepics.discs.conf.webservice;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import org.openepics.discs.ccdb.core.ejb.UnitEJB;
import org.openepics.discs.ccdb.jaxb.UnitRep;
import org.openepics.discs.ccdb.jaxrs.UnitResource;
import org.openepics.discs.ccdb.model.Unit;

/**
 * An implementation of the InstallationSlotResource interface.
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 */
public class UnitResourceImpl implements UnitResource {

    @EJB
    private UnitEJB unitEJB;

    private static final Logger LOGGER = Logger.getLogger(UnitResourceImpl.class.getName());

    @Override
    public List<UnitRep> searchUnits(String query) {
        List<Unit> properties = unitEJB.queryByName(query);
        
        if (properties == null) {
            return null;
        }
        LOGGER.log(Level.FINE, "Found number of properties: {0}", properties.size());
               
        List<UnitRep> propReps = properties.stream().map(p -> UnitRep.newInstance(p)).collect(Collectors.toList());
        return propReps;
    }

    @Override
    public UnitRep getUnit(String name) {
        Unit property = unitEJB.findByName(name);
        
        if (property == null) {
            return null;
        } else {
            return UnitRep.newInstance(property);
        }

    }

}
