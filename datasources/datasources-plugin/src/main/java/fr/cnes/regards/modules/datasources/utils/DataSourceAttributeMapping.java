/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.utils;

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
     * Default constructor
     */
    public DataSourceAttributeMapping() {
        super();
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
        super();
        this.name = pName;
        this.type = pType;
        this.nameSpace = pNameSpace;
        this.nameDS = pMappingDS;
        this.typeDS = pTypeDS;
    }

    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMappingDS, int pTypeDS) {
        super();
        this.name = pName;
        this.type = pType;
        this.nameDS = pMappingDS;
        this.typeDS = pTypeDS;
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
     */
    public DataSourceAttributeMapping(String pName, String pNameSpace, AttributeType pType, String pMappingDS) {
        super();
        this.name = pName;
        this.type = pType;
        this.nameSpace = pNameSpace;
        this.nameDS = pMappingDS;
    }

    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMappingDS) {
        super();
        this.name = pName;
        this.type = pType;
        this.nameDS = pMappingDS;
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

    public void setTypeDS(Integer typeDS) {
        this.typeDS = typeDS;
    }

}
