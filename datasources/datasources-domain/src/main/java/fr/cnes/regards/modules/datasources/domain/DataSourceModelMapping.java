/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.domain;

import java.util.List;

import com.google.gson.annotations.JsonAdapter;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * This class is used to map a data source to a {@link Model}
 * 
 * @author Christophe Mertz
 *
 */
@JsonAdapter(value = ModelMappingAdapter.class)
public class DataSourceModelMapping {

    /**
     * The {@link Model} identifier
     */
    private Long model;

    /**
     * The mapping between the attribute of the {@link Model} of the attributes of th data source
     */
    private List<AbstractAttributeMapping> attributesMapping;

    public DataSourceModelMapping() {
        super();
    }

    public DataSourceModelMapping(Long pModelId, List<AbstractAttributeMapping> pAttributesMapping) {
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

    public List<AbstractAttributeMapping> getAttributesMapping() {
        return attributesMapping;
    }

    public void setAttributesMapping(List<AbstractAttributeMapping> pAttributesMapping) {
        this.attributesMapping = pAttributesMapping;
    }

}
