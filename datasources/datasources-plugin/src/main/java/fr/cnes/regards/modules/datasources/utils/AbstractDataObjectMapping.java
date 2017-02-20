/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

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

import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
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
 * TODO
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
    private static final String LIMIT_CLAUSE = " LIMIT %d OFFSET %d";

    /**
     * A pattern used to set a date in the statement
     */
    private static final String DATE_STATEMENT = "%last_modification_date%";

    /**
     * A default date
     */
    private static final LocalDateTime INIT_DATE = LocalDateTime.of(1, 1, 1, 0, 0);

    /**
     * The mapping between the attributes in the {@link Model} and the data source
     *
     * @return the mapping
     */
    protected abstract DataSourceModelMapping getModelMapping();

    /**
     *
     */
    private int nn = RESET_COUNT;

    private static final int RESET_COUNT = -1;

    /**
     * Returns a page of DataObject from the database defined by the {@link Connection} and corresponding to the SQL. A
     * {@link Date} is apply to filter the {@link DataObject} created or updated after this {@link Date}. And add the
     * page limit clause in the request.</br>
     * TODO : does not work for Oracle, need to used the right SqlGenerator
     *
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
        Statement statement = null;
        ResultSet rs = null;

        try {
            statement = pConn.createStatement();

            // Execute SQL request
            String sqlRequestWithPagedInformation = buildLimitPart(applyDateStatement(pRequestSql, pDate), pPageable);

            rs = statement.executeQuery(sqlRequestWithPagedInformation);

            while (rs.next()) {
                dataObjects.add(processResultSet(pTenant, rs));
            }

            rs.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        return new PageImpl(dataObjects);
    }

    /**
     * Returns a page of DataObject from the database defined by the {@link Connection} and corresponding to the SQL. A
     * {@link Date} is apply to filter the {@link DataObject} created or updated after this {@link Date}.
     *
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
    @SuppressWarnings("unchecked")
    public Page<DataObject> findAll(String pTenant, Connection pConn, String pRequestSql, String pCountRequest,
            Pageable pPageable, LocalDateTime pDate) {
        List<DataObject> dataObjects = new ArrayList<>();

        Statement statement = null;

        try {
            statement = pConn.createStatement();

            // Execute the request to get the elements
            ResultSet rs = statement.executeQuery(pRequestSql);

            while (rs.next()) {
                dataObjects.add(processResultSet(pTenant, rs));
            }

            rs.close();

            if (nn == -1) {
                // Execute the request ot count the element
                rs = statement.executeQuery(pCountRequest);
                if (rs.next()) {
                    nn = rs.getInt(1);
                }
                rs.close();
            }

        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }

        return new PageImpl<>(dataObjects, pPageable, nn);
    }

    /**
     * Returns a page of DataObject from the database defined by the {@link Connection} and corresponding to the SQL.
     *
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
     * @param pRs
     *            the {@link ResultSet}
     * @return the {@link DataObject} created
     * @throws SQLException
     */
    protected DataObject processResultSet(String pTenant, ResultSet pRs) throws SQLException {
        final DataObject data = new DataObject();
        final List<AbstractAttribute<?>> attributes = new ArrayList<>();
        final Map<String, List<AbstractAttribute<?>>> spaceNames = Maps.newHashMap();

        /**
         * Loop the attributes in the mapping
         */
        for (DataSourceAttributeMapping attrMapping : getModelMapping().getAttributesMapping()) {

            if (attrMapping.isPrimaryKey()) {
                String val = pRs.getString(attrMapping.getNameDS());
                data.setIpId(buildUrn(pTenant, val, attrMapping));
                data.setSipId(val);
            } else {

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
                attr = AttributeBuilder.buildString(pAttrMapping.getName(), pRs.getString(pAttrMapping.getNameDS()));
                break;
            case LONG:
                attr = AttributeBuilder.buildLong(pAttrMapping.getName(), pRs.getLong(pAttrMapping.getNameDS()));
                break;
            case INTEGER:
                attr = AttributeBuilder.buildInteger(pAttrMapping.getName(), pRs.getInt(pAttrMapping.getNameDS()));
                break;
            case BOOLEAN:
                attr = AttributeBuilder.buildBoolean(pAttrMapping.getName(), pRs.getBoolean(pAttrMapping.getNameDS()));
                break;
            case DOUBLE:
                attr = AttributeBuilder.buildDouble(pAttrMapping.getName(), pRs.getDouble(pAttrMapping.getNameDS()));
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
        return attr;
    }

    private UniformResourceName buildUrn(String pTenant, String pVal, DataSourceAttributeMapping pAttrMapping)
            throws SQLException {
        return new UniformResourceName(OAISIdentifier.SIP, EntityType.DATA, pTenant,
                UUID.nameUUIDFromBytes(pVal.getBytes()), 1);
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
        final String limit = String.format(LIMIT_CLAUSE, pPage.getPageSize(), offset);
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
        nn = RESET_COUNT;
    }
}
