/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class AbstractDataObjectMapping
 *
 * @author Christophe Mertz
 */
public abstract class AbstractDataObjectMapping {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataObjectMapping.class);

    /**
     * The string used to add the pagination information in PostGreSql
     */
    private static final String LIMIT_CLAUSE = " ORDER BY %s LIMIT %d OFFSET %d";

    /**
     * The PL/SQL key word AS
     */
    private static final String AS = "as";

    private static final String COMMA = ",";

    private static final String SELECT = "SELECT ";

    /**
     * A pattern used to set a date in the statement
     */
    private static final String DATE_STATEMENT = "%last_modification_date%";

    /**
     * A default date
     */
    private static final LocalDateTime INIT_DATE = LocalDateTime.of(1, 1, 1, 0, 0);

    /**
     * A default value to indicates that the count request should be execute
     */
    private static final int RESET_COUNT = -1;

    /**
     * The result of the count request
     */
    private int nbItems = RESET_COUNT;

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

    // protected SqlGenerator sqlGenerator;
    //
    // protected abstract SqlGenerator buildSqlGenerator();
    //
    // protected abstract SqlGenerator buildSqlGenerator(String pAllColumnsClause, String pOrderBy);

    /**
     * Returns a page of DataObject from the database defined by the {@link Connection} and corresponding to the SQL. A
     * {@link Date} is apply to filter the {@link DataObject} created or updated after this {@link Date}. And add the
     * page limit clause in the request.</br>
     * TODO : does not work for Oracle, need to used the right SqlGenerator
     *
     * @param pTenant
     *            the tenant name
     * @param pConn
     *            a {@link Connection} to a database
     * @param pRequestSql
     *            the SQL request
     * @param pPageable
     *            the page information
     * @param pDate
     *            a {@link Date} used to apply returns the {@link DataObject} update or create after this date
     * @return a page of {@link DataObject}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Page<DataObject> findAllApplyPageAndDate(String pTenant, Connection pConn, String pRequestSql,
            Pageable pPageable, LocalDateTime pDate) {
        List<DataObject> dataObjects = new ArrayList<>();

        try (Statement statement = pConn.createStatement()) {

            String requestWithDate = applyDateStatement(pRequestSql, pDate);

            String requestWithLimit = buildLimitPart(requestWithDate, pPageable);

            String sqlRequestWithPagedInformation = SELECT + buildColumnClause(columns.toArray(new String[0]))
                    + requestWithLimit;

            // Execute SQL request
            // String sqlRequestWithPagedInformation = buildLimitPart(applyDateStatement(SELECT
            // +buildColumnClause(columns
            // .toArray(new String[0])) + " " + pRequestSql, pDate), pPageable);

            try (ResultSet rs = statement.executeQuery(sqlRequestWithPagedInformation)) {
                while (rs.next()) {
                    dataObjects.add(processResultSet(pTenant, rs));
                }
            }

            statement.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }

        return new PageImpl(dataObjects);
    }

    /**
     * Returns a page of DataObject from the database defined by the {@link Connection} and corresponding to the SQL. A
     * {@link Date} is apply to filter the {@link DataObject} created or updated after this {@link Date}.
     *
     * @param pTenant
     *            the tenant name
     * @param pConn
     *            a {@link Connection} to a database
     * @param pRequestSql
     *            the SQL request
     * @param pCountRequest
     *            the SQL count request
     * @param pPageable
     *            the page information
     * @param pDate
     *            a {@link Date} used to apply returns the {@link DataObject} update or create after this date
     * @return a page of {@link DataObject}
     */
    public Page<DataObject> findAll(String pTenant, Connection pConn, String pRequestSql, String pCountRequest,
            Pageable pPageable, LocalDateTime pDate) {
        List<DataObject> dataObjects = new ArrayList<>();

        try (Statement statement = pConn.createStatement();) {

            // Execute the request to get the elements
            try (ResultSet rs = statement.executeQuery(pRequestSql)) {

                while (rs.next()) {
                    dataObjects.add(processResultSet(pTenant, rs));
                }

                if (nbItems == RESET_COUNT) {
                    // Execute the request to count the elements
                    try (ResultSet rsCount = statement.executeQuery(pCountRequest)) {
                        if (rsCount.next()) {
                            nbItems = rsCount.getInt(1);
                        }
                    }
                }
            }

            statement.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }

        return new PageImpl<>(dataObjects, pPageable, nbItems);
    }

    /**
     * Returns a page of DataObject from the database defined by the {@link Connection} and corresponding to the SQL.
     *
     * @param pTenant
     *            the tenant name
     * @param pConn
     *            a {@link Connection} to a database
     * @param pRequestSql
     *            the SQL request
     * @param pPageable
     *            the page information
     * @return a page of {@link DataObject}
     */
    public Page<DataObject> findAll(String pTenant, Connection pConn, String pRequestSql, Pageable pPageable) {
        return findAllApplyPageAndDate(pTenant, pConn, pRequestSql, pPageable, null);
    }

    /**
     * Build a {@link DataObject} for a {@link ResultSet}.
     *
     * @param pTenant
     *            the tenant name
     * @param pRs
     *            the {@link ResultSet}
     * @return the {@link DataObject} created
     * @throws SQLException
     *             An SQL error occurred
     */
    protected DataObject processResultSet(String pTenant, ResultSet pRs) throws SQLException {
        final DataObject data = new DataObject();
        final List<AbstractAttribute<?>> attributes = new ArrayList<>();
        final Map<String, List<AbstractAttribute<?>>> spaceNames = Maps.newHashMap();

        /**
         * Loop the attributes in the mapping
         */
        for (DataSourceAttributeMapping attrMapping : dataSourceMapping.getAttributesMapping()) {

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

                    if (attrMapping.isPrimaryKey()) {
                        String val = attr.getValue().toString();
                        data.setIpId(buildUrn(pTenant, val));
                        data.setSipId(val);
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("get value for <" + pAttrMapping.getNameDS() + "> of type <" + pAttrMapping.getType() + ">");
        }

        String label = extractCollumnName(pAttrMapping.getNameDS());

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

        if (LOG.isDebugEnabled() && attr != null) {
            LOG.debug("the value for <" + pAttrMapping.getNameDS() + "> is :" + attr.getValue());
        }

        return attr;
    }

    /**
     * Extracts a column label from a PL/SQL expression.</br>
     * The column label is placed after the word 'AS'.
     * 
     * @param pAttrMapping
     *            The PL/SQL expression to analyze
     * @return the column label extracted from the PL/SQL
     */
    private String extractCollumnName(String pAttrMapping) {
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
     * Build an URN for a {@link EntityType} of type DATA. The URN contains an UUID builds for a specific value, it used
     * {@link UUID#nameUUIDFromBytes(byte[]).
     * 
     * @param pTenant
     *            the tenant name
     * @param pVal
     *            the value used to build the UUID
     * @return the URN
     */
    private UniformResourceName buildUrn(String pTenant, String pVal) {
        return new UniformResourceName(OAISIdentifier.SIP, EntityType.DATA, pTenant,
                UUID.nameUUIDFromBytes(pVal.getBytes()), 1);
    }

    protected String buildColumnClause(String... pColumns) {
        StringBuilder clauseStr = new StringBuilder();
        for (String col : pColumns) {
            clauseStr.append(col + COMMA);
        }
        return clauseStr.substring(0, clauseStr.length() - 1) + " ";
    }

    /**
     * Get {@link DateAttribute}.
     *
     * @param pRs
     *            the {@link ResultSet}
     * @param pAttrMapping
     *            the {@link DataSourceAttributeMapping}
     * @return a new {@link DateAttribute}
     * @throws SQLException
     *             if an error occurs in the {@link ResultSet}
     */
    private AbstractAttribute<?> buildDateAttribute(ResultSet pRs, DataSourceAttributeMapping pAttrMapping)
            throws SQLException {
        long n = 0;
        if (pAttrMapping.getTypeDS() == null) {
            n = pRs.getTimestamp(pAttrMapping.getNameDS()).getTime();
        } else {
            if ((pAttrMapping.getTypeDS() == Types.DECIMAL) || (pAttrMapping.getTypeDS() == Types.NUMERIC)) {
                n = pRs.getLong(pAttrMapping.getNameDS());
            }
        }
        Instant instant = Instant.ofEpochMilli(n);
        return AttributeBuilder.buildDate(pAttrMapping.getName(),
                                          LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    /**
     * Add to the SQL request the part to fetch only a portion of the results.
     * 
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
        final String limit = String.format(LIMIT_CLAUSE, orderByColumn, pPage.getPageSize(), offset);
        str.append(limit);

        return str.toString();
    }

    private String applyDateStatement(String pRequest, LocalDateTime pDate) {
        if (pDate == null) {
            return pRequest.replaceAll(DATE_STATEMENT, INIT_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } else {
            return pRequest.replaceAll(DATE_STATEMENT, pDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    /**
     * This method reset the number of data element from the database.<br>
     */
    protected void reset() {
        nbItems = RESET_COUNT;
    }

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
     * Converts the mapping between the attribute of the data source and the attributes of the model from a JSon
     * representation to a {@link List} of {@link DataSourceAttributeMapping}.
     * 
     * @param pModelJson
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

}
