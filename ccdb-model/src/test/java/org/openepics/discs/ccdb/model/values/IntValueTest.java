/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of CCDB System.
 *
 * CCDB is free software: you can redistribute it
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

package org.openepics.discs.ccdb.model.values;

import org.openepics.discs.ccdb.model.values.IntValue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 *
 */
public class IntValueTest {

    @Test(expected = NullPointerException.class)
    public void intValue() {
        IntValue intValue = new IntValue(null);
    }

    @Test
    public void displayToString() {
        IntValue intValue = new IntValue(123);
        assertEquals("123", intValue.toString());
    }

    @Test
    public void displayAuditLog() {
        IntValue intValue = new IntValue(123);
        assertEquals("123", intValue.auditLogString());
    }
}
