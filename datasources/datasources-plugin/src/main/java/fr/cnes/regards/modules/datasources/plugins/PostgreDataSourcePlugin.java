/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
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
        contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss")
public class PostgreDataSourcePlugin extends AbstractDataSourcePlugin {

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
    @PluginParameter(name = IS_INTERNAL_PARAM, value="false")
    private String internalDataSource;
    
    /**
     * Ingestion refresh rate
     */
    @PluginParameter(name = REFRESH_RATE, value="1800")
    private Integer refreshRate;

    /**
     * Init method
     */
    @PluginInit
    private void initPlugin() {
        LOG.info("Init method call : " + this.getClass().getName() + "connection=" + dbConnection.toString() + "model="
                + modelJSon + "requete=" + sqlFromClause);

        LOG.info("Init method call : "
                + (dbConnection.testConnection() ? "CONNECTION_PARAM IS VALID" : "ERROR CONNECTION_PARAM"));

        // Converts the modelJson to a list of AbstractAttributeMapping
        initDataSourceMapping(modelJSon);
    }

    @Override
    protected AbstractAttribute<?> buildDateAttribute(ResultSet pRs, AbstractAttributeMapping pAttrMapping)
            throws SQLException {
        OffsetDateTime date = buildOffsetDateTime(pRs, pAttrMapping);
        return AttributeBuilder.buildDate(pAttrMapping.getName(), date);
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
        return refreshRate;
    }
}
