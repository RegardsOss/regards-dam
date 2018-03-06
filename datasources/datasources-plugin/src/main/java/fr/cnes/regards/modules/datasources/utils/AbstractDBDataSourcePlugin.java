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

package fr.cnes.regards.modules.datasources.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.datasources.domain.plugins.DataSourceException;
import fr.cnes.regards.modules.datasources.domain.plugins.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.domain.plugins.IDBDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.DataObject;

/**
 * A {@link Plugin} to retrieve the data elements from a SQL Database.</br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the Database.
 * @author Christophe Mertz
 */
public abstract class AbstractDBDataSourcePlugin extends AbstractDataObjectMapping implements IDBDataSourcePlugin {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDBDataSourcePlugin.class);

    protected static final String SELECT = "SELECT ";

    protected static final String WHERE = " WHERE ";

    protected static final String SELECT_COUNT = "SELECT COUNT(*) ";

    protected static final String LIMIT_CLAUSE = " ORDER BY %s LIMIT %d OFFSET %d";

    /**
     * By default, the refresh rate is set to 1 day (in ms)
     */
    @Override
    public int getRefreshRate() {
        return 86400;
    }

    protected abstract String getFromClause();

    protected String getSelectRequest(Pageable pageable, OffsetDateTime sinceData) {
        if ((sinceData != null) && !getLastUpdateAttributeName().isEmpty()) {
            return SELECT + buildColumnClause(columns.toArray(new String[0])) + getFromClause() + WHERE
                    + AbstractDataObjectMapping.LAST_MODIFICATION_DATE_KEYWORD + buildLimitPart(pageable);
        } else {
            return SELECT + buildColumnClause(columns.toArray(new String[0])) + getFromClause() + buildLimitPart(
                    pageable);
        }
    }

    protected String getCountRequest(OffsetDateTime sinceDate) {
        if ((sinceDate != null) && !getLastUpdateAttributeName().isEmpty()) {
            return SELECT_COUNT + getFromClause() + WHERE + AbstractDataObjectMapping.LAST_MODIFICATION_DATE_KEYWORD;

        } else {
            return SELECT_COUNT + getFromClause();
        }

    }

    /**
     * @param sinceDate can be null
     */
    public Page<DataObject> findAll(String tenant, Pageable pageable, OffsetDateTime sinceDate) throws DataSourceException {
        final String selectRequest = getSelectRequest(pageable, sinceDate);
        final String countRequest = getCountRequest(sinceDate);

        LOG.debug("select request: {}", selectRequest);
        LOG.debug("count request: {}", countRequest);

        try (Connection conn = getDBConnection().getConnection()) {
            return findAll(tenant, conn, selectRequest, countRequest, pageable, sinceDate);
        } catch (SQLException e) {
            LOG.error("Unable to obtain a database connection.", e);
            throw new DataSourceException("Unable to obtain a database connection.", e);
        }
    }

    /**
     * Add to the SQL request the part to fetch only a portion of the results.
     * @param pageable the page of the element to fetch
     * @return the SQL request
     */
    protected String buildLimitPart(Pageable pageable) {
        StringBuilder str = new StringBuilder(" ");
        final int offset = pageable.getPageNumber() * pageable.getPageSize();
        final String limit = String.format(LIMIT_CLAUSE, orderByColumn, pageable.getPageSize(), offset);
        str.append(limit);

        return str.toString();
    }
}
