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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nurkiewicz.jdbcrepository.sql.OracleSqlGenerator;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * A {@link Plugin} to discover the tables, columns and indexes to an Oracle Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "oracle-datasource-single-table", version = "1.0-SNAPSHOT",
        description = "Allows introspection and data extraction to a Oracle database", author = "REGARDS Team",
        contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss")
public class OracleDataSourceFromSingleTablePlugin extends AbstractDataSourceFromSingleTablePlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(OracleDataSourceFromSingleTablePlugin.class);

    /**
     * The connection to the database
     */
    @PluginParameter(name = CONNECTION_PARAM)
    private IDBConnectionPlugin dbConnection;

    /**
     * The table name used to request the database
     */
    @PluginParameter(name = TABLE_PARAM)
    private String tableName;

    /**
     * The {@link Model} to used by the {@link Plugin} in JSon format
     */
    @PluginParameter(name = MODEL_PARAM)
    private String modelJSon;

    /**
     * Ingestion refresh rate (in seconds)
     */
    @PluginParameter(name = REFRESH_RATE, defaultValue = REFRESH_RATE_DEFAULT_VALUE, optional = true)
    private Integer refreshRate;

    /**
     * Init method
     */
    @PluginInit
    private void initPlugin() {
        LOG.info("Init method call : " + this.getClass().getName() + "connection=" + dbConnection.toString()
                + "table name=" + tableName + "model=" + modelJSon);

        // Converts the modelJson to a list of AbstractAttributeMapping
        initDataSourceMapping(modelJSon);

        initializePluginMapping(tableName);

        try {
            initDataSourceColumns(getDBConnection());
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    protected SqlGenerator buildSqlGenerator() {
        return new OracleSqlGenerator();
    }

    @Override
    protected SqlGenerator buildSqlGenerator(String pAllColumnsClause, String pOrderBy) {
        return new OracleSqlGenerator(pAllColumnsClause);
    }

    @Override
    public IDBConnectionPlugin getDBConnection() throws SQLException {
        return dbConnection;
    }

    @Override
    protected AbstractAttribute<?> buildDateAttribute(ResultSet rs, String attrName, String attrDSName, String colName)
            throws SQLException {
        OffsetDateTime date;
        Integer typeDS = getTypeDs(attrDSName);

        if (typeDS == null) {
            date = buildOffsetDateTime(rs, attrName);
        } else {
            long n;
            Instant instant;

            switch (typeDS) {
                case Types.DECIMAL:
                case Types.NUMERIC:
                    n = rs.getLong(colName);
                    instant = Instant.ofEpochMilli(n);
                    date = OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
                    break;
                default:
                    date = buildOffsetDateTime(rs, colName);
                    break;
            }
        }

        return AttributeBuilder.buildDate(attrName, date);
    }

    @Override
    public boolean isInternalDataSource() {
        return false;
    }

    @Override
    public int getRefreshRate() {
        return refreshRate;
    }

}
