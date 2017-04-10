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
public class DataSourceAttributeMapping {

    /**
     * Constant to be used in mappingOptions. Indicates that the attribute has no specific mapping options
     */
    public static final short NO_MAPPING_OPTIONS = 0;

    /**
     * Constant to be used in mappingOptions. Indicates the attribute is a primary key
     */
    public static final short PRIMARY_KEY = 1;

    /**
     * Constant to be used in mappingOptions. Indicates the attribute is the last update field
     */
    public static final short LAST_UPDATE = 2;

    /**
     * Constant to be used in mappingOptions. Indicates the attribute is the label
     */
    public static final short LABEL = 4;

    /**
     * Constant to be used in mappingOptions. Indicates the attribute is raw data
     */
    public static final short RAW_DATA = 8;

    /**
     * Constant to be used in mappingOptions. Indicates the attribute is a thumbnail
     */
    public static final short THUMBNAIL = 16;

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

    /**
     * Combination of mapping options, ie a OR bitwise (| operator) of {@value #PRIMARY_KEY}, {@value #LAST_UPDATE},
     * {@value #RAW_DATA}, {@value #LABEL} and {@value #THUMBNAIL}
     */
    private short mappingOptions = NO_MAPPING_OPTIONS;

    /**
     * Default constructor
     */
    public DataSourceAttributeMapping() {
        super();
    }

    /**
     * Constructor with all attributes
     * @param pName the attribute name in the model
     * @param pNameSpace the attribute name space in the model
     * @param pType the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS The attribute name in the data source
     * @param pTypeDS The attribute type in the data source @see {@link Types}
     * @param pMappingOptions combination of mapping options, ie a OR bitwise (| operator) of {@value #PRIMARY_KEY}, {@value #LAST_UPDATE},
     * {@value #RAW_DATA}, {@value #LABEL} and {@value #THUMBNAIL}
     */
    public DataSourceAttributeMapping(String pName, String pNameSpace, AttributeType pType, String pMappingDS,
            Integer pTypeDS, short pMappingOptions) {
        this();
        this.name = pName;
        this.type = pType;
        this.nameSpace = pNameSpace;
        this.nameDS = pMappingDS;
        this.typeDS = pTypeDS;
        this.mappingOptions = pMappingOptions;
    }

    /**
     * Constructor for a {@link DataSourceAttributeMapping} with a namespace
     *
     * @param pName the attribute name in the model
     * @param pNameSpace the attribute name space in the model
     * @param pType the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS The attribute name in the data source
     * @param pTypeDS The attribute type in the data source @see {@link Types}
     * @param pMappingOptions combination of mapping options, ie a OR bitwise (| operator) of {@value #PRIMARY_KEY}, {@value #LAST_UPDATE},
     * {@value #RAW_DATA}, {@value #LABEL} and {@value #THUMBNAIL}
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMappingDS, Integer pTypeDS,
            short pMappingOptions) {
        this(pName, null, pType, pMappingDS, pTypeDS, pMappingOptions);
    }

    /**
     * Constructor
     * @param pName  the attribute name in the model
     * @param pNameSpace the attribute name space in the model
     * @param pType the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS The attribute name in the data source
     * @param pTypeDS The attribute type in the data source @see {@link Types}
     */
    public DataSourceAttributeMapping(String pName, String pNameSpace, AttributeType pType, String pMappingDS,
            Integer pTypeDS) {
        this(pName, pNameSpace, pType, pMappingDS, pTypeDS, NO_MAPPING_OPTIONS);
    }

    /**
     *
     * @param pName the attribute name in the model
     * @param pType the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS The attribute name in the data source
     * @param pTypeDS The attribute type in the data source @see {@link Types}
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMappingDS, Integer pTypeDS) {
        this(pName, null, pType, pMappingDS, pTypeDS);
    }

    /**
     * Constructor for a {@link DataSourceAttributeMapping} with a namespace
     * @param pName the attribute name in the model
     * @param pNameSpace the attribute name space in the model
     * @param pType the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS The attribute name in the data source
     */
    public DataSourceAttributeMapping(String pName, String pNameSpace, AttributeType pType, String pMappingDS) {
        this(pName, pNameSpace, pType, pMappingDS, null);
    }

    /**
     * Constructor for a {@link DataSourceAttributeMapping} without namespace
     * @param pName the attribute name in the model
     * @param pType the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS The attribute name in the data source
     * @param pMappingOptions combination of mapping options, ie a OR bitwise (| operator) of {@value #PRIMARY_KEY}, {@value #LAST_UPDATE},
     * {@value #RAW_DATA}, {@value #LABEL} and {@value #THUMBNAIL}
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMappingDS, short pMappingOptions) {
        this(pName, pType, pMappingDS, null, pMappingOptions);
    }

    /**
     * Constructor for a {@link DataSourceAttributeMapping} without namespace
     *
     * @param pName the attribute name in the model
     * @param pType the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS The attribute name in the data source
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMappingDS) {
        this(pName, pType, pMappingDS, NO_MAPPING_OPTIONS);
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

    public void addMappingOption(short option) {
        mappingOptions |= option;
    }

    public void setMappingOption(short options) {
        mappingOptions = options;
    }

    public short getMappingOptions() {
        return mappingOptions;
    }

    public boolean isPrimaryKey() {
        return ((mappingOptions & PRIMARY_KEY) == PRIMARY_KEY);
    }

    public boolean isLastUpdate() {
        return ((mappingOptions & LAST_UPDATE) == LAST_UPDATE);
    }

    public boolean isLabel() {
        return ((mappingOptions & LABEL) == LABEL);
    }

    public boolean isRawData() {
        return ((mappingOptions & RAW_DATA) == RAW_DATA);
    }

    public boolean isThumbnail() {
        return ((mappingOptions & THUMBNAIL) == THUMBNAIL);
    }
}
