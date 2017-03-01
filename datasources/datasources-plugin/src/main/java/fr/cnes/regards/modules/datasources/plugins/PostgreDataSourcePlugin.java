/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDataObjectMapping;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * A {@link Plugin} to extract data from a PostgreSQL Database.<br>
 * This {@link Plugin} used a {@link IDBConnectionPlugin} to define to connection to the {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "postgresql-datasource", author = "CSSI", version = "1.0-SNAPSHOT",
        description = "Allows data extraction to a PostgreSql database")
public class PostgreDataSourcePlugin extends AbstractDataObjectMapping implements IDataSourcePlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourcePlugin.class);

    /**
     * The connection to the database
     */
    @PluginParameter(name = CONNECTION_PARAM)
    private IDBConnectionPlugin dbConnection;

    /**
     * The SQL request
     */
    @PluginParameter(name = FROM_CLAUSE)
    private String requestSql;

    /**
     * The {@link Model} to used by the {@link Plugin} in JSon format.
     */
    @PluginParameter(name = MODEL_PARAM)
    private String modelJSon;

    /**
     * Init method
     */
    @PluginInit
    private void initPlugin() {
        LOG.info("Init method call : " + this.getClass().getName() + "connection=" + this.dbConnection.toString()
                + "model=" + this.modelJSon + "requete=" + this.requestSql);

        LOG.info("Init method call : "
                + (this.dbConnection.testConnection() ? "CONNECTION_PARAM IS VALID" : "ERROR CONNECTION_PARAM"));

        // Converts the modelJson to a list of DataSourceAttributeMapping
        initDataSourceMapping(this.modelJSon);
    }

    @Override
    public int getRefreshRate() {
        // in seconds, 30 minutes
        return 1800;
    }

    @Override
    public boolean isOutOfDate() {
        return true;
    }

    @Override
    public Page<DataObject> findAll(String pTenant, Pageable pPageable, LocalDateTime pDate) {
        Connection conn = dbConnection.getConnection();
        if (conn == null) {
            LOG.error("Unable to obtain a database connection.");
            return null;
        }

        Page<DataObject> pages = findAllApplyPageAndDate(pTenant, conn, requestSql, pPageable, pDate);

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

    @Override
    protected AbstractAttribute<?> buildDateAttribute(ResultSet pRs, DataSourceAttributeMapping pAttrMapping)
            throws SQLException {
        LocalDateTime ldt;

        if (pAttrMapping.getTypeDS() == null) {
            ldt = buildLocatDateTime(pRs, pAttrMapping);
        } else {
            long n;
            Instant instant;

            switch (pAttrMapping.getTypeDS()) {
                case Types.TIME:
                    n = pRs.getTime(pAttrMapping.getNameDS()).getTime();
                    instant = Instant.ofEpochMilli(n);
                    ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    break;
                case Types.DATE:
                    n = pRs.getDate(pAttrMapping.getNameDS()).getTime();
                    instant = Instant.ofEpochMilli(n);
                    ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    break;
                default:
                    ldt = buildLocatDateTime(pRs, pAttrMapping);
                    break;
            }
        }

        return AttributeBuilder.buildDate(pAttrMapping.getName(), ldt);
    }

}
