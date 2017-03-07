/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.entities.domain.DataObject;

/**
 * A {@link Plugin} to discover the tables, columns and indexes of a SQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
public abstract class AbstractDataSourcePlugin extends AbstractDataObjectMapping {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataSourcePlugin.class);

    /**
     * The string used to add the pagination information in PostGreSql
     */
    protected static final String LIMIT_CLAUSE = " ORDER BY %s LIMIT %d OFFSET %d";

    /**
     * The PL/SQL key word SELECT
     */
    protected static final String SELECT = "SELECT ";

    /**
     * The PL/SQL expression SELECT COUNt(*)
     */
    protected static final String SELECT_COUNT = "SELECT COUNT(*) ";

    /**
     * The PL/SQL key word AS
     */
    protected static final String AS = "as";

    /**
     * A comma used to build the select clause
     */
    protected static final String COMMA = ",";

    protected abstract IDBConnectionPlugin getDBConnectionPlugin();

    public int getRefreshRate() {
        // in seconds, 30 minutes
        return 1800;
    }

    public boolean isOutOfDate() {
        boolean outDated = true;

        // TODO compute the out dated value

        if (outDated) {
            this.reset();
        }

        return outDated;
    }

    protected abstract String getFromClause();

    protected String getSelectRequest(Pageable pPageable) {
        return SELECT + buildColumnClause(columns.toArray(new String[0])) + getFromClause() + buildLimitPart(pPageable);
    }

    protected String getCountRequest() {
        return SELECT_COUNT + getFromClause();
    }

    /**
     * 
     * @param pTenant
     * @param pPageable
     * @return
     */
    public Page<DataObject> findAll(String pTenant, Pageable pPageable) {
        return findAll(pTenant, pPageable, null);
    }

    /**
     * 
     * @param pTenant
     * @param pPageable
     * @param pDate
     * @return
     */
    public Page<DataObject> findAll(String pTenant, Pageable pPageable, LocalDateTime pDate) {
        final String selectRequest = getSelectRequest(pPageable);
        final String countRequest = getCountRequest();

        LOG.debug("select request :" + selectRequest);
        LOG.debug("count request :" + countRequest);

        try (Connection conn = getDBConnectionPlugin().getConnection()) {

            Page<DataObject> pages = findAll(pTenant, conn, selectRequest, countRequest, pPageable, pDate);

            conn.close();

            return pages;
        } catch (SQLException e) {
            LOG.error("Unable to obtain a database connection.", e);
            return null;
        }
    }

    /**
     * Add to the SQL request the part to fetch only a portion of the results.
     * 
     * @param pPage
     *            the page of the element to fetch
     * @return the SQL request
     */
    protected String buildLimitPart(Pageable pPage) {
        StringBuilder str = new StringBuilder(" ");
        final int offset = pPage.getPageNumber() * pPage.getPageSize();
        final String limit = String.format(LIMIT_CLAUSE, orderByColumn, pPage.getPageSize(), offset);
        str.append(limit);

        return str.toString();
    }
}
