/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator;

import org.springframework.validation.Validator;

import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;

/**
 *
 * @author Marc Sordi
 *
 */
public abstract class AbstractAttributeValidator implements Validator {

    /**
     * Error code for inconsistent attribute type
     */
    protected static final String INCONSISTENT_ATTRIBUTE = "error.inconsistent.attribute.type";

    /**
     * Attribute key
     */
    protected final String attributeKey;

    public AbstractAttributeValidator(String pAttributeKey) {
        this.attributeKey = pAttributeKey;
    }

    @Override
    public boolean supports(Class<?> pClazz) {
        return AbstractAttribute.class.isAssignableFrom(pClazz);
    }
}
