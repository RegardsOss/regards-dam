/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;

/**
 * Class AbstractDataSourceIntrospection
 *
 * A {@link Plugin} to discover the tables and columnsof a SQL Database.<br>
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
public abstract class AbstractDataSourceIntrospection {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataSourceIntrospection.class);

    /**
     * The SQL request parameter name
     */
    public static final String REQUEST_PARAM = "requestSQL";

    private static final String METADATA_TABLE = "TABLE";

    private static final String TABLE_CAT = "TABLE_CAT";

    private static final String TABLE_SCHEM = "TABLE_SCHEM";

    private static final String TABLE_NAME = "TABLE_NAME";

    private static final String TABLE_TYPE = "TABLE_TYPE";

    private static final String COLUMN_NAME = "COLUMN_NAME";

    private static final String TYPE_NAME = "TYPE_NAME";

    private static final String REMARKS = "REMARKS";

    protected abstract IDBConnectionPlugin getDBConnectionPlugin();

    /**
     * Returns all the table from the database.
     * 
     * @return a {@link Map} of {@link Table}
     */
    public Map<String, Table> getTables() {
        Map<String, Table> tables = new HashMap<>();
        ResultSet rs = null;

        // Get a connection
        Connection conn = getDBConnectionPlugin().getConnection();
        try {
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
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                conn.close();
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        return tables;
    }

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
     * Get the columns of a {@link Table} from the database.
     * 
     * @param pTable
     *            a table of the database
     * @return a {@link Map} of {@link Column}
     */
    public Map<String, Column> getColumns(Table pTable) {
        Map<String, Column> cols = new HashMap<>();
        ResultSet rs = null;

        // Get a connection
        Connection conn = getDBConnectionPlugin().getConnection();

        if (conn == null) {
            LOG.error("Unable to obtain a database connection");
            return null;
        }

        try {
            DatabaseMetaData metaData = conn.getMetaData();

            rs = metaData.getColumns(null, null, pTable.getName(), null);

            while (rs.next()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[COLUMN] --> " + logString(rs, "COLUMN_NAME") + logString(rs, "TYPE_NAME")
                            + logInt(rs, "DATA_TYPE"));
                }

                Column column = new Column(rs.getString(COLUMN_NAME), rs.getString(TYPE_NAME));
                column.setPrimaryKey((pTable.getPKey() != null) && !"".equals(pTable.getPKey())
                        && pTable.getPKey().equals(column.getName()));
                cols.put(column.getName(), column);
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                conn.close();
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
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
