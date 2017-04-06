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

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDataSourcePlugin;
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
@Plugin(id = "postgresql-datasource", version = "1.0-SNAPSHOT",
        description = "Allows data extraction to a PostgreSql database", author = "REGARDS Team",
        contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI", url = "http://www.c-s.fr/")
public class PostgreDataSourcePlugin extends AbstractDataSourcePlugin implements IDataSourcePlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourcePlugin.class);

    /**
     * The connection to the database
     */
    @PluginParameter(name = CONNECTION_PARAM)
    private IDBConnectionPlugin dbConnection;

    /**
     * The SQL request
     */
    @PluginParameter(name = FROM_CLAUSE)
    private String sqlFromClause;

    /**
     * The {@link Model} to used by the {@link Plugin} in JSon format.
     */
    @PluginParameter(name = MODEL_PARAM)
    private String modelJSon;

    /**
     * Is this data source is a REGARDS internal data source
     */
    @PluginParameter(name = IS_INTERNAL_PARAM)
    private String internalDataSource;

    /**
     * Init method
     */
    @PluginInit
    private void initPlugin() {
        LOG.info("Init method call : " + this.getClass().getName() + "connection=" + dbConnection.toString() + "model="
                + modelJSon + "requete=" + sqlFromClause);

        LOG.info("Init method call : "
                + (dbConnection.testConnection() ? "CONNECTION_PARAM IS VALID" : "ERROR CONNECTION_PARAM"));

        // Converts the modelJson to a list of DataSourceAttributeMapping
        initDataSourceMapping(modelJSon);
    }

    @Override
    /**
     * @see https://jdbc.postgresql.org/documentation/head/8-date-time.html
     */
    protected AbstractAttribute<?> buildDateAttribute(ResultSet pRs, DataSourceAttributeMapping pAttrMapping)
            throws SQLException {
        LocalDateTime ldt;

        if (pAttrMapping.getTypeDS() == null) {
            ldt = buildLocatDateTime(pRs, pAttrMapping);
        } else {
            long n;
            Instant instant;

            switch (pAttrMapping.getTypeDS()) {
                case Types.TIME:
                    n = pRs.getTime(pAttrMapping.getNameDS()).getTime();
                    instant = Instant.ofEpochMilli(n);
                    ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    break;
                case Types.DATE:
                    n = pRs.getDate(pAttrMapping.getNameDS()).getTime();
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
        return !internalDataSource.isEmpty() && TRUE_INTERNAL_DATASOURCE.equalsIgnoreCase(internalDataSource);
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
        // in seconds, 30 minutes
        return 1800;
    }

    @Override
    public boolean isOutOfDate() {
        return true;
    }
}
