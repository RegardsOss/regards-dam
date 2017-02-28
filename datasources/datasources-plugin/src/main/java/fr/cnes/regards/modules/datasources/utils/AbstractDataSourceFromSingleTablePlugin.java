/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nurkiewicz.jdbcrepository.TableDescription;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.entities.domain.DataObject;

/**
 * Class AbstractDataSourceFromSingleTablePlugin
 *
 * A {@link Plugin} to discover the tables, columns and indexes of a SQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
public abstract class AbstractDataSourceFromSingleTablePlugin extends AbstractDataObjectMapping
        implements IDataSourceFromSingleTablePlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataSourceFromSingleTablePlugin.class);

    /**
     * The SQL request parameter name
     */
    public static final String REQUEST_PARAM = "requestSQL";

    private static final String COLUMN_NAME = "COLUMN_NAME";

    private static final String DATABASE_ACCESS_ERROR = "Unable to obtain a database connection";

    // private static final String METADATA_TABLE = "TABLE";
    //
    // private static final String TABLE_CAT = "TABLE_CAT";
    //
    // private static final String TABLE_SCHEM = "TABLE_SCHEM";
    //
    // private static final String TABLE_NAME = "TABLE_NAME";
    //
    // private static final String TABLE_TYPE = "TABLE_TYPE";

    // private static final String TYPE_NAME = "TYPE_NAME";
    //
    // private static final String INDEX_NAME = "INDEX_NAME";
    //
    // private static final String REMARKS = "REMARKS";
    //
    // private static final String ASC_OR_DESC = "asc_or_desc";
    //
    // private static final String NON_UNIQUE = "non_unique";

    /**
     * The description of the {@link Table} used by this {@link Plugin} to requests the database.
     */
    private TableDescription tableDescription;

    /**
     *
     */
    private SqlGenerator sqlGenerator;

    protected abstract SqlGenerator buildSqlGenerator();

    protected abstract SqlGenerator buildSqlGenerator(String pAllColumnsClause, String pOrderBy);

    protected abstract IDBConnectionPlugin getDBConnectionPlugin();

    /**
     * This method initialize the {@link SqlGenerator} used to request the database.<br>
     *
     * @param pTable
     *            the table used to requests the database
     * @param pMapping
     *            the mapping between the attributes's model and the attributes of the database
     */
    @Override
    public void initializePluginMapping(String pTable, DataSourceModelMapping pMapping) {

        // reset the number of data element hosted by the datasource
        this.reset();

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

    @Override
    public int getRefreshRate() {
        // in seconds, 30 minutes
        return 1800;
    }

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

}
