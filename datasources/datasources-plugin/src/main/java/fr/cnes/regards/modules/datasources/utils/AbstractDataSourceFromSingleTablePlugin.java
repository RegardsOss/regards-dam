/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nurkiewicz.jdbcrepository.TableDescription;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.entities.domain.DataObject;

/**
 * A {@link Plugin} to discover the tables and columns of a SQL Database and to retrieve the data elements of a specific
 * table.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define the connection to the Database.
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

    protected static final String AND = " AND ";

    protected static final String COMMA = ", ";

    protected static final String LIMIT = "LIMIT";

    protected static final String ORDER_BY = "ORDER";

    protected static final String SPACE = " ";

    protected static final String WHERE = " WHERE ";

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

    public abstract IDBConnectionPlugin getDBConnection() throws SQLException;

    /**
     * This method initialize the {@link SqlGenerator} used to request the database.<br>
     *
     * @param pTable
     *            the table used to requests the database
     */
    public void initializePluginMapping(String pTable) {

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

    public int getRefreshRate() {
        // in seconds, 30 minutes
        return 1800;
    }

    /**
     * Build the SELECT request.</br>
     * Add the key word "%last_modification_date%" in the WHERE clause.
     *
     * @param pPageable
     *
     * @param pDate
     *
     * @return the SELECT request
     */
    protected String getSelectRequest(Pageable pPageable, LocalDateTime pDate) {
        String selectRequest = sqlGenerator.selectAll(tableDescription, pPageable);

        if (pDate != null) {

            if (selectRequest.contains(WHERE)) {
                // Add at the beginning of the where clause
                int pos = selectRequest.indexOf(WHERE);
                selectRequest = selectRequest.substring(0, pos) + WHERE
                        + AbstractDataObjectMapping.LAST_MODIFICATION_DATE_KEYWORD + AND
                        + selectRequest.substring(pos + WHERE.length(), selectRequest.length());
            } else if (selectRequest.contains(ORDER_BY)) {
                // Add before the order by clause
                int pos = selectRequest.indexOf(ORDER_BY);
                selectRequest = selectRequest.substring(0, pos) + WHERE
                        + AbstractDataObjectMapping.LAST_MODIFICATION_DATE_KEYWORD + SPACE
                        + selectRequest.substring(pos, selectRequest.length());
            } else if (selectRequest.contains(LIMIT)) {
                // Add before the limit clause
                int pos = selectRequest.indexOf(LIMIT);
                selectRequest = selectRequest.substring(0, pos) + WHERE
                        + AbstractDataObjectMapping.LAST_MODIFICATION_DATE_KEYWORD + SPACE
                        + selectRequest.substring(pos, selectRequest.length());
            } else {
                // Add at the end of the request
                selectRequest += WHERE + AbstractDataObjectMapping.LAST_MODIFICATION_DATE_KEYWORD;
            }
        }

        return selectRequest;
    }

    protected String getCountRequest(LocalDateTime pDate) {
        if (pDate == null) {
            return sqlGenerator.count(tableDescription);
        } else {
            return sqlGenerator.count(tableDescription) + WHERE
                    + AbstractDataObjectMapping.LAST_MODIFICATION_DATE_KEYWORD;
        }
    }

    public Page<DataObject> findAll(String pTenant, Pageable pPageable) {
        return findAll(pTenant, pPageable, null);
    }

    public Page<DataObject> findAll(String pTenant, Pageable pPageable, LocalDateTime pDate) {
        if (sqlGenerator == null) {
            LOG.error("the sqlGenerator is null");
            return null;
        }
        final String selectRequest = getSelectRequest(pPageable, pDate);
        final String countRequest = getCountRequest(pDate);

        LOG.debug("select request :" + selectRequest);
        LOG.debug("count  request :" + countRequest);

        try (Connection conn = getDBConnection().getConnection()) {

            Page<DataObject> pages = findAll(pTenant, conn, selectRequest, countRequest, pPageable, pDate);

            conn.close();

            return pages;
        } catch (SQLException e) {
            LOG.error("Unable to obtain a database connection.", e);
            return null;
        }

    }

}
