/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

import org.springframework.validation.annotation.Validated;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Validated
public class ModelAttributeValue {

    private AttributeType type;

    private Object value;

    public ModelAttributeValue(AttributeType pType, Object pValue) {
        super();
        type = pType;
        value = pValue;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType pType) {
        type = pType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object pValue) {
        value = pValue;
    }

}
