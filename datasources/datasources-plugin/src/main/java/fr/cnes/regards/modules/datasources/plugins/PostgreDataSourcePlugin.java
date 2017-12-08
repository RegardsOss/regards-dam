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

package fr.cnes.regards.modules.datasources.plugins;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDBDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * A {@link Plugin} to extract data from a PostgreSQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "postgresql-datasource", version = "2.0-SNAPSHOT",
        description = "Allows data extraction to a PostgreSql database", author = "REGARDS Team",
        contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss")
public class PostgreDataSourcePlugin extends AbstractDBDataSourcePlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourcePlugin.class);

    /**
     * The connection to the database
     */
    @PluginParameter(name = CONNECTION_PARAM, label = "Database connection plugin" )
    private IDBConnectionPlugin dbConnection;

    /**
     * The SQL request
     */
    @PluginParameter(name = FROM_CLAUSE, label = "SQL FROM clause")
    private String sqlFromClause;

    /**
     * The {@link Model} to used by the {@link Plugin} in JSon format.
     */
    @PluginParameter(name = MODEL_PARAM, label = "model mapping",
            description = "Mapping between model and database table (in JSON format)")
    private String modelMappingJSon;

    /**
     * Ingestion refresh rate in seconds
     */
    @PluginParameter(name = REFRESH_RATE, defaultValue = REFRESH_RATE_DEFAULT_VALUE, optional = true,
            label = "refresh rate",
            description = "Ingestion refresh rate in seconds (minimum delay between two consecutive ingestions)")
    private Integer refreshRate;

    /**
     * Init method
     */
    @PluginInit
    private void initPlugin() {
        LOG.info("Init method call : " + this.getClass().getName() + "connection=" + dbConnection.toString() + "model="
                + modelMappingJSon + "requete=" + sqlFromClause);

        LOG.info("Init method call : "
                + (dbConnection.testConnection() ? "CONNECTION_PARAM IS VALID" : "ERROR CONNECTION_PARAM"));

        // Converts the modelJson to a list of AbstractAttributeMapping
        initDataSourceMapping(modelMappingJSon);
    }

    @Override
    public IDBConnectionPlugin getDBConnection() throws SQLException {
        return dbConnection;
    }

    @Override
    protected String getFromClause() {
        return sqlFromClause;
    }

    @Override
    public int getRefreshRate() {
        return refreshRate;
    }

    @Override
    protected AbstractAttribute<?> buildDateAttribute(ResultSet rs, String attrName, String attrDSName, String colName)
            throws SQLException {
        OffsetDateTime date = buildOffsetDateTime(rs, colName);
        return AttributeBuilder.buildDate(attrName, date);
    }
}
