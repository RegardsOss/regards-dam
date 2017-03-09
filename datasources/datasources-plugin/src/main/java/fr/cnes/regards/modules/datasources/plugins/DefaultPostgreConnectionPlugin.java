/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDataSourceConnection;

/**
 * A default {@link Plugin} of type {@link IDBConnectionPlugin}.
 *
 * For the test of the connection :
 *
 * @see http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "postgresql-db-connection", author = "CSSI", version = "1.0-SNAPSHOT",
        description = "Connection to a PostgreSql database")
public class DefaultPostgreConnectionPlugin extends AbstractDataSourceConnection implements IDBConnectionPlugin {

    /**
     * The JDBC PostgreSQL driver
     */
    private static final String POSTGRESQL_JDBC_DRIVER = "org.postgresql.Driver";

    /**
     * The user to used for the database connection
     */
    @PluginParameter(name = USER_PARAM)
    private String dbUser;

    /**
     * The user's password to used for the database connection
     */
    @PluginParameter(name = PASSWORD_PARAM)
    private String dbPassword;

    /**
     * The URL to the database's host
     */
    @PluginParameter(name = DB_HOST_PARAM)
    private String dbHost;

    /**
     * The PORT to the database's host
     */
    @PluginParameter(name = DB_PORT_PARAM)
    private String dbPort;

    /**
     * The NAME of the database
     */
    @PluginParameter(name = DB_NAME_PARAM)
    private String dbName;

    /**
     * Maximum number of Connections a pool will maintain at any given time.
     */
    @PluginParameter(name = MAX_POOLSIZE_PARAM)
    private Integer maxPoolSize;

    /**
     * Minimum number of Connections a pool will maintain at any given time.
     */
    @PluginParameter(name = MIN_POOLSIZE_PARAM)
    private Integer minPoolSize;

    /**
     * This class is used to initialize the {@link Plugin}
     */
    @PluginInit
    private void createPoolConnection() {
        createPoolConnection(dbUser, dbPassword, maxPoolSize, minPoolSize);
    }

    @Override
    public String buildUrl() {
        return "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
    }

    @Override
    protected IDBConnectionPlugin getDBConnectionPlugin() {
        return this;
    }

    @Override
    protected String getJdbcDriver() {
        return POSTGRESQL_JDBC_DRIVER;
    }

    @Override
    protected String getSqlRequestTestConnection() {
        return "select 1";
    }

}