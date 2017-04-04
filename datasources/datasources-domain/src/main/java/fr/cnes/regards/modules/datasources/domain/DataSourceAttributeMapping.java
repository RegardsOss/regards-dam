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
    private String nameSpace;

    /**
     * The attribute name in the data source
     */
    private String nameDS;

    /**
     * The attribute type in the datasource, see {@link Types}
     */
    private Integer typeDS;

    /**
     * This attribute is the primary key
     */
    private boolean isPrimaryKey;

    /**
     * This attribute is the last update date
     */
    private boolean isLastUpdateDate;

    /**
     * Default constructor
     */
    public DataSourceAttributeMapping() {
        super();
        this.isPrimaryKey = false;
        this.isLastUpdateDate = false;
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
     * @param pTypeDS
     *            The attribute type in the data source @see {@link Types}
     * @param pIsPrimaryKey
     *            true if the attribute is a primary key in the data source
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMappingDS, int pTypeDS,
            boolean pIsPrimaryKey) {
        this();
        this.name = pName;
        this.type = pType;
        this.nameDS = pMappingDS;
        this.typeDS = pTypeDS;
        this.isPrimaryKey = pIsPrimaryKey;
    }

    /**
     *
     * @param pName
     *            the attribute name in the model
     * @param pType
     *            the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS
     *            The attribute name in the data source
     * @param pTypeDS
     *            The attribute type in the data source @see {@link Types}
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMappingDS, int pTypeDS) {
        this();
        this.name = pName;
        this.type = pType;
        this.nameDS = pMappingDS;
        this.typeDS = pTypeDS;
        this.isPrimaryKey = false;
    }

    /**
     * Constructor with all attributes
     *
     * @param pName
     *            the attribute name in the model
     * @param pNameSpace
     *            the attribute name space in the model
     * @param pType
     *            the attribute type in the model @see {@link AttributeType}
     * @param pMappingDS
     *            The attribute name in the data source
     * @param pTypeDS
     *            The attribute type in the data source @see {@link Types}
     */
    public DataSourceAttributeMapping(String pName, String pNameSpace, AttributeType pType, String pMappingDS,
            int pTypeDS) {
        this();
        this.name = pName;
        this.type = pType;
        this.nameSpace = pNameSpace;
        this.nameDS = pMappingDS;
        this.typeDS = pTypeDS;
        this.isPrimaryKey = false;
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
        this();
        this.name = pName;
        this.type = pType;
        this.nameSpace = pNameSpace;
        this.nameDS = pMappingDS;
        this.isPrimaryKey = false;
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
        this();
        this.name = pName;
        this.type = pType;
        this.nameDS = pMappingDS;
        this.isPrimaryKey = false;
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
        this();
        this.name = pName;
        this.type = pType;
        this.nameDS = pMappingDS;
        this.isPrimaryKey = pIsPrimaryKey;
    }

    /**
     * Constructor for a {@link DataSourceAttributeMapping} without namespace
     *
     * @param pName the attribute name in the model
     * @param pType the attribute type in the model @see {@link AttributeType}
     * @param pIsLastUpdateDate true if the attribute is the last update date attribute
     * @param pMappingDS The attribute name in the data source
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, boolean pIsLastUpdateDate, String pMappingDS) {
        this();
        this.name = pName;
        this.type = pType;
        this.nameDS = pMappingDS;
        this.isLastUpdateDate = pIsLastUpdateDate;
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

    public boolean isLastUpdateDate() {
        return isLastUpdateDate;
    }

    public void setLastUpdateDate(boolean pIsLastUpdateDate) {
        isLastUpdateDate = pIsLastUpdateDate;
    }

}
