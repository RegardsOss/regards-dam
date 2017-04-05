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
 *
 */
public class DataSourceAttributeMapping {

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
     * This attribute is the primary key
     */
    private boolean isPrimaryKey = false;

    /**
     * This attribute is the last update
     */
    private boolean isLastUpdate = false;

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
     * @param isPrimaryKey is this attribute the primary key ?
     * @param isLastUpdate is this attribute the last update attribute ?
     */
    public DataSourceAttributeMapping(String pName, String pNameSpace, AttributeType pType, String pMappingDS,
            Integer pTypeDS, boolean isPrimaryKey, boolean isLastUpdate) {
        this();
        this.name = pName;
        this.type = pType;
        this.nameSpace = pNameSpace;
        this.nameDS = pMappingDS;
        this.typeDS = pTypeDS;
        this.isPrimaryKey = isPrimaryKey;
        this.isLastUpdate = isLastUpdate;
    }

    /**
     * Constructor for a {@link DataSourceAttributeMapping} with a namespace
     *
     * @param pName the attribute name in the model
     * @param pNameSpace the attribute name space in the model
     * @param pType the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS The attribute name in the data source
     * @param pTypeDS The attribute type in the data source @see {@link Types}
     * @param pIsPrimaryKey true if the attribute is a primary key in the data source
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMappingDS, Integer pTypeDS,
            boolean pIsPrimaryKey) {
        this(pName, null, pType, pMappingDS, pTypeDS, pIsPrimaryKey, false);
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
        this(pName, pNameSpace, pType, pMappingDS, pTypeDS, false, false);
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
     *
     * @param pName
     *            the attribute name in the model
     * @param pNameSpace
     *            the attribute name space in the model
     * @param pType
     *            the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS
     *            The attribute name in the data source
     */
    public DataSourceAttributeMapping(String pName, String pNameSpace, AttributeType pType, String pMappingDS) {
        this(pName, pNameSpace, pType, pMappingDS, null);
    }

    /**
     * Constructor for a {@link DataSourceAttributeMapping} without namespace
     *
     * @param pName
     *            the attribute name in the model
     * @param pType
     *            the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS
     *            The attribute name in the data source
     * @param pIsPrimaryKey
     *            true if the attribute is a primary key in the data source
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMappingDS, boolean pIsPrimaryKey) {
        this(pName, pType, pMappingDS, null, pIsPrimaryKey);
    }

    /**
     * Constructor for a {@link DataSourceAttributeMapping} without namespace
     *
     * @param pName the attribute name in the model
     * @param pType the attribute type in the model @see {@link AttributeType}
     * @param pIsLastUpdate true if the attribute is the last update attribute
     * @param pMappingDS The attribute name in the data source
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, boolean pIsLastUpdate, String pMappingDS) {
        this(pName, null, pType, pMappingDS, null, false, pIsLastUpdate);
    }

    /**
     * Constructor for a {@link DataSourceAttributeMapping} without namespace
     *
     * @param pName
     *            the attribute name in the model
     * @param pType
     *            the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS
     *            The attribute name in the data source
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMappingDS) {
        this(pName, pType, false, pMappingDS);
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
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(boolean pIsPrimaryKey) {
        this.isPrimaryKey = pIsPrimaryKey;
    }

    public boolean isLastUpdate() {
        return isLastUpdate;
    }

    public void setLastUpdate(boolean pIsLastUpdate) {
        isLastUpdate = pIsLastUpdate;
    }

}
