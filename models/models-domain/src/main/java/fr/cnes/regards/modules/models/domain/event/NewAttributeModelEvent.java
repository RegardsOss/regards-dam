/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.event;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * 
 * @author Marc Sordi
 *
 */
public class NewAttributeModelEvent {

    /**
     * {@link AttributeModel} name
     */
    private String attributeName;

    /**
     * {@link AttributeType}
     */
    private AttributeType attributeType;

    public NewAttributeModelEvent(AttributeModel pAttributeModel) {
        this.setAttributeName(pAttributeModel.getName());
        this.setAttributeType(pAttributeModel.getType());
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType pAttributeType) {
        attributeType = pAttributeType;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String pAttributeName) {
        attributeName = pAttributeName;
    }
}
