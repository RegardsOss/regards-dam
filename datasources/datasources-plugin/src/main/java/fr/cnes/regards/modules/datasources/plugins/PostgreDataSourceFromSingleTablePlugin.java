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
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDBDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.utils.PostgreSqlGenerator;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;

/**
 * Class PostgreDataSourceFromSingleTablePlugin A {@link Plugin} to discover the tables, columns and indexes to a
 * PostgreSQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "postgresql-datasource-single-table", version = "2.0-SNAPSHOT",
        description = "Allows introspection and data extraction to a PostgreSql database", author = "REGARDS Team",
        contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss")
public class PostgreDataSourceFromSingleTablePlugin extends AbstractDBDataSourceFromSingleTablePlugin {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourceFromSingleTablePlugin.class);

    @PluginParameter(name = CONNECTION_PARAM, label = "Database connection plugin")
    private IDBConnectionPlugin dbConnection;

    @PluginParameter(name = TABLE_PARAM, label = "Table name", description = "Database table name to be requested")
    private String tableName;

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
        LOG.info("Init method call : {}, connection = {}, table name = {}, model = {}, mapping = {}",
                 this.getClass().getName(), dbConnection.toString(), tableName, modelName, attributesMapping);
        init(modelName, attributesMapping, commonTags);
        initializePluginMapping(tableName);
        initDataSourceColumns(getDBConnection());
    }

    @Override
    protected SqlGenerator buildSqlGenerator() {
        return new PostgreSqlGenerator();
    }

    @Override
    protected SqlGenerator buildSqlGenerator(String allColumnsClause, String orderBy) {
        return new PostgreSqlGenerator(allColumnsClause, orderBy);
    }

    @Override
    public IDBConnectionPlugin getDBConnection() {
        return dbConnection;
    }

    /**
     * @see 'https://jdbc.postgresql.org/documentation/head/8-date-time.html'
     */
    @Override
    protected AbstractAttribute<?> buildDateAttribute(ResultSet rs, String attrName, String attrDSName, String colName)
            throws SQLException {
        OffsetDateTime ldt;
        Integer typeDS = getTypeDs(attrDSName);

        if (typeDS == null) {
            ldt = buildOffsetDateTime(rs, colName);
        } else {
            long n;
            Instant instant;

            switch (typeDS) {
                case Types.TIME:
                    n = rs.getTime(colName).getTime();
                    instant = Instant.ofEpochMilli(n);
                    ldt = OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
                    break;
                case Types.DATE:
                    n = rs.getDate(colName).getTime();
                    instant = Instant.ofEpochMilli(n);
                    ldt = OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
                    break;
                default:
                    ldt = buildOffsetDateTime(rs, colName);
                    break;
            }
        }

        return AttributeBuilder.buildDate(attrName, ldt);
    }

    @Override
    public int getRefreshRate() {
        return refreshRate;
    }
}
