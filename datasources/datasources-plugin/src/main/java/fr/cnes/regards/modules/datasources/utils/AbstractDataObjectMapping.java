/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.ModelMappingAdapter;
import fr.cnes.regards.modules.entities.domain.converter.GeometryAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.entities.domain.DataFile;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.DataType;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * This class allows to process a SQL request to a SQL Database.</br>
 * For each data reads in the Database, a {@link DataObject} is created. This {@link DataObject} are compliant with a
 * {@link Model}.</br>
 * Some attributes extracts from the Database are specials. For each one, a {@link DataObject} property is set :
 * <li>the primary key of the data
 * <li>the data file of the data
 * <li>the thumbnail of the data
 * <li>the update date of the data
 *
 * @author Christophe Mertz
 */
public abstract class AbstractDataObjectMapping {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataObjectMapping.class);

    /**
     * The PL/SQL key word AS
     */
    private static final String AS = "as";

    /**
     * A comma used to build the select clause
     */
    private static final String COMMA = ",";

    /**
     * A default date
     */
    private static final LocalDateTime INIT_DATE = LocalDateTime.of(1, 1, 1, 0, 0);

    private static final GeometryAdapter<?> GEOMETRY_ADAPTER = new GeometryAdapter<>();

    /**
     * A default value to indicates that the count request should be execute
     */
    private static final int RESET_COUNT = -1;

    /**
     * A pattern used to set a date in the statement
     */
    protected static final String LAST_MODIFICATION_DATE_KEYWORD = "%last_modification_date%";

    /**
     * The {@link List} of columns used by this {@link Plugin} to requests the database. This columns are in the
     * {@link Table}.
     */
    protected List<String> columns;

    /**
     * The column name used in the ORDER BY clause
     */
    protected String orderByColumn = "";

    /**
     * The mapping between the attributes in the {@link Model} and the data source
     */
    protected DataSourceModelMapping dataSourceMapping;

    /**
     * The result of the count request
     */
    private int nbItems = RESET_COUNT;

    /**
     * The attribute name used for the date comparison
     */
    private String dateAttributeName = "";

    /**
     * For each attributes of the data source, this map contains an optional internal attribute type
     */
    private final Map<String, InternalAttributes> mappingInternalAttributes = new HashMap<>();

    /**
     * Get {@link DateAttribute}.
     *
     * @param pRs the {@link ResultSet}
     * @param pAttrMapping the {@link AbstractAttributeMapping}
     * @return a new {@link DateAttribute}
     * @throws SQLException if an error occurs in the {@link ResultSet}
     */
    protected abstract AbstractAttribute<?> buildDateAttribute(ResultSet pRs, AbstractAttributeMapping pAttrMapping)
            throws SQLException;

    /**
     * Get a {@link LocalDateTime} value from a {@link ResultSet} for a {@link AbstractAttributeMapping}.
     *
     * @param pRs The {@link ResultSet} to read
     * @param pAttrMapping The {@link AbstractAttributeMapping}
     * @return the {@link LocalDateTime}
     * @throws SQLException An error occurred when try to read the {@link ResultSet}
     */
    protected LocalDateTime buildLocatDateTime(ResultSet pRs, AbstractAttributeMapping pAttrMapping)
            throws SQLException {
        long n = pRs.getTimestamp(pAttrMapping.getNameDS()).getTime();
        Instant instant = Instant.ofEpochMilli(n);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * Returns a page of DataObject from the database defined by the {@link Connection} and corresponding to the SQL. A
     * {@link Date} is apply to filter the {@link DataObject} created or updated after this {@link Date}.
     *
     * @param pTenant the tenant name
     * @param pConn a {@link Connection} to a database
     * @param pSelectRequest the SQL request
     * @param pCountRequest the SQL count request
     * @param pPageable the page information
     * @param pDate a {@link Date} used to apply returns the {@link DataObject} update or create after this date
     * @return a page of {@link DataObject}
     */
    protected Page<DataObject> findAll(String pTenant, Connection pConn, String pSelectRequest, String pCountRequest,
            Pageable pPageable, LocalDateTime pDate) {
        List<DataObject> dataObjects = new ArrayList<>();

        try (Statement statement = pConn.createStatement()) {

            String selectRequest = pSelectRequest;
            String countRequest = pCountRequest;

            // for each attributes to read, defines the REGARDS internal attributes corresponding
            initMappingInternalAttributes();

            if (pDate != null) {
                selectRequest = buildDateStatement(selectRequest, pDate);
                countRequest = buildDateStatement(countRequest, pDate);
            }
            LOG.debug("select request : " + selectRequest);
            LOG.debug("count request : " + countRequest);

            // Execute the request to get the elements
            try (ResultSet rs = statement.executeQuery(selectRequest)) {
                while (rs.next()) {
                    dataObjects.add(processResultSet(pTenant, rs));
                }
            }

            countItems(statement, countRequest);

            statement.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }

        return new PageImpl<>(dataObjects, pPageable, nbItems);
    }

    /**
     * Execute a SQL request to count the number of items
     *
     * @param pStatement a {@link Statement} used to execute the SQL request
     * @param pCountRequest the SQL count request
     * @throws SQLException an SQL error occurred
     */
    private void countItems(Statement pStatement, String pCountRequest) throws SQLException {
        if ((pCountRequest != null) && !pCountRequest.isEmpty() && (nbItems == RESET_COUNT)) {
            // Execute the request to count the elements
            try (ResultSet rsCount = pStatement.executeQuery(pCountRequest)) {
                if (rsCount.next()) {
                    nbItems = rsCount.getInt(1);
                }
            }
        }
    }

    /**
     * Build a {@link DataObject} for a {@link ResultSet}.
     *
     * @param pTenant the tenant name
     * @param pRs the {@link ResultSet}
     * @return the {@link DataObject} created
     * @throws SQLException An SQL error occurred
     */
    protected DataObject processResultSet(String pTenant, ResultSet pRs) throws SQLException {
        final DataObject data = new DataObject();

        final Set<AbstractAttribute<?>> attributes = new HashSet<>();
        final Map<String, List<AbstractAttribute<?>>> spaceNames = Maps.newHashMap();

        /**
         * Loop the attributes in the mapping
         */
        for (AbstractAttributeMapping attrMapping : dataSourceMapping.getAttributesMapping()) {

            try {
                AbstractAttribute<?> attr = buildAttribute(pRs, attrMapping);

                if (attr != null) {
                    // dynamic mapping
                    if (!attrMapping.isMappedToStaticProperty()) {
                        if (attrMapping.getNameSpace() != null) {
                            if (!spaceNames.containsKey(attrMapping.getNameSpace())) {
                                // It is a new name space
                                spaceNames.put(attrMapping.getNameSpace(), new ArrayList<>());
                            }
                            // Add the attribute to the namespace
                            spaceNames.get(attrMapping.getNameSpace()).add(attr);
                        } else {
                            attributes.add(attr);
                        }
                    } else { // static mapping
                        processStaticAttributes(data, attr, attrMapping);
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

        data.setProperties(attributes);

        return data;
    }

    /**
     * Get an attribute define in the mapping in a {@link ResultSet}
     *
     * @param pRs the {@link ResultSet}
     * @param pAttrMapping the {@link AbstractAttributeMapping}
     * @return a new {@link AbstractAttribute}
     * @throws SQLException if an error occurs in the {@link ResultSet}
     */
    private AbstractAttribute<?> buildAttribute(ResultSet pRs, AbstractAttributeMapping pAttrMapping)
            throws SQLException {
        AbstractAttribute<?> attr = null;
        final String label = extractColumnName(pAttrMapping.getNameDS());

        switch (pAttrMapping.getType()) {
            case STRING:
                attr = AttributeBuilder.buildString(pAttrMapping.getName(), pRs.getString(label));
                break;
            case LONG:
                attr = AttributeBuilder.buildLong(pAttrMapping.getName(), pRs.getLong(label));
                break;
            case INTEGER:
                attr = AttributeBuilder.buildInteger(pAttrMapping.getName(), pRs.getInt(label));
                break;
            case BOOLEAN:
                attr = AttributeBuilder.buildBoolean(pAttrMapping.getName(), pRs.getBoolean(label));
                break;
            case DOUBLE:
                attr = AttributeBuilder.buildDouble(pAttrMapping.getName(), pRs.getDouble(label));
                break;
            case DATE_ISO8601:
                attr = buildDateAttribute(pRs, pAttrMapping);
                break;
            default:
                break;
        }

        // If value was null => no attribute value
        if (pRs.wasNull()) {
            return null;
        }

        if (LOG.isDebugEnabled() && (attr != null)) {
            if ((pAttrMapping.getName() != null) && pAttrMapping.getName().equals(pAttrMapping.getNameDS())) {
                LOG.debug("the value for <" + pAttrMapping.getName() + "> of type <" + pAttrMapping.getType() + "> is :"
                                  + attr.getValue());

            } else {
                LOG.debug("the value for <" + pAttrMapping.getName() + "|" + pAttrMapping.getNameDS() + "> of type <"
                                  + pAttrMapping.getType() + "> is :" + attr.getValue());
            }
        }

        return attr;
    }

    /**
     * Extracts a column label from a PL/SQL expression.</br>
     * The column label is placed after the word 'AS'.
     *
     * @param pAttrMapping The PL/SQL expression to analyze
     * @return the column label extracted from the PL/SQL
     */
    private String extractColumnName(String pAttrMapping) {
        int pos = pAttrMapping.toLowerCase().lastIndexOf(AS);

        if (pos > 0) {
            String str = pAttrMapping.substring(pos + AS.length()).trim();
            if (LOG.isDebugEnabled()) {
                LOG.debug("the column label extracted is :<" + str + ">");
            }
            return str;
        } else {
            return pAttrMapping;
        }
    }

    /**
     * This class extracts data information from an attribute and sets this informations into the
     * {@link DataObject}.</br>
     * The REGARDS internal attributes's that are analyzed :
     * <li>primary key
     * <li>raw data
     * <li>thumbnail
     * <li>label
     * <li>description
     *
     * @param pData the current {@link DataObject} to build
     * @param pAttr the current {@link AbstractAttribute} to analyze
     * @param pAttrMapping the {@link AbstractAttributeMapping} for the current attribute
     */
    private void processStaticAttributes(DataObject pData, AbstractAttribute<?> pAttr,
            AbstractAttributeMapping pAttrMapping) {
        if (pAttrMapping.isPrimaryKey()) {
            String val = pAttr.getValue().toString();
            pData.setSipId(val);
        }
        if (pAttrMapping.isRawData() || pAttrMapping.isThumbnail()) {
            String str = ((StringAttribute) pAttr).getValue();
            if (pData.getFiles() == null) {
                pData.setFiles(new ArrayList<>());
            }
            try {
                DataType type = pAttrMapping.isRawData() ? DataType.RAWDATA : DataType.THUMBNAIL;
                DataFile dataFile = new DataFile();
                dataFile.setDataType(type);
                dataFile.setFileRef(new URI(str));
                pData.getFiles().add(dataFile);
            } catch (URISyntaxException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (pAttrMapping.isLastUpdate()) {
            pData.setLastUpdate((LocalDateTime) pAttr.getValue());
        }
        if (pAttrMapping.isLabel()) {
            pData.setLabel(((StringAttribute) pAttr).getValue());
        }
        if (pAttrMapping.isGeometry()) {
            String str = ((StringAttribute) pAttr).getValue();
            try {
                pData.setGeometry(GEOMETRY_ADAPTER.read(new JsonReader(new StringReader(str))));
            } catch (IOException ioe) {
                LOG.error("Unable to deserialize geometry : " + str, ioe);
            }
        }
    }

    /**
     * Build the select clause with the {@link List} of columns used for the mapping.
     *
     * @param pColumns the comulns used for the mapping
     * @return a {@link String} withe the columns separated by a comma
     */
    protected String buildColumnClause(String... pColumns) {
        StringBuilder clauseStr = new StringBuilder();
        for (String col : pColumns) {
            clauseStr.append(col + COMMA);
        }
        return clauseStr.substring(0, clauseStr.length() - 1) + " ";
    }

    /**
     * Replace the key word '%last_modification_date%' in the request to get the data from a date
     *
     * @param pRequest the SQL request
     * @param pDate the date to used to build the date filter
     * @return the SQL request with a from clause to filter the result since a date
     */
    private String buildDateStatement(String pRequest, LocalDateTime pDate) {
        // Search the attribute used to get the new data (first one is used)
        for (Map.Entry<String, InternalAttributes> entry : mappingInternalAttributes.entrySet()) {
            if (entry.getValue() == InternalAttributes.LAST_UPDATE) {
                dateAttributeName = entry.getKey();
                LOG.debug("Attribute for date comparison found: " + entry.getKey());
                break;
            }
        }

        // Any attribute is defined in the mapping for compare the date, return
        if (dateAttributeName.isEmpty()) {
            return pRequest;
        }

        // if any date is defined, replace the keyword and used the first existing date
        if (pDate == null) {
            return pRequest.replaceAll(LAST_MODIFICATION_DATE_KEYWORD,
                                       INIT_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } else {
            return pRequest.replaceAll(LAST_MODIFICATION_DATE_KEYWORD,
                                       dateAttributeName + "> '" + pDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                               + "'");
        }
    }

    /**
     * This method reset the number of data element from the database.<br>
     */
    protected void reset() {
        nbItems = RESET_COUNT;
    }

    /**
     * Converts the mapping between the attribute of the data source and the attributes of the model from a JSon
     * representation to a {@link List} of {@link AbstractAttributeMapping}.
     *
     * @param pModelJson the mapping in JSon format
     */
    protected void initDataSourceMapping(String pModelJson) {
        ModelMappingAdapter adapter = new ModelMappingAdapter();
        try {
            dataSourceMapping = adapter.read(new JsonReader(new StringReader(pModelJson)));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        extractColumnsFromMapping();
    }

    /**
     * This method extracts the {@link List} of columns from the data source mapping.
     */
    private void extractColumnsFromMapping() {
        if (columns == null) {
            columns = new ArrayList<>();
        }

        dataSourceMapping.getAttributesMapping().forEach(d -> {
            columns.add(d.getNameDS());
            if (d.isPrimaryKey()) {
                orderByColumn = d.getNameDS();
            }
        });
    }

    /**
     * For each attributes of the mapping, determine if it is an internal attributes.</br>
     * This initialization is done, only once, before the read of the data.
     */
    private void initMappingInternalAttributes() {
        for (AbstractAttributeMapping attrMapping : dataSourceMapping.getAttributesMapping()) {

            if (attrMapping.isLabel()) {
                mappingInternalAttributes.put(attrMapping.getNameDS(), InternalAttributes.LABEL);
            } else if (attrMapping.isRawData()) {
                mappingInternalAttributes.put(attrMapping.getNameDS(), InternalAttributes.RAW_DATA);
            } else if (attrMapping.isThumbnail()) {
                mappingInternalAttributes.put(attrMapping.getNameDS(), InternalAttributes.THUMBNAIL);
            } else if (attrMapping.isLastUpdate()) {
                mappingInternalAttributes.put(attrMapping.getNameDS(), InternalAttributes.LAST_UPDATE);
            }
        }
    }

    private enum InternalAttributes {
        /**
         * Identify attribute for the last update attribute
         */
        LAST_UPDATE,

        /**
         * Identify an attribute for a file as {@link DataType}{@link #RAW_DATA}
         */
        RAW_DATA,

        /**
         * Identify an attribute for a file as {@link DataType}{@link #THUMBNAIL}
         */
        THUMBNAIL,

        /**
         * Identify an attribute for a label attribute
         */
        LABEL
    }

    ;

}
