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

import com.nurkiewicz.jdbcrepository.TableDescription;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.entities.domain.DataObject;

/**
 * A {@link Plugin} to discover the tables, columns and indexes of a SQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
public abstract class AbstractDataSourceFromSingleTablePlugin extends AbstractDataObjectMapping {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataSourceFromSingleTablePlugin.class);

    /**
     * A comma used to build the select clause
     */
    protected static final String COMMA = ",";

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
    // @Override
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

    // @Override
    public int getRefreshRate() {
        // in seconds, 30 minutes
        return 1800;
    }

    // @Override
    public boolean isOutOfDate() {
        boolean outDated = true;

        // TODO compute the out dated value

        if (outDated) {
            this.reset();
        }

        return outDated;
    }

    protected String getSelectRequest(Pageable pPageable) {
        return sqlGenerator.selectAll(tableDescription, pPageable);
    }

    protected String getCountRequest() {
        return sqlGenerator.count(tableDescription);
    }

    // @Override
    public Page<DataObject> findAll(String pTenant, Pageable pPageable) {
        return findAll(pTenant, pPageable, null);
    }

    // @Override
    public Page<DataObject> findAll(String pTenant, Pageable pPageable, LocalDateTime pDate) {
        if (sqlGenerator == null) {
            LOG.error("the sqlGenerator is null");
            return null;
        }
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

}
