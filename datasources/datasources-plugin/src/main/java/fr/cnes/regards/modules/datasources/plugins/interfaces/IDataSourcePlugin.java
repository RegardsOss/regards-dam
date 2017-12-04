/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

package fr.cnes.regards.modules.datasources.plugins.interfaces;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.datasources.plugins.exception.DataSourceException;
import fr.cnes.regards.modules.entities.domain.DataObject;

/**
 * Class IDataSourcePlugin
 *
 * Allows to search in a data source,
 * @author Christophe Mertz
 * @author oroussel
 */
@PluginInterface(description = "Plugin to search in a data source")
public interface IDataSourcePlugin {

    /**
     * The model parameter name
     */
    String MODEL_PARAM = "model";

    /**
     * Ingestion refresh rate parameter name
     */
    String REFRESH_RATE = "refreshRate";

    /**
     * Ingestion refresh rate default value in seconds
     */
    String REFRESH_RATE_DEFAULT_VALUE = "86400";

    /**
     * The refresh rate of the data source
     * @return the refresh rate value (in seconds)
     */
    int getRefreshRate();

    /**
     * Returns a {@link Page} of new entities meeting the paging restriction provided in the {@code Pageable} object.
     * @param tenant tenant to build URN
     * @param pageable the pagination information
     * @param date Allows to filter the new entities created after this date parameter (can be null)
     * @return a page of entities
     */
    Page<DataObject> findAll(String tenant, Pageable pageable, OffsetDateTime date) throws DataSourceException;

    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
     * @param tenant tenant to build URN
     * @param pageable the pagination information
     * @return a page of entities
     */
    default Page<DataObject> findAll(String tenant, Pageable pageable) throws DataSourceException {
        return this.findAll(tenant, pageable, null);
    }

}
