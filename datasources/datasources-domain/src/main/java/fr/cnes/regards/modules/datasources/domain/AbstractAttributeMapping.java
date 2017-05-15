/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.domain;

import java.sql.Types;

import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * This class is used to map a data source attribute to an attribute of a {@link Model}
 *
 * @author Christophe Mertz
 * @author oroussel
 */
public abstract class AbstractAttributeMapping {

    /**
     * Constant used for the primary key attribute
     */
    public static final String PRIMARY_KEY = "sipId";

    /**
     * Constant used for the last update attribute
     */
    public static final String LAST_UPDATE = "lastUpdate";

    /**
     * Constant used for the label attribute
     */
    public static final String LABEL = "label";

    /**
     * Constant used for the raw data attribute
     */
    public static final String RAW_DATA = "download";

    /**
     * Constant used for the thumbnail attribute 
     */
    public static final String THUMBNAIL = "thumbnail";

    /**
     * Constant used for the geometry attribute
     */
    public static final String GEOMETRY = "geometry";

    /**
     * The attribute name in the model
     */
    private String name;

    /**
     * The attribute type in the model
     */
    private AttributeType type;

    /**
     * The attribute namespace in the model
     */
    private String nameSpace = null;

    /**
     * The attribute name in the data source
     */
    private String nameDS;

    /**
     * The attribute type in the datasource, see {@link Types}
     */
    private Integer typeDS = null;

    protected AbstractAttributeMapping() {
    }

    /**
     * Constructor with all attributes
     * @param pName the attribute name in the model
     * @param pNameSpace the attribute name space in the model
     * @param pType the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS The attribute name in the data source
     * @param pTypeDS The attribute type in the data source @see {@link Types}
     */
    protected AbstractAttributeMapping(String pName, String pNameSpace, AttributeType pType, String pMappingDS,
            Integer pTypeDS) {
        this.name = pName;
        this.nameSpace = pNameSpace;
        this.nameDS = pMappingDS;
        this.typeDS = pTypeDS;
        if (pType == null && isMappedToStaticProperty()) {
            this.type = getStaticdAttributeType(pName);
        } else {
            this.type = pType;
        }
    }

    /**
     * Get the {@link AttributeType} for a static attribute
     * @param staticAttrName of one of the static attribute :
     * <li>{@value #PRIMARY_KEY}
     * <li>{@value #LAST_UPDATE}
     * <li>{@value #LABEL}
     * <li>{@value #RAW_DATA}
     * <li>{@value #THUMBNAIL}
     * <li>{@value #GEOMETRY}
     * @return the {@link AttributeType}
     */
    private AttributeType getStaticdAttributeType(String staticAttrName) {
        switch (staticAttrName) {
            case PRIMARY_KEY:
                return AttributeType.LONG;
            case LABEL:
            case RAW_DATA:
            case THUMBNAIL:
                return AttributeType.STRING;
            case LAST_UPDATE:
                return AttributeType.DATE_ISO8601;
            default:
                return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        this.name = pName;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType pType) {
        this.type = pType;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String pNameSpace) {
        this.nameSpace = pNameSpace;
    }

    public String getNameDS() {
        return nameDS;
    }

    public void setNameDS(String pNameDS) {
        this.nameDS = pNameDS;
    }

    public Integer getTypeDS() {
        return typeDS;
    }

    public void setTypeDS(Integer pTypeDS) {
        this.typeDS = pTypeDS;
    }

    public boolean isPrimaryKey() {
        return name.equals(PRIMARY_KEY);
    }

    public boolean isLastUpdate() {
        return name.equals(LAST_UPDATE);
    }

    public boolean isLabel() {
        return name.equals(LABEL);
    }

    public boolean isRawData() {
        return name.equals(RAW_DATA);
    }

    public boolean isThumbnail() {
        return name.equals(THUMBNAIL);
    }

    public boolean isGeometry() {
        return name.equals(GEOMETRY);
    }

    public boolean isMappedToStaticProperty() {
        return isPrimaryKey() || isLastUpdate() || isLabel() || isRawData() || isThumbnail() || isGeometry();
    }
}
