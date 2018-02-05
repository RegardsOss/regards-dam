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

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDBDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Plugin} to extract data from a PostgreSQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "postgresql-datasource", version = "2.0-SNAPSHOT",
        description = "Allows data extraction to a PostgreSql database", author = "REGARDS Team",
        contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss")
public class PostgreDataSourcePlugin extends AbstractDBDataSourcePlugin {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourcePlugin.class);

    @PluginParameter(name = CONNECTION_PARAM, label = "Database connection plugin")
    private IDBConnectionPlugin dbConnection;

    @PluginParameter(name = FROM_CLAUSE, label = "SQL FROM clause")
    private String sqlFromClause;

    @PluginParameter(name = MODEL_NAME_PARAM, label = "model name", description = "Associated data source model name")
    private String modelName;

    @PluginParameter(name = MODEL_MAPPING_PARAM, label = "model attributes mapping",
            description = "Mapping between model and database table (in JSON format)")
    private List<AbstractAttributeMapping> attributesMapping;

    @PluginParameter(name = REFRESH_RATE, defaultValue = REFRESH_RATE_DEFAULT_VALUE_AS_STRING, optional = true,
            label = "refresh rate",
            description = "Ingestion refresh rate in seconds (minimum delay between two consecutive ingestions)")
    private Integer refreshRate;

    @PluginParameter(name = TAGS, label = "data objects common tags", optional = true,
            description = "Common tags to be put on all data objects created by the data source")
    private Collection<String> commonTags = Collections.emptyList();

    /**
     * Init method
     */
    @PluginInit
    private void initPlugin() throws ModuleException {
        LOG.info("Init method call : {}, connection = {}, model = {}, mapping = {}, request = {}", this.getClass().getName(),
                 dbConnection.toString(), modelName, attributesMapping, sqlFromClause);
        LOG.info("Init method call : {}",
                 dbConnection.testConnection() ? "CONNECTION_PARAM IS VALID" : "ERROR CONNECTION_PARAM");

        init(modelName, attributesMapping, commonTags);
    }

    @Override
    public IDBConnectionPlugin getDBConnection() {
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
