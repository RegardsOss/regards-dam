/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nurkiewicz.jdbcrepository.sql.OracleSqlGenerator;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class OracleDataSourceFromSingleTablePlugin
 *
 * A {@link Plugin} to discover the tables, columns and indexes to a PostgreSQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "oracle-datasource-single-table", author = "CSSI", version = "1.0-SNAPSHOT",
        description = "Allows introspection and data extraction to a Oracle database")
public class OracleDataSourceFromSingleTablePlugin extends AbstractDataSourceFromSingleTablePlugin
        implements IDataSourceFromSingleTablePlugin {

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
     * Init method
     */
    @PluginInit
    private void initPlugin() {
        LOG.info("Init method call : " + this.getClass().getName() + "connection=" + this.dbConnection.toString()
                + "table name=" + this.tableName + "model=" + this.modelJSon);

        // Converts the modelJson to a list of DataSourceAttributeMapping
        initDataSourceMapping(this.modelJSon);

        initializePluginMapping(this.tableName);
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
    protected IDBConnectionPlugin getDBConnectionPlugin() {
        return dbConnection;
    }

    @Override
    protected AbstractAttribute<?> buildDateAttribute(ResultSet pRs, DataSourceAttributeMapping pAttrMapping)
            throws SQLException {
        LocalDateTime ldt;

        if (pAttrMapping.getTypeDS() == null) {
            ldt = buildLocatDateTime(pRs, pAttrMapping);
        } else {
            long n;
            Instant instant;

            switch (pAttrMapping.getTypeDS()) {
                case Types.DECIMAL:
                case Types.NUMERIC:
                    n = pRs.getLong(pAttrMapping.getNameDS());
                    instant = Instant.ofEpochMilli(n);
                    ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    break;
                default:
                    ldt = buildLocatDateTime(pRs, pAttrMapping);
                    break;
            }
        }

        return AttributeBuilder.buildDate(pAttrMapping.getName(), ldt);
    }

    @Override
    public boolean isInternalDataSource() {
        return false;
    }

}