/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.cnes.regards.modules.datasources.utils;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;
import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.domain.plugins.DataSourceException;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.entities.domain.converter.GeometryAdapter;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * This class allows to process a SQL request to a SQL Database.</br>
 * For each data reads in the Database, a {@link DataObject} is created. This {@link DataObject} are compliant with a
 * {@link Model}.</br>
 * Some attributes extracts from the Database are specials. For each one, a {@link DataObject} property is set :
 * <li>the primary key of the data
 * <li>the data file of the data
 * <li>the thumbnail of the data
 * <li>the update date of the data
 * @author Christophe Mertz
 */
public abstract class AbstractDataObjectMapping {

    /**
     * The PL/SQL key word AS
     */
    protected static final String AS = "as ";

    /**
     * A pattern used to set a date in the statement
     */
    protected static final String LAST_MODIFICATION_DATE_KEYWORD = "%last_modification_date%";

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataObjectMapping.class);

    private static final String BLANK = " ";

    /**
     * A comma used to build the select clause
     */
    private static final String COMMA = ",";

    /**
     * A default date
     */
    private static final OffsetDateTime INIT_DATE = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final GeometryAdapter<?> GEOMETRY_ADAPTER = new GeometryAdapter<>();

    /**
     * A default value to indicates that the count request should be execute
     */
    private static final int RESET_COUNT = -1;

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
     * The {@link Model} identifier
     */
    protected Model model;

    /**
     * The mapping between the attribute of the {@link Model} of the attributes of th data source
     */
    protected List<AbstractAttributeMapping> attributesMapping;

    @Autowired
    private IModelService modelService;

    /**
     * Common tags to be added on all created data objects
     */
    private Collection<String> commonTags;

    /**
     * The result of the count request
     */
    private int nbItems = RESET_COUNT;

    /**
     * The attribute name used for the date comparison
     */
    private String lastUpdateAttributeName = "";

    /**
     * Get {@link DateAttribute}.
     * @param rs the {@link ResultSet}
     * @param attrName the attribute name
     * àparam attrDSName the column name in the external data source
     * @param colName the column name in the {@link ResultSet}
     * @return a new {@link DateAttribute}
     * @throws SQLException if an error occurs in the {@link ResultSet}
     */
    protected abstract AbstractAttribute<?> buildDateAttribute(ResultSet rs, String attrName, String attrDSName,
            String colName) throws SQLException;

    /**
     * Get a {@link LocalDateTime} value from a {@link ResultSet} for a {@link AbstractAttributeMapping}.
     * @param rs The {@link ResultSet} to read
     * @param colName the column name in the {@link ResultSet}
     * @return the {@link OffsetDateTime}
     * @throws SQLException An error occurred when try to read the {@link ResultSet}
     */
    protected OffsetDateTime buildOffsetDateTime(ResultSet rs, String colName) throws SQLException {
        long n = rs.getTimestamp(colName).getTime();
        Instant instant = Instant.ofEpochMilli(n);
        return OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }

    /**
     * Returns a page of DataObject from the database defined by the {@link Connection} and corresponding to the SQL. A
     * {@link Date} is apply to filter the {@link DataObject} created or updated after this {@link Date}.
     * @param tenant the tenant name
     * @param ctx a {@link Connection} to a database
     * @param inSelectRequest the SQL request
     * @param inCountRequest the SQL count request
     * @param pageable the page information
     * @param sinceDate a {@link Date} used to apply returns the {@link DataObject} update or create after this date
     * @return a page of {@link DataObject}
     */
    protected Page<DataObject> findAll(String tenant, Connection ctx, String inSelectRequest, String inCountRequest,
            Pageable pageable, OffsetDateTime sinceDate) throws DataSourceException {
        List<DataObject> dataObjects = new ArrayList<>();

        try (Statement statement = ctx.createStatement()) {

            String selectRequest = inSelectRequest;
            String countRequest = inCountRequest;

            if (sinceDate != null) {
                selectRequest = buildDateStatement(selectRequest, sinceDate);
                countRequest = buildDateStatement(countRequest, sinceDate);
            }
            LOG.info("select request : " + selectRequest);
            LOG.info("count request : " + countRequest);

            // Execute the request to get the elements
            try (ResultSet rs = statement.executeQuery(selectRequest)) {
                while (rs.next()) {
                    dataObjects.add(processResultSet(rs, this.model, tenant));
                }
            }
            countItems(statement, countRequest);
        } catch (SQLException e) {
            LOG.error("Error while retrieving or counting datasource elements", e);
            throw new DataSourceException("Error while retrieving or counting datasource elements", e);
        }
        return new PageImpl<>(dataObjects, pageable, nbItems);
    }

    /**
     * Execute a SQL request to count the number of items
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
     * @param rset the {@link ResultSet}
     * @return the {@link DataObject} created
     * @throws SQLException An SQL error occurred
     */
    protected DataObject processResultSet(ResultSet rset, Model model, String tenant)
            throws SQLException, DataSourceException {
        final DataObject data = new DataObject(model, tenant, null);

        final Set<AbstractAttribute<?>> attributes = new HashSet<>();
        final Map<String, List<AbstractAttribute<?>>> spaceNames = Maps.newHashMap();

        /**
         * Loop the attributes in the mapping
         */
        for (AbstractAttributeMapping attrMapping : attributesMapping) {
            AbstractAttribute<?> attr = buildAttribute(rset, attrMapping);

            if (attr != null) {
                if (attrMapping.isMappedToStaticProperty()) {
                    // static attribute mapping
                    processStaticAttributes(data, attr, attrMapping);
                } else {
                    // dynamic attribute mapping
                    if (!Strings.isNullOrEmpty(attrMapping.getNameSpace())) {
                        if (!spaceNames.containsKey(attrMapping.getNameSpace())) {
                            // It is a new name space
                            spaceNames.put(attrMapping.getNameSpace(), new ArrayList<>());
                        }
                        // Add the attribute to the namespace
                        spaceNames.get(attrMapping.getNameSpace()).add(attr);
                    } else {
                        attributes.add(attr);
                    }
                }
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

        // Add common tags
        data.getTags().addAll(commonTags);

        return data;
    }

    /**
     * Get an attribute define in the mapping in a {@link ResultSet}
     * @param rset the {@link ResultSet}
     * @param attrMapping the {@link AbstractAttributeMapping}
     * @return a new {@link AbstractAttribute}
     * @throws SQLException if an error occurs in the {@link ResultSet}
     */
    private AbstractAttribute<?> buildAttribute(ResultSet rset, AbstractAttributeMapping attrMapping)
            throws SQLException, DataSourceException {
        AbstractAttribute<?> attr = null;
        final String colName = extractColumnName(attrMapping.getNameDS(), attrMapping.getName(),
                                                 attrMapping.isPrimaryKey());

        switch (attrMapping.getType()) {
            // lets handle touchy cases by hand
            case URL:
                try {
                    attr = AttributeBuilder.buildUrl(attrMapping.getName(), new URL(rset.getString(colName)));
                } catch (MalformedURLException e) {
                    String message = String
                            .format("Given url into database (column %s) could not be processed as a URL", colName);
                    LOG.error(message, e);
                    throw new DataSourceException(message, e);
                }
                break;
            case DATE_ISO8601:
                attr = buildDateAttribute(rset, attrMapping.getName(), attrMapping.getNameDS(), colName);
                break;
            // if it is not a touchy case, lets use the general way
            default:
                attr = AttributeBuilder.forType(attrMapping.getType(), attrMapping.getName(), rset.getObject(colName));
                break;
        }
        // If value was null => no attribute value
        if (rset.wasNull()) {
            return null;
        }

        if (LOG.isDebugEnabled() && (attr != null)) {
            if ((attrMapping.getName() != null) && attrMapping.getName().equals(attrMapping.getNameDS())) {
                LOG.debug("the value for <" + attrMapping.getName() + "> of type <" + attrMapping.getType() + "> is :"
                                  + attr.getValue());

            } else {
                LOG.debug("the value for <" + attrMapping.getName() + "|" + attrMapping.getNameDS() + "> of type <"
                                  + attrMapping.getType() + "> is :" + attr.getValue());
            }
        }

        return attr;
    }

    /**
     * Extracts a column name from a PL/SQL expression.</br>
     * The column label can be placed after the word 'AS'.
     * If 'AS' is not present the column name is the internal attribute name.
     * @param attrDataSourceName The PL/SQL expression to analyze
     * @param attrName The attribute name
     * @return the column label extracted from the PL/SQL
     */
    protected String extractColumnName(String attrDataSourceName, String attrName, boolean isPrimaryKey) {
        String colName = "";

        int pos = attrDataSourceName.toLowerCase().lastIndexOf(AS);

        if (pos > 0) {
            String str = attrDataSourceName.substring(pos + AS.length()).trim();
            LOG.debug("the extracted column name is : <{}>", str);
            colName = str;
        } else {
            LOG.debug("the extracted column name is : <{}>", attrName);
            if (isPrimaryKey) {
                colName = attrDataSourceName;
            } else {
                colName = attrName;
            }
        }

        return colName;
    }

    /**
     * This class extracts data information from an attribute and sets this informations into the
     * {@link DataObject}.</br>
     * The REGARDS internal attributes's that are analyzed :
     * <li>primary key
     * <li>raw data
     * <li>thumbnail
     * <li>label
     * <li>last update date
     * <li>geometry
     * @param dataObject the current {@link DataObject} to build
     * @param attr the current {@link AbstractAttribute} to analyze
     * @param attrMapping the {@link AbstractAttributeMapping} for the current attribute
     */
    private void processStaticAttributes(DataObject dataObject, AbstractAttribute<?> attr,
            AbstractAttributeMapping attrMapping) {
        if (attrMapping.isPrimaryKey()) {
            String val = attr.getValue().toString();
            dataObject.setSipId(val);
        }
        if (attrMapping.isRawData() || attrMapping.isThumbnail()) {
            String str = ((StringAttribute) attr).getValue();
            try {
                DataType type = attrMapping.isRawData() ? DataType.RAWDATA : DataType.THUMBNAIL;
                Collection<DataFile> dataFiles = dataObject.getFiles().get(type);
                // When external mapping, only one file per type is authorized so dataFiles is a singleton or empty
                DataFile dataFile = dataFiles.isEmpty() ? new DataFile() : dataFiles.iterator().next();
                dataFile.setUri(new URI(str));
                // No check that uri is truly available
                dataFile.setOnline(true);
                // No need to re-put data file if it already exist
                if (dataFiles.isEmpty()) {
                    dataObject.getFiles().put(type, dataFile);
                }
            } catch (URISyntaxException e) {
                LOG.error(e.getMessage(), e);
            }
        }
/*        if (attrMapping.isRawDataSize()) {
            Long size = ((LongAttribute) attr).getValue();
            Collection<DataFile> rawDatas = dataObject.getFiles().get(DataType.RAWDATA);
            // When external mapping, only one file per type is authorized so dataFiles is a singleton or empty
            DataFile dataFile = rawDatas.isEmpty() ? new DataFile() : rawDatas.iterator().next();
            dataFile.setSize(size);
            // No need to re-put data file if it already exist
            if (rawDatas.isEmpty()) {
                dataObject.getFiles().put(DataType.RAWDATA, dataFile);
            }
        }*/
        if (attrMapping.isLastUpdate()) {
            dataObject.setLastUpdate((OffsetDateTime) attr.getValue());
        }
        if (attrMapping.isLabel()) {
            dataObject.setLabel(((StringAttribute) attr).getValue());
        }
        if (attrMapping.isGeometry()) {
            String str = ((StringAttribute) attr).getValue();
            try {
                dataObject.setGeometry(GEOMETRY_ADAPTER.read(new JsonReader(new StringReader(str))));
            } catch (IOException ioe) {
                LOG.error("Unable to deserialize geometry : " + str, ioe);
            }
        }
    }

    /**
     * Build the select clause with the {@link List} of columns used for the mapping.
     * @param columns the comulns used for the mapping
     * @return a {@link String} withe the columns separated by a comma
     */
    protected String buildColumnClause(String... columns) {
        StringBuilder clauseStr = new StringBuilder();
        for (String col : columns) {
            clauseStr.append(col + COMMA);
        }
        return clauseStr.substring(0, clauseStr.length() - 1) + BLANK;
    }

    /**
     * Replace the key word '%last_modification_date%' in the request to get the data from a date
     * @param request the SQL request
     * @param date the date to be used for building date filter
     * @return the SQL request with a from clause to filter the result since a date
     */
    private String buildDateStatement(String request, OffsetDateTime date) {
        // Any attribute is defined in the mapping for compare the date, return
        if (getLastUpdateAttributeName().isEmpty()) {
            return request;
        }

        // if any date is defined, replace the keyword and used the first existing date
        if (date == null) {
            return request.replaceAll(LAST_MODIFICATION_DATE_KEYWORD, OffsetDateTimeAdapter.format(INIT_DATE));
        } else {
            return request.replaceAll(LAST_MODIFICATION_DATE_KEYWORD,
                                      getLastUpdateAttributeName() + "> '" + OffsetDateTimeAdapter.format(date) + "'");
        }
    }

    /**
     * This method reset the number of data element from the database.<br>
     */
    protected void reset() {
        nbItems = RESET_COUNT;
    }

    /**
     * Init with parameters given directly on plugins with @PluginParameter annotation
     */
    protected void init(String modelName, List<AbstractAttributeMapping> attributesMapping,
            Collection<String> commonTags) throws ModuleException {
        this.model = modelService.getModelByName(modelName);
        this.attributesMapping = attributesMapping;
        this.commonTags = commonTags;

        extractColumnsFromMapping();
    }

    protected String getLastUpdateAttributeName() {
        if (!lastUpdateAttributeName.isEmpty()) {
            return lastUpdateAttributeName;
        }

        for (AbstractAttributeMapping attMapping : attributesMapping) {
            if (attMapping.isLastUpdate()) {
                lastUpdateAttributeName = attMapping.getNameDS();
                LOG.debug("Attribute for date comparison found: {}", lastUpdateAttributeName);
                break;
            }
        }
        return lastUpdateAttributeName;
    }

    /**
     * This method extracts the {@link List} of columns from the data source mapping.
     */
    private void extractColumnsFromMapping() {
        if (columns == null) {
            columns = new ArrayList<>();
        }

        attributesMapping.forEach(d -> {
            if ((0 > d.getNameDS().toLowerCase().lastIndexOf(AS)) && !d.isPrimaryKey()) {
                columns.add(d.getNameDS() + BLANK + AS + d.getName());
            } else {
                columns.add(d.getNameDS());
            }

            if (d.isPrimaryKey()) {
                orderByColumn = d.getNameDS();
            }
        });
    }
}
