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
package fr.cnes.regards.modules.dam.rest.datasources;

import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.DataSourceException;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDBConnectionPlugin;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDBDataSourcePlugin;
import fr.cnes.regards.modules.dam.domain.entities.feature.DataObjectFeature;

/**
 * For testing purpose
 * @author Marc Sordi
 *
 */
@Plugin(id = "MockDatasourcePlugin", author = "CSSI", contact = "CSSI", description = "MockDatasourcePlugin",
        version = "alpha", url = "none", owner = "CSSI", licence = "none")
public class MockDatasourcePlugin implements IDBDataSourcePlugin {

    @Override
    public int getRefreshRate() {
        return 0;
    }

    @Override
    public Page<DataObjectFeature> findAll(String tenant, Pageable pageable, OffsetDateTime date)
            throws DataSourceException {
        return null;
    }

    @Override
    public IDBConnectionPlugin getDBConnection() throws SQLException {
        return null;
    }

    @Override
    public String getModelName() {
        return "Cindy Crawford";
    }
}
