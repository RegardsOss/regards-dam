/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.datasources;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.dam.domain.datasources.Column;
import fr.cnes.regards.modules.dam.domain.datasources.Table;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDBConnectionPlugin;

/**
 * For testing purpose
 *
 * @author Marc Sordi
 *
 */
@Plugin(id = "MockConnectionPlugin", author = "CSSI", contact = "CSSI", description = "MockConnectionPlugin",
        version = "alpha", url = "none", owner = "CSSI", license = "GPLv3")
public class MockConnectionPlugin implements IDBConnectionPlugin {

    @Override
    public boolean testConnection() {
        return false;
    }

    @Override
    public void closeConnection() {

    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public Map<String, Table> getTables(String schemaPattern, String tableNamePattern) {
        return null;
    }

    @Override
    public Map<String, Column> getColumns(String tableName) {
        return null;
    }

}
