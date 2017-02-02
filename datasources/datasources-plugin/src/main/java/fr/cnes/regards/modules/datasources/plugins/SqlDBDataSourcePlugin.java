/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.domain.Column;
import fr.cnes.regards.modules.datasources.plugins.domain.Index;
import fr.cnes.regards.modules.datasources.plugins.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * Class SqlDBDataSourcePlugin
 *
 * A {@link Plugin} to discover the tables, colums and index of a SQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(author = "CSSI", version = "1.0-SNAPSHOT", description = "Connection to a Elasticsearch engine")
public class SqlDBDataSourcePlugin implements IDBDataSourcePlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(SqlDBDataSourcePlugin.class);

    private static final String METADATA_TABLE = "TABLE";

    private static final String TABLE_CAT = "TABLE_CAT";

    private static final String TABLE_SCHEM = "TABLE_SCHEM";

    private static final String TABLE_NAME = "TABLE_NAME";

    private static final String TABLE_TYPE = "TABLE_TYPE";

    private static final String COLUMN_NAME = "COLUMN_NAME";

    private static final String TYPE_NAME = "TYPE_NAME";

    private static final String INDEX_NAME = "INDEX_NAME";

    private static final String REMARKS = "REMARKS";

    private static final String ASC_OR_DESC = "asc_or_desc";

    private static final String NON_UNIQUE = "non_unique";

    /**
     * The connection to the database
     */
    @PluginParameter(name = CONNECTION_PARAM)
    private IDBConnectionPlugin dbConnection;

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin#getRefreshRate()
     */
    @Override
    public int getRefreshRate() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin#isOutOfDate()
     */
    @Override
    public boolean isOutOfDate() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin#findAll(org.springframework.data.domain.
     * Pageable, java.time.LocalDateTime)
     */
    @Override
    public Page<AbstractEntity> findAll(Pageable pPageable, LocalDateTime pDate) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin#findAll(org.springframework.data.domain.
     * Pageable)
     */
    @Override
    public Page<AbstractEntity> findAll(Pageable pPageable) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin#getTables()
     */
    @Override
    public Map<String, Table> getTables() {
        Map<String, Table> tables = new HashMap<>();

        // Get a connection
        Connection conn = dbConnection.getConnection();
        try {

            DatabaseMetaData metaData = conn.getMetaData();

            ResultSet rs = metaData.getTables(conn.getCatalog(), null, null, new String[] { METADATA_TABLE });

            while (rs.next()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[TABLE] --> " + logString(rs, TABLE_NAME) + "] " + logString(rs, TABLE_CAT)
                            + logString(rs, TABLE_SCHEM) + logString(rs, TABLE_TYPE) + logString(rs, "REMARKS"));
                }
                Table table = new Table(rs.getString(TABLE_NAME), rs.getString(TABLE_CAT), rs.getString(TABLE_SCHEM));
                table.setPkColumn(getPrimaryKey(metaData, rs.getString(TABLE_CAT), rs.getString(TABLE_SCHEM),
                                                rs.getString(TABLE_NAME)));
                tables.put(table.getName(), table);
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }

        return tables;
    }

    private String getPrimaryKey(DatabaseMetaData pMetaData, String pCatalog, String pSchema, String pTable)
            throws SQLException {
        String column = "";
        ResultSet rs = pMetaData.getPrimaryKeys(pCatalog, pSchema, pTable);
        if (rs.next()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(rs.getString("COLUMN_NAME") + ", " + rs.getString("PK_NAME"));
            }
            column = rs.getString("COLUMN_NAME");
        }
        return column;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin#getColumns(java.lang.String)
     */
    @Override
    public Map<String, Column> getColumns(Table pTable) {
        Map<String, Column> columns = new HashMap<>();

        // Get a connection
        Connection conn = dbConnection.getConnection();

        try {
            DatabaseMetaData metaData = conn.getMetaData();

            ResultSet rs = metaData.getColumns(null, null, pTable.getName(), null);

            while (rs.next()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[COLUMN] --> " + logString(rs, "COLUMN_NAME") + logString(rs, "TABLE_CAT")
                            + logString(rs, "TABLE_SCHEM") + logString(rs, "TABLE_NAME") + logString(rs, "TYPE_NAME")
                            + logString(rs, "SCOPE_CATLOG") + logInt(rs, "SQL_DATA_TYPE") + logInt(rs, "DATA_TYPE")
                            + logString(rs, "SCOPE_SCHEMA") + logString(rs, "SCOPE_TABLE"));
                }
                // ,

                Column column = new Column(rs.getString(COLUMN_NAME), rs.getString(TYPE_NAME));
                column.setPrimaryKey(pTable.getPkColumn() != null && !"".equals(pTable.getPkColumn())
                        && pTable.getPkColumn().equals(column.getName()));
                columns.put(column.getName(), column);
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }

        return columns;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin#getIndexes()
     */
    @Override
    public Map<String, Index> getIndexes(Table pTable) {
        Map<String, Index> indexes = new HashMap<>();

        // Get a connection
        Connection conn = dbConnection.getConnection();

        try {
            DatabaseMetaData metaData = conn.getMetaData();

            ResultSet rs = metaData.getIndexInfo(null, null, pTable.getName(), true, false);

            while (rs.next()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[INDEX] --> " + logString(rs, TABLE_NAME) + logString(rs, INDEX_NAME)
                            + logString(rs, COLUMN_NAME) + logBoolean(rs, NON_UNIQUE) + logString(rs, ASC_OR_DESC));
                }
                Index index = new Index(rs.getString(INDEX_NAME), rs.getString(COLUMN_NAME), rs.getBoolean(NON_UNIQUE),
                        rs.getString(INDEX_NAME));
                indexes.put(index.getName(), index);
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }

        return indexes;
    }

    private String logBoolean(ResultSet pRs, String pParamName) throws SQLException {
        if (pRs.getBoolean(pParamName)) {
            return pParamName + "=" + pRs.getBoolean(pParamName) + ",";
        }
        return "";
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

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin#getConfiguredTable()
     */
    @Override
    public Table getConfiguredTable() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin#getConfiguredColumns()
     */
    @Override
    public List<Column> getConfiguredColumns() {
        // TODO Auto-generated method stub
        return null;
    }

}
