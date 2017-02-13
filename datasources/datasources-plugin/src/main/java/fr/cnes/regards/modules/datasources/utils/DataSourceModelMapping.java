/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.utils;

import java.util.List;

import fr.cnes.regards.modules.models.domain.Model;

/**
 * This class is used to map a data source to a {@link Model}
 * 
 * @author Christophe Mertz
 *
 */
public class DataSourceModelMapping {

    /**
     * The name of the model @see {@link Model}
     */
    private String modelName;

    /**
     * The mapping between the attribute of the {@link Model} of the attributes of th data source
     */
    private List<DataSourceAttributeMapping> attributesMapping;

    public DataSourceModelMapping() {
        super();
    }

    public DataSourceModelMapping(String pModelName, List<DataSourceAttributeMapping> pAttributesMapping) {
        super();
        this.modelName = pModelName;
        this.attributesMapping = pAttributesMapping;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String pModelName) {
        this.modelName = pModelName;
    }

    public List<DataSourceAttributeMapping> getAttributesMapping() {
        return attributesMapping;
    }

    public void setAttributesMapping(List<DataSourceAttributeMapping> pAttributesMapping) {
        this.attributesMapping = pAttributesMapping;
    }

}
