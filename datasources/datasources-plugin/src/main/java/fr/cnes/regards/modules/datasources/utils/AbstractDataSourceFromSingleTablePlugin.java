/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nurkiewicz.jdbcrepository.TableDescription;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.datasources.domain.Column;
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

    private Map<String, Column> columnsType;

    /**
     *
     */
    private SqlGenerator sqlGenerator;

    protected abstract SqlGenerator buildSqlGenerator();

    protected abstract SqlGenerator buildSqlGenerator(String pAllColumnsClause, String pOrderBy);

    /**
     * This method initialize the {@link SqlGenerator} used to request the database.<br>
     *
     * @param pTable
     *            the table used to requests the database
     */
    @Override
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

    protected void initDataSourceColumns(IDBConnectionPlugin dbConnection) {

        columnsType = dbConnection.getColumns(tableDescription.getName());

    }

    protected Integer getTypeDs(String colName) {
        String extractColumnName = extractDataSourceColumnName(colName);

        if (LOG.isDebugEnabled()) {
            LOG.debug("******************************************************************");
            LOG.debug("Retrieving type for {}", colName);
            LOG.debug("Extracted type is {}", extractColumnName);
            if (extractColumnName != null) {
                Column col = columnsType.get(extractColumnName);
                if (col != null) {
                    LOG.debug("Column name {} mapped to {} / JAVA {} / SQL {}", extractColumnName, col.getName(),
                              col.getJavaSqlType(), col.getSqlType());
                } else {
                    LOG.debug("No column mapped to {}", extractColumnName);
                }
            }
            LOG.debug("******************************************************************");
        }

        return columnsType.get(extractColumnName).getSqlType();
    }

    protected String extractDataSourceColumnName(String attrDataSourceName) {
        int pos = attrDataSourceName.toLowerCase().lastIndexOf(AS);

        if (pos > 0) {
            String str = attrDataSourceName.substring(pos + AS.length()).trim();
            if (LOG.isDebugEnabled()) {
                LOG.debug("the extracted column name is : <" + str + ">");
            }
            return str;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("the extracted column name is : <" + attrDataSourceName + ">");
            }
            return attrDataSourceName;
        }
    }

    /**
     * Build the SELECT request.</br>
     * Add the key word "%last_modification_date%" in the WHERE clause.
     * @param pPageable
     * @param pDate
     * @return the SELECT request
     */
    protected String getSelectRequest(Pageable pPageable, OffsetDateTime pDate) {
        String selectRequest = sqlGenerator.selectAll(tableDescription, pPageable);

        if ((pDate != null) && !getLastUpdateAttributeName().isEmpty()) {

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
                StringBuffer newRequest = new StringBuffer(selectRequest);
                newRequest.append(WHERE).append(AbstractDataObjectMapping.LAST_MODIFICATION_DATE_KEYWORD);
                selectRequest = newRequest.toString();
            }
        }

        return selectRequest;
    }

    protected String getCountRequest(OffsetDateTime pDate) {
        if ((pDate == null) || getLastUpdateAttributeName().isEmpty()) {
            return sqlGenerator.count(tableDescription);
        } else {
            return sqlGenerator.count(tableDescription) + WHERE
                    + AbstractDataObjectMapping.LAST_MODIFICATION_DATE_KEYWORD;
        }
    }

    @Override
    public Page<DataObject> findAll(String pTenant, Pageable pPageable) {
        return findAll(pTenant, pPageable, null);
    }

    @Override
    public Page<DataObject> findAll(String pTenant, Pageable pPageable, OffsetDateTime pDate) {
        if (sqlGenerator == null) {
            LOG.error("the sqlGenerator is null");
            return null;
        }
        final String selectRequest = getSelectRequest(pPageable, pDate);
        final String countRequest = getCountRequest(pDate);

        try (Connection conn = getDBConnection().getConnection()) {

            Page<DataObject> pages = findAll(pTenant, conn, selectRequest, countRequest, pPageable, pDate);

            return pages;
        } catch (SQLException e) {
            LOG.error("Unable to obtain a database connection.", e);
            return null;
        }

    }

}
