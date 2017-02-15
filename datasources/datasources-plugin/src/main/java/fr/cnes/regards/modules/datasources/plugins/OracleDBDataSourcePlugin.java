/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;
import com.nurkiewicz.jdbcrepository.sql.OracleSqlGenerator;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.utils.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.utils.ModelMappingAdapter;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class OracleDBDataSourcePlugin
 *
 * A {@link Plugin} to discover the tables, colums and index of a SQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(author = "CSSI", version = "1.0-SNAPSHOT", description = "Allows introspection to a Oracle database")
public class OracleDBDataSourcePlugin extends AbstractDBDataSourcePlugin implements IDBDataSourcePlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(OracleDBDataSourcePlugin.class);

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
     * The mapping between the attributes in the {@link Model} and the data source
     */
    private DataSourceModelMapping dataSourceMapping;

    /**
     * Init method
     */
    @PluginInit
    private void initPlugin() {
        LOG.info("Init method call : " + this.getClass().getName() + "connection=" + this.dbConnection.toString()
                + "model=" + this.modelJSon);

        // Converts the modelJson to a list of DataSourceAttributeMapping
        loadModel();
    }

    /**
     * Converts the mapping between the attribute of the data source and the attributes of the model from a JSon
     * representation to a {@link List} of {@link DataSourceAttributeMapping}.
     */
    private void loadModel() {
        ModelMappingAdapter adapter = new ModelMappingAdapter();
        try {
            dataSourceMapping = adapter.read(new JsonReader(new StringReader(this.modelJSon)));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.AbstractDBDataSourcePlugin#buildSqlGenerator()
     */
    @Override
    protected SqlGenerator buildSqlGenerator() {
        return new OracleSqlGenerator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.utils.AbstractDBDataSourcePlugin#buildSqlGenerator(java.lang.String,
     * java.lang.String)
     */
    @Override
    protected SqlGenerator buildSqlGenerator(String pAllColumnsClause, String pOrderBy) {
        return new OracleSqlGenerator(pAllColumnsClause);
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.AbstractDBDataSourcePlugin#getDBConnectionPlugin()
     */
    @Override
    protected IDBConnectionPlugin getDBConnectionPlugin() {
        return dbConnection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.AbstractDataObjectMapping#getModelMapping()
     */
    @Override
    protected DataSourceModelMapping getModelMapping() {
        return dataSourceMapping;
    }

}
