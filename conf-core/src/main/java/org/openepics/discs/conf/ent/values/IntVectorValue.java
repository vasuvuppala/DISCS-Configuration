/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 * Controls Configuration Database is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or any
 * newer version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.conf.ent.values;

import java.util.List;

/**
 * 1-D vector if integer numbers.
 *
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 *
 */
public class IntVectorValue extends Value {
    private static final int MAX_ELEMENTS = 5;

    private List<Integer> intVectorValue;

    public IntVectorValue(List<Integer> intVectorValue) {
        this.intVectorValue = intVectorValue;
    }

    /**
     * @return the intVectorValue
     */
    public List<Integer> getIntVectorValue() { return intVectorValue; }

    /**
     * @param intVectorValue the intVectorValue to set
     */
    public void setIntVectorValue(List<Integer> intVectorValue) { this.intVectorValue = intVectorValue; }

    @Override
    public String toString() {
        StringBuilder retStr = new StringBuilder() ;
        int i = 0;
        retStr.append("(integer vector): [");

        for (Integer item : intVectorValue) {
            retStr.append(item).append(", ");
            if (i++ > MAX_ELEMENTS) {
                retStr.append("...");
                break;
            }
        }
        retStr.append(']');

        return retStr.toString();
    }

}
