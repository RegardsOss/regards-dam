/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.cnes.regards.modules.datasources.domain.plugins;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;

/**
 * Class IDBDataSourceFromSingleTablePlugin
 *
 * Allows to search in a database, and to explore the database's tables, columns and indexes.
 * @author Christophe Mertz
 */
@PluginInterface(description = "Plugin to explore a data source and search in a single table of the data source")
public interface IDBDataSourceFromSingleTablePlugin extends IDBDataSourcePlugin {

    /**
     * Allows to define the database table used, and the columns of this table.</br>
     * The tables and columns are used to generate the SQL request used to execute statement on the database.
     * @param pTable the name of the table
     */
    void initializePluginMapping(String pTable);
}
