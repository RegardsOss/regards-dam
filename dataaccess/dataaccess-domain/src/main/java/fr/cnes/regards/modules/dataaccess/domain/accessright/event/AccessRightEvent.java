/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright.event;

import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;

/**
 * Abstract Event about {@link AccessRight}.
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class AccessRightEvent implements ISubscribable {

    @NotNull
    private final Long accessRightId;

    public AccessRightEvent(Long pAccessRightId) {
        accessRightId = pAccessRightId;
    }

}
