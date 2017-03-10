/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;

/**
 * A class to discover the tables and columns of a SQL Database.</br>
 * This class manage a connection pool to the database.</br>
 * This class used @see http://www.mchange.com/projects/c3p0/index.html.</br>
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
public abstract class AbstractDataSourceConnection {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataSourceConnection.class);

    private static final String METADATA_TABLE = "TABLE";

    private static final String TABLE_CAT = "TABLE_CAT";

    private static final String TABLE_SCHEM = "TABLE_SCHEM";

    private static final String TABLE_NAME = "TABLE_NAME";

    private static final String TABLE_TYPE = "TABLE_TYPE";

    private static final String COLUMN_NAME = "COLUMN_NAME";

    private static final String TYPE_NAME = "TYPE_NAME";

    private static final String REMARKS = "REMARKS";

    /**
     * A {@link ComboPooledDataSource} to used to connect to a data source
     */
    protected ComboPooledDataSource cpds;

    protected abstract IDBConnectionPlugin getDBConnectionPlugin();

    /**
     * The driver used to connect to the database
     * 
     * @return the JDBC driver
     */
    protected abstract String getJdbcDriver();

    /**
     * The SQL request used to test the connection to the database
     * 
     * @return the SQL request
     */
    protected abstract String getSqlRequestTestConnection();

    /**
     * The URL used to connect to the database.</br>
     * Generally this URL look likes : jdbc:xxxxx//host:port/databaseName
     * 
     * @return the database's URL
     */
    protected abstract String buildUrl();

    /**
     * Initialize the {@link ComboPooledDataSource}
     * 
     * @param pUser
     *            The user to used for the database connection
     * @param pPassword
     *            The user's password to used for the database connection
     * @param pMaxPoolSize
     *            Maximum number of Connections a pool will maintain at any given time.
     * @param pMinPoolSize
     *            Minimum number of Connections a pool will maintain at any given time.
     */
    protected void createPoolConnection(String pUser, String pPassword, Integer pMaxPoolSize, Integer pMinPoolSize) {
        cpds = new ComboPooledDataSource();
        cpds.setJdbcUrl(buildUrl());
        cpds.setUser(pUser);
        cpds.setPassword(pPassword);
        cpds.setMaxPoolSize(pMaxPoolSize);
        cpds.setMinPoolSize(pMinPoolSize);

        try {
            cpds.setDriverClass(getJdbcDriver());
        } catch (PropertyVetoException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Test the connection to the database
     * 
     * @return true if the connection is active
     */
    public boolean testConnection() {
        boolean isConnected = false;
        try (Connection conn = cpds.getConnection()) {
            try (Statement statement = conn.createStatement()) {
                // Execute a simple SQL request
                try (ResultSet rs = statement.executeQuery(getSqlRequestTestConnection())) {
                    isConnected = true;
                }
            }
            conn.close();
        } catch (SQLException e) {
            LOG.error("Unable to connect to the database", e);
        }
        return isConnected;
    }

    /**
     * Get a {@link Connection} to the database
     * 
     * @return the {@link Connection}
     * @throws SQLException 
     * 
     */
    public Connection getConnection() throws SQLException {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Returns all the table from the database.
     * 
     * @return a {@link Map} of {@link Table}
     */
    public Map<String, Table> getTables() {
        Map<String, Table> tables = new HashMap<>();
        ResultSet rs = null;

        // Get a connection
        try (Connection conn = getDBConnectionPlugin().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            rs = metaData.getTables(conn.getCatalog(), null, null, new String[] { METADATA_TABLE });

            while (rs.next()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[TABLE] --> " + logString(rs, TABLE_NAME) + "] " + logString(rs, TABLE_CAT)
                            + logString(rs, TABLE_SCHEM) + logString(rs, TABLE_TYPE) + logString(rs, REMARKS));
                }
                Table table = new Table(rs.getString(TABLE_NAME), rs.getString(TABLE_CAT), rs.getString(TABLE_SCHEM));
                table.setPKey(getPrimaryKey(metaData, rs.getString(TABLE_CAT), rs.getString(TABLE_SCHEM),
                                            rs.getString(TABLE_NAME)));
                tables.put(table.getName(), table);
            }
            conn.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }

        return tables;
    }

    /**
     * Get the primary key name of a database's table
     * 
     * @param pMetaData
     *            The {@link DatabaseMetaData} of the database
     * @param pCatalog
     *            The catalog name
     * @param pSchema
     *            The database's schema
     * @param pTable
     *            The table name
     * @return the primary key name
     * @throws SQLException
     *             an SQL error occurred
     */
    private String getPrimaryKey(DatabaseMetaData pMetaData, String pCatalog, String pSchema, String pTable)
            throws SQLException {
        String column = "";
        ResultSet rs = pMetaData.getPrimaryKeys(pCatalog, pSchema, pTable);
        if (rs.next()) {
            column = rs.getString(COLUMN_NAME);
            if (LOG.isDebugEnabled()) {
                LOG.debug("[PKEY] --> " + column + ", " + rs.getString("PK_NAME"));
            }
        }
        return column;
    }

    /**
     * Get the columns of a {@link Table} from the database
     * 
     * @param pTableName
     *            a table of the database
     * @return a {@link Map} of {@link Column}
     */
    public Map<String, Column> getColumns(String pTableName) {
        Map<String, Column> cols = new HashMap<>();

        // Get a connection
        try (Connection conn = getDBConnectionPlugin().getConnection()) {

            DatabaseMetaData metaData = conn.getMetaData();

            try (ResultSet rs = metaData.getColumns(null, null, pTableName, null)) {

                while (rs.next()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[COLUMN] --> " + logString(rs, "COLUMN_NAME") + logString(rs, "TYPE_NAME")
                                + logInt(rs, "DATA_TYPE"));
                    }

                    Column column = new Column(rs.getString(COLUMN_NAME), rs.getString(TYPE_NAME));
                    cols.put(column.getName(), column);
                }
            }

            conn.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return cols;
    }

    private String logString(ResultSet pRs, String pParamName) throws SQLException {
        if (pRs.getString(pParamName) != null) {
            return pParamName + "=" + pRs.getString(pParamName) + ",";
        }
        return "";
    }

    private String logInt(ResultSet pRs, String pParamName) throws SQLException {
        return pParamName + "=" + pRs.getInt(pParamName) + ",";
    }

}
