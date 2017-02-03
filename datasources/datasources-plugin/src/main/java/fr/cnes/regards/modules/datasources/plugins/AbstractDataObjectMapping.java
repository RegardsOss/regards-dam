/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Maps;

import fr.cnes.regards.modules.datasources.plugins.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
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
    protected abstract List<DataSourceAttributeMapping> getAttributesMapping();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Page<DataObject> findAll(Connection conn, String requestSql, Pageable pPageable, LocalDateTime pDate) {
        List<DataObject> dataObjects = new ArrayList<>();

        try {
            Statement statement = conn.createStatement();

            // Execute SQL request
            String sqlRequestWithPagedInformation = buildLimitPart(applyDateStatement(requestSql, pDate), pPageable);

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

        return new PageImpl(dataObjects);
    }

    @SuppressWarnings("unchecked")
    public Page<DataObject> findAll(Connection conn, Pageable pPageable, String requestSql, LocalDateTime pDate) {
        List<DataObject> dataObjects = new ArrayList<>();

        try {
            Statement statement = conn.createStatement();

            ResultSet rs = statement.executeQuery(requestSql);

            while (rs.next()) {
                dataObjects.add(processResultSet(rs));
            }

            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }

        return new PageImpl<>(dataObjects, pPageable, 3137);
    }

    public Page<DataObject> findAll(Connection conn, String requestSql, Pageable pPageable) {
        return findAll(conn, requestSql, pPageable, null);
    }

    /**
     * Build a {@link DataObject} for a {@link ResultSet}.
     * 
     * @param pRs
     *            the {@link ResultSet}
     * @return the {@link DataObject} created
     */
    protected DataObject processResultSet(ResultSet pRs) {
        final DataObject data = new DataObject();
        final List<AbstractAttribute<?>> attributes = new ArrayList<>();
        final Map<String, List<AbstractAttribute<?>>> spaceNames = Maps.newHashMap();

        /**
         * Loop the attributes in the mapping
         */
        for (DataSourceAttributeMapping attrMapping : getAttributesMapping()) {
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
                attr = AttributeBuilder.buildString(pAttrMapping.getName(), pRs.getString(pAttrMapping.getNameDS()));
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
                long n = 0;
                if (pAttrMapping.getTypeDS() == null) {
                    n = pRs.getTimestamp(pAttrMapping.getNameDS()).getTime();
                } else
                    if (pAttrMapping.getTypeDS() == Types.DECIMAL) {
                        n = pRs.getLong(pAttrMapping.getNameDS());
                    } else
                        if (pAttrMapping.getTypeDS() == Types.NUMERIC) {
                            n = pRs.getLong(pAttrMapping.getNameDS());
                        }
                Instant instant = Instant.ofEpochMilli(n);
                attr = AttributeBuilder.buildDate(pAttrMapping.getName(),
                                                  LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                break;
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
}
