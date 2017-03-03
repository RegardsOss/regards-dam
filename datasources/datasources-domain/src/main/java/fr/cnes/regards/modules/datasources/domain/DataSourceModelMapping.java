/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.domain;

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
     * The {@link Model} identifier
     */
    private Long model;

    /**
     * The mapping between the attribute of the {@link Model} of the attributes of th data source
     */
    private List<DataSourceAttributeMapping> attributesMapping;

    public DataSourceModelMapping() {
        super();
    }

    public DataSourceModelMapping(Long pModelId, List<DataSourceAttributeMapping> pAttributesMapping) {
        super();
        this.model = pModelId;
        this.attributesMapping = pAttributesMapping;
    }

    public Long getModel() {
        return model;
    }

    public void setModel(Long pModel) {
        this.model = pModel;
    }

    public List<DataSourceAttributeMapping> getAttributesMapping() {
        return attributesMapping;
    }

    public void setAttributesMapping(List<DataSourceAttributeMapping> pAttributesMapping) {
        this.attributesMapping = pAttributesMapping;
    }

}
