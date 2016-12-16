/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright.event;

import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;

/**
 * Abstract Event about {@link AbstractAccessRight}.
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class AccessRightEvent {

    @NotNull
    private final Long accessRightId;

    public AccessRightEvent(Long pAccessRightId) {
        accessRightId = pAccessRightId;
    }

}
