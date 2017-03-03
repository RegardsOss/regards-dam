/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nurkiewicz.jdbcrepository.sql.OracleSqlGenerator;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class OracleDataSourceFromSingleTablePlugin
 *
 * A {@link Plugin} to discover the tables, colums and index of a SQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "oracle-datasource-single-table", author = "CSSI", version = "1.0-SNAPSHOT",
        description = "Allows introspection to a Oracle database")
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
     * THe {@link Model} to used by the {@link Plugin} in JSon format.
     */
    @PluginParameter(name = MODEL_PARAM)
    private String modelJSon;

    /**
     * Init method
     */
    @PluginInit
    private void initPlugin() {
        LOG.info("Init method call : " + this.getClass().getName() + "connection=" + this.dbConnection.toString()
                + "model=" + this.modelJSon);

        // Converts the modelJson to a list of DataSourceAttributeMapping
        initDataSourceMapping(this.modelJSon);
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

}
