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

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.datasources.domain.DBConnection;
import fr.cnes.regards.modules.entities.domain.DataObject;

/**
 * Class IDataSourcePlugin
 *
 * Allows to search in a data base,
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@PluginInterface(description = "Plugin to search in a data source")
public interface IDataSourcePlugin {

    /**
     * The from clause to apply to the SQL request parameter name
     */
    String FROM_CLAUSE = "fromClause";

    /**
     * The model parameter name
     */
    String MODEL_PARAM = "model";

    /**
     * The connection parameter name
     */
    String CONNECTION_PARAM = "connection";

    /**
     * The connection parameter name
     */
    String IS_INTERNAL_PARAM = "internalDataSource";

    String TRUE_INTERNAL_DATASOURCE = "true";

    /**
     * Ingestion refresh rate parameter name
     */
    String REFRESH_RATE = "refreshRate";

    /**
     * Ingestion refresh rate default value in seconds
     */
    String REFRESH_RATE_DEFAULT_VALUE = "86400";

    /**
     * Retrieve the {@link DBConnection} used by the {@link Plugin}
     *
     * @return Retrieve a {@link DBConnection}
     *
     * @throws SQLException
     *             the {@link Connection} is not available
     */
    IDBConnectionPlugin getDBConnection() throws SQLException;

    /**
     * The refresh rate of the data source
     *
     * @return the refresh rate value (in seconds)
     */
    int getRefreshRate();

    /**
     * Returns <code>true</code> if the data source is connected to internal database.
     *
     * @return boolean
     */
    boolean isInternalDataSource();

    /**
     * Returns a {@link Page} of new entities meeting the paging restriction provided in the {@code Pageable} object.
     *
     * @param pTenant
     *            tenant to build URN
     * @param pPageable
     *            the pagination information
     * @param pDate
     *            Allows to filter the new entities created after this date parameter (can be null)
     * @return a page of entities
     */
    Page<DataObject> findAll(String pTenant, Pageable pPageable, OffsetDateTime pDate);

    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
     *
     * @param pTenant
     *            tenant to build URN
     * @param pPageable
     *            the pagination information
     * @return a page of entities
     */
    default Page<DataObject> findAll(String pTenant, Pageable pPageable) {
        return this.findAll(pTenant, pPageable, null);
    }

}
