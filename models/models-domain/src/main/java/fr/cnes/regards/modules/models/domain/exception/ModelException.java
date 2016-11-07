/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.exception;

/**
 * Main exception
 *
 * @author Marc Sordi
 *
 */
public class ModelException extends Exception {

    private static final long serialVersionUID = 3000913251286413787L;

    public ModelException(String pErrorMessage) {
        super(pErrorMessage);
    }
}
