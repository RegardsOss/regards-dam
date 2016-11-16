/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service.exception;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;

/**
 * Occurs if model attribute not bound to given model
 *
 * @author Marc Sordi
 *
 */
public class UnexpectedModelAttributeException extends ModuleException {

    /**
     *
     */
    private static final long serialVersionUID = -6692699923716998197L;

    public UnexpectedModelAttributeException(Long pModelId, Long pModelAttributeId) {
        super(String.format("Model attribute %s is not bound to model %s.", pModelAttributeId, pModelId));
    }

}
