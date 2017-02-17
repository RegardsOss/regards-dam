/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nurkiewicz.jdbcrepository.TableDescription;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.entities.domain.DataObject;

/**
 * Class AbstractDataSourceFromSingleTablePlugin
 *
 * A {@link Plugin} to discover the tables, columns and indices of a SQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
public abstract class AbstractDataSourceFromSingleTablePlugin extends AbstractDataObjectMapping implements IDataSourceFromSingleTablePlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataSourceFromSingleTablePlugin.class);

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

    private static final String INDEX_NAME = "INDEX_NAME";

    private static final String REMARKS = "REMARKS";

    private static final String ASC_OR_DESC = "asc_or_desc";

    private static final String NON_UNIQUE = "non_unique";

    private static final String COMMA = ",";

    private static final String DATABASE_ACCESS_ERROR = "Unable to obtain a database connection";

    /**
     * The description of the {@link Table} used by this {@link Plugin} to requests the database.
     */
    private TableDescription tableDescription;

    /**
     * The {@link List} of columns used by this {@link Plugin} to requests the database. This columns are in the
     * {@link Table}.
     */
    private List<String> columns;

    /**
     * The column name used in the ORDER BY clause
     */
    private String orderByColumn = "";

    /**
     *
     */
    private SqlGenerator sqlGenerator;

    protected abstract SqlGenerator buildSqlGenerator();

    protected abstract SqlGenerator buildSqlGenerator(String pAllColumnsClause, String pOrderBy);

    protected abstract IDBConnectionPlugin getDBConnectionPlugin();

    /**
     * This method initialize the mapping used to request the database.<br>
     *
     * @param pTable
     *            the table used to requests the database
     * @param pMapping
     *            the mapping between the attributes's model and the attributes of the database
     */
    @Override
    public void setMapping(String pTable, DataSourceModelMapping pMapping) {

        // reset the number of data element hosted by the datasource
        this.reset();

        if (columns == null) {
            columns = new ArrayList<>();
        }

        pMapping.getAttributesMapping().forEach(d -> {
            columns.add(d.getNameDS());
            if (d.isPrimaryKey()) {
                orderByColumn = d.getNameDS();
            }
        });

        tableDescription = new TableDescription(pTable, null, columns.toArray(new String[0]));
        if (columns.isEmpty()) {
            sqlGenerator = buildSqlGenerator();
        } else {
            if ("".equals(orderByColumn)) {
                orderByColumn = columns.get(0);
            }
            sqlGenerator = buildSqlGenerator(buildColumnClause(columns.toArray(new String[0])), orderByColumn);
        }
    }

    private String buildColumnClause(String... pColumns) {
        StringBuilder clauseStr = new StringBuilder();
        for (String col : pColumns) {
            clauseStr.append(col + COMMA);
        }
        return clauseStr.substring(0, clauseStr.length() - 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin#getRefreshRate()
     */
    @Override
    public int getRefreshRate() {
        // in seconds, 30 minutes
        return 1800;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin#isOutOfDate()
     */
    @Override
    public boolean isOutOfDate() {
        boolean outDated = true;

        // TODO compute the out dated value

        if (isOutOfDate()) {
            this.reset();
        }

        return outDated;
    }

    @Override
    public Page<DataObject> findAll(String pTenant, Pageable pPageable, LocalDateTime pDate) {
        if (sqlGenerator == null) {
            return null;
        }

        String requestSql = sqlGenerator.selectAll(tableDescription, pPageable);
        String countRequestSql = sqlGenerator.count(tableDescription);

        LOG.debug("request :" + requestSql);

        // Get a connection
        Connection conn = getDBConnectionPlugin().getConnection();

        if (conn == null) {
            LOG.error(DATABASE_ACCESS_ERROR);
            return null;
        }

        Page<DataObject> pages = findAll(pTenant, conn, requestSql, countRequestSql, pPageable, pDate);

        try {
            conn.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }

        return pages;
    }

    @Override
    public Page<DataObject> findAll(String pTenant, Pageable pPageable) {
        return findAll(pTenant, pPageable, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin#getTables()
     */
    @Override
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
                table.setPkColumn(getPrimaryKey(metaData, rs.getString(TABLE_CAT), rs.getString(TABLE_SCHEM),
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("[PKEY] --> " + rs.getString(COLUMN_NAME) + ", " + rs.getString("PK_NAME"));
            }
            column = rs.getString(COLUMN_NAME);
        }
        return column;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin#getColumns(java.lang.String)
     */
    @Override
    public Map<String, Column> getColumns(Table pTable) {
        Map<String, Column> cols = new HashMap<>();
        ResultSet rs = null;

        // Get a connection
        Connection conn = getDBConnectionPlugin().getConnection();

        if (conn == null) {
            LOG.error(DATABASE_ACCESS_ERROR);
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
                column.setPrimaryKey((pTable.getPkColumn() != null) && !"".equals(pTable.getPkColumn())
                        && pTable.getPkColumn().equals(column.getName()));
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

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin#getIndices()
     */
    @Override
    public Map<String, Index> getIndices(Table pTable) {
        Map<String, Index> indices = new HashMap<>();
        ResultSet rs = null;

        // Get a connection
        Connection conn = getDBConnectionPlugin().getConnection();

        if (conn == null) {
            LOG.error(DATABASE_ACCESS_ERROR);
            return null;
        }

        try {
            DatabaseMetaData metaData = conn.getMetaData();

            rs = metaData.getIndexInfo(null, null, pTable.getName(), true, false);

            while (rs.next()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[INDEX] --> " + logString(rs, TABLE_NAME) + logString(rs, INDEX_NAME)
                            + logString(rs, COLUMN_NAME) + logBoolean(rs, NON_UNIQUE) + logString(rs, ASC_OR_DESC));
                }
                Index index = new Index(rs.getString(INDEX_NAME), rs.getString(COLUMN_NAME), rs.getBoolean(NON_UNIQUE),
                        rs.getString(INDEX_NAME));
                indices.put(index.getName(), index);
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

        return indices;
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
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin#getConfiguredTable()
     */
    @Override
    public String getConfiguredTable() {
        return tableDescription.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin#getConfiguredColumns()
     */
    @Override
    public List<String> getConfiguredColumns() {
        return columns;
    }

}
