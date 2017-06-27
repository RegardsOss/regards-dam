/*
 * LICENSE_PLACEHOLDER
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

import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.utils.PostgreSqlGenerator;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class PostgreDataSourceFromSingleTablePlugin A {@link Plugin} to discover the tables, columns and indexes to a
 * PostgreSQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "postgresql-datasource-single-table", version = "1.0-SNAPSHOT",
        description = "Allows introspection and data extraction to a PostgreSql database", author = "REGARDS Team",
        contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss")
public class PostgreDataSourceFromSingleTablePlugin extends AbstractDataSourceFromSingleTablePlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourceFromSingleTablePlugin.class);

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
     * Is this data source is a REGARDS internal data source
     */
    @PluginParameter(name = IS_INTERNAL_PARAM, defaultValue = "false", optional = true)
    private String internalDataSource;

    /**
     * Ingestion refresh rate
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
        return new PostgreSqlGenerator();
    }

    @Override
    protected SqlGenerator buildSqlGenerator(String pAllColumnsClause, String pOrderBy) {
        return new PostgreSqlGenerator(pAllColumnsClause, pOrderBy);
    }

    @Override
    public IDBConnectionPlugin getDBConnection() throws SQLException {
        return dbConnection;
    }

    @Override
    /**
     * @see https://jdbc.postgresql.org/documentation/head/8-date-time.html
     */
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
    public boolean isInternalDataSource() {
        return !internalDataSource.isEmpty() && TRUE_INTERNAL_DATASOURCE.equalsIgnoreCase(internalDataSource);
    }

    @Override
    public int getRefreshRate() {
        return refreshRate;
    }
}
