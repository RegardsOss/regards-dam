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

package fr.cnes.regards.modules.dam.domain.datasources.plugins;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.dam.domain.entities.feature.DataObjectFeature;

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
     * The model name
     * @return model name
     */
    String getModelName();

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
     * @throws DataSourceException
     */
    Page<DataObjectFeature> findAll(String tenant, Pageable pageable, OffsetDateTime date) throws DataSourceException;

    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
     * @param tenant tenant to build URN
     * @param pageable the pagination information
     * @return a page of entities
     * @throws DataSourceException
     */
    default Page<DataObjectFeature> findAll(String tenant, Pageable pageable) throws DataSourceException {
        return this.findAll(tenant, pageable, null);
    }

}
