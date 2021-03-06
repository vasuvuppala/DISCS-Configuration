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
package org.openepics.discs.ccdb.gui.ui.common;

import org.openepics.discs.ccdb.core.dl.common.DataLoaderResult;
import org.openepics.discs.ccdb.core.util.ImportFileStatistics;

/**
 * The interface contains all the methods that the UI control handling the import of any number of excel files must implement.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 *
 */
public interface ExcelImportUIHandlers {

    /** The action called to actually import excel file containing properties. */
    public void doImport();

    /** Called to prepare the data to display in the UI "import excel" dialog. */
    public void prepareImportPopup();

    /** @return The results of the "excel import" operation to display to the user. */
    public DataLoaderResult getLoaderResult();

    /** @return the import statistics for the imported file */
    public ImportFileStatistics getImportedFileStatistics();

    /** The method must set the data loader that will be used in the file import. */
    public void setDataLoader();
}
