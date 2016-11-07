/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.exception;

/**
 *
 * If a conflict is detected
 *
 * @author Marc Sordi
 *
 */
public final class AlreadyExistsException extends ModelException {

    private static final long serialVersionUID = -8722047726544333700L;

    public AlreadyExistsException(String pErrorMessage) {
        super(pErrorMessage);
    }
}
