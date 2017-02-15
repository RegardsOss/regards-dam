/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;

/**
 * Event to be sent once an {@link AbstractAccessRight} is updated
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Event(target = Target.MICROSERVICE)
public class AccessRightUpdated extends AccessRightEvent {

    /**
     * @param pAccessRightId
     */
    public AccessRightUpdated(Long pAccessRightId) {
        super(pAccessRightId);
    }

}
