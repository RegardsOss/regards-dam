/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service.exception;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;

/**
 * Occurs if an attribute is part of a fragment but shouldn't be
 *
 * @author Marc Sordi
 *
 */
public class FragmentAttributeException extends ModuleException {

    private static final long serialVersionUID = -8782744824297669779L;

    public FragmentAttributeException(Long pAttributeId) {
        super(String.format("Cannot (un)bind attribute %s that is part of a fragment. Try to (un)bind the fragment!",
                            pAttributeId));
    }

}
