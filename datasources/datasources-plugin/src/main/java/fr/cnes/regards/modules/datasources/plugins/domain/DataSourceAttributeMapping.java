/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.domain;

import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * This class is used to map a datasource to a {@link Model}
 * 
 * @author Christophe Mertz
 *
 */
public class DataSourceAttributeMapping {

    /**
     * Attribute name
     */
    private String name;

    /**
     * Attribute type
     */
    private AttributeType type;

    /**
     * Attribute namespace
     */
    private String nameSpace;

    /**
     * This attribute is the attribute name in the datasource
     */
    private String mapping;

    /**
     * True if this attribute can be used to find new data in the data source
     */
    private Boolean isAttributeComprator;

    /**
     * Default constructor
     */
    public DataSourceAttributeMapping() {
    }

    /**
     * Constructor with all attributes
     * 
     * @param pName
     *            the name
     * @param pType
     *            the {@link AttributeType}
     * @param pMapping
     *            the mapping
     * @param pNameSpace
     *            the attribute name space
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMapping, String pNameSpace) {
        super();
        this.name = pName;
        this.type = pType;
        this.nameSpace = pNameSpace;
        this.mapping = pMapping;
    }
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMapping, String pNameSpace, boolean pIsComparable) {
        super();
        this.name = pName;
        this.type = pType;
        this.nameSpace = pNameSpace;
        this.mapping = pMapping;
        this.isAttributeComprator = pIsComparable;
    }

    /**
     * Constructor with specific attributes
     * 
     * @param pName
     *            the name
     * @param pType
     *            the {@link AttributeType}
     * @param pMapping
     *            the mapping
     */
    public DataSourceAttributeMapping(String pName, AttributeType pType, String pMapping) {
        super();
        this.name = pName;
        this.type = pType;
        this.mapping = pMapping;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public Boolean isAttributeComprator() {
        return isAttributeComprator;
    }

    public void setIsAttributeComprator(Boolean isAttributeComprator) {
        this.isAttributeComprator = isAttributeComprator;
    }

}
