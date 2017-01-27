/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.domain.AttributeMappingAdapter;
import fr.cnes.regards.modules.datasources.plugins.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.plugintypes.IConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.plugintypes.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.plugintypes.IDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class DefaultESConnectionPlugin
 *
 * A default {@link Plugin} of type {@link IConnectionPlugin}. Allows to
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(author = "CSSI", version = "1.0-SNAPSHOT", description = "Connection to a Elasticsearch engine")
public class PostgreDataSourcePlugin implements IDataSourcePlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourcePlugin.class);

    /**
     * The SQL request parameter name
     */
    public static final String REQUEST_PARAM = "requestSQL";

    /**
     * The string LIMIT used to add the pagination information in the SQL request
     */
    private static final String LIMIT = "LIMIT";

    /**
     * The string OFFSET used to add the pagination information in the SQL request
     */
    private static final String OFFSET = "OFFSET";

    /**
     * A space used to add the pagination information in the SQL request
     */
    private static final String SPACE = " ";

    /**
     * A pattern used to set a date in the statement
     */
    private static final String DATE_STATEMENT = "%last_modification_date%";

    /**
     * A default date
     */
    private static final LocalDateTime INIT_DATE = LocalDateTime.of(1, 1, 1, 0, 0);

    /**
     * The connection to the database
     */
    @PluginParameter(name = CONNECTION_PARAM)
    private IDBConnectionPlugin dbConnection;

    /**
     * The SQL request
     */
    @PluginParameter(name = REQUEST_PARAM)
    private String requestSql;

    /**
     * THe {@link Model} to used by the {@link Plugin} in JSon format.
     */
    @PluginParameter(name = MODEL_PARAM)
    private String modelJSon;

    /**
     * The mapping between the attributes in the {@link Model} and the data source
     */
    private List<DataSourceAttributeMapping> attributesMapping;

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.plugintypes.IDataSourcePlugin#getRefreshRate()
     */
    @Override
    public int getRefreshRate() {
        // in seconds, 30 minutes
        return 1800;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.plugintypes.IDataSourcePlugin#isOutOfDate()
     */
    @Override
    public boolean isOutOfDate() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.cnes.regards.modules.datasources.plugins.plugintypes.IDataSourcePlugin#getNewData(org.springframework.data.
     * domain.Pageable)
     */
    @Override
    public Page<AbstractEntity> getNewData(Pageable pPageable, LocalDateTime pDate) {
        List<DataObject> dataObjects = new ArrayList<>();

        // Get a connection
        Connection conn = dbConnection.getConnection();

        try {
            Statement statement = conn.createStatement();

            // Execute SQL request
            String sqlWithDateStatement = addDateStatement(requestSql, pDate);

            String sqlRequestWithPagedInformation = buildLimitPart(sqlWithDateStatement, pPageable);

            ResultSet rs = statement.executeQuery(sqlRequestWithPagedInformation);

            while (rs.next()) {
                dataObjects.add(processResultSet(rs));
            }

            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }

        return new PageImpl(dataObjects, pPageable, 10);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.cnes.regards.modules.datasources.plugins.plugintypes.IDataSourcePlugin#findAll(org.springframework.data.domain
     * .Pageable)
     */
    @Override
    public Page<AbstractEntity> findAll(Pageable pPageable) {
        List<DataObject> dataObjects = new ArrayList<>();

        // Get a connection
        Connection conn = dbConnection.getConnection();

        try {
            Statement statement = conn.createStatement();

            // Execute SQL request
            String sqlWithDateStatement = noDateStatement(requestSql);

            String sqlRequestWithPagedInformation = buildLimitPart(sqlWithDateStatement, pPageable);

            ResultSet rs = statement.executeQuery(sqlRequestWithPagedInformation);

            while (rs.next()) {
                dataObjects.add(processResultSet(rs));
            }

            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }

        return new PageImpl(dataObjects, pPageable, 10);
    }

    /**
     * Build a {@link DataObject} for a {@link ResultSet}.
     * 
     * @param pRs
     *            the {@link ResultSet}
     * @return the {@link DataObject} created
     */
    private DataObject processResultSet(ResultSet pRs) {
        final DataObject data = new DataObject();
        final List<AbstractAttribute<?>> attributes = new ArrayList<>();
        final Map<String, List<AbstractAttribute<?>>> spaceNames = Maps.newHashMap();

        /**
         * Loop the attributes in the mapping
         */
        for (DataSourceAttributeMapping attrMapping : attributesMapping) {
            final boolean asNameSpace = attrMapping.getNameSpace() != null;
            try {
                AbstractAttribute<?> attr = buildAttribute(pRs, attrMapping);

                if (attr != null) {
                    if (asNameSpace) {
                        /**
                         * The attribute has a name space
                         */
                        if (spaceNames.containsKey(attrMapping.getNameSpace())) {
                            /**
                             * The name space already exists
                             */
                            spaceNames.get(attrMapping.getNameSpace()).add(attr);
                        } else {
                            /**
                             * It is a new name space
                             */
                            final List<AbstractAttribute<?>> nameSpaceAttributes = new ArrayList<>();
                            nameSpaceAttributes.add(attr);
                            spaceNames.put(attrMapping.getNameSpace(), nameSpaceAttributes);
                        }
                    } else {
                        attributes.add(attr);
                    }
                }
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }

        }

        /**
         * For each name space, add an ObjectAttribute to the list of attribute
         */
        spaceNames.forEach((pName, pAttrs) -> {
            attributes
                    .add(AttributeBuilder.buildObject(pName, pAttrs.toArray(new AbstractAttribute<?>[pAttrs.size()])));
        });

        data.setAttributes(attributes);

        return data;
    }

    /**
     * Get an attribute define in the mapping in a {@link ResultSet}
     * 
     * @param pRs
     *            the {@link ResultSet}
     * @param pAttrMapping
     *            the {@link DataSourceAttributeMapping}
     * @return a new {@link AbstractAttribute}
     * @throws SQLException
     *             if an error occurs in the {@link ResultSet}
     */
    private AbstractAttribute<?> buildAttribute(ResultSet pRs, DataSourceAttributeMapping pAttrMapping)
            throws SQLException {
        AbstractAttribute<?> attr = null;

        switch (pAttrMapping.getType()) {
            case STRING:
                attr = AttributeBuilder.buildString(pAttrMapping.getName(), pRs.getString(pAttrMapping.getMapping()));
                break;
            case INTEGER:
                attr = AttributeBuilder.buildInteger(pAttrMapping.getName(), pRs.getInt(pAttrMapping.getMapping()));
                break;
            case BOOLEAN:
                attr = AttributeBuilder.buildBoolean(pAttrMapping.getName(), pRs.getBoolean(pAttrMapping.getMapping()));
                break;
            case DOUBLE:
                attr = AttributeBuilder.buildDouble(pAttrMapping.getName(), pRs.getDouble(pAttrMapping.getMapping()));
                break;
            case DATE_ISO8601:
                Timestamp date = pRs.getTimestamp(pAttrMapping.getMapping());
                Instant instant = Instant.ofEpochMilli(date.getTime());
                attr = AttributeBuilder.buildDate(pAttrMapping.getName(),
                                                  LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                break;
            default:
        }

        return attr;
    }

    /**
     * Add the elements to the request to fetch only a portion of the results
     * 
     * @param pRequest
     *            the SQL request
     * @param pPage
     *            the page of the element to fetch
     * @return the SQL request
     */
    private String buildLimitPart(String pRequest, Pageable pPage) {
        if (pPage == null) {
            // Skip
            return pRequest;
        }
        StringBuilder str = new StringBuilder(pRequest);
        final int offset = pPage.getPageNumber() * pPage.getPageSize();
        str.append(SPACE.concat(LIMIT).concat(SPACE).concat(String.valueOf(pPage.getPageSize())));
        str.append(SPACE.concat(OFFSET).concat(SPACE).concat(String.valueOf(offset)));

        return str.toString();
    }

    private void loadModel() {
        AttributeMappingAdapter adapter = new AttributeMappingAdapter();
        try {
            attributesMapping = adapter.read(new JsonReader(new StringReader(this.modelJSon)));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    String noDateStatement(String pRequest) {
        return pRequest.replaceAll(DATE_STATEMENT, INIT_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    String addDateStatement(String pRequest, LocalDateTime pDate) {
        return pRequest.replaceAll(DATE_STATEMENT, pDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Init method
     */
    @PluginInit
    private void aInit() {
        LOG.info("Init method call : " + this.getClass().getName() + "connection=" + this.dbConnection.toString()
                + "model=" + this.modelJSon + "requete=" + this.requestSql);

        LOG.info("Init method call : "
                + (this.dbConnection.testConnection() ? "CONNECTION_PARAM IS VALID" : "ERROR CONNECTION_PARAM"));

        loadModel();
    }

}
