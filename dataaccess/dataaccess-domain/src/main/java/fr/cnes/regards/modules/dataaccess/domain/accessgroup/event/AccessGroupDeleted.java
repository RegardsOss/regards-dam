/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessgroup.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;

/**
 * Event to be sent once an {@link AccessGroup} is deleted
 *
 * @author Xavier-Alexandre Brochard
 */
@Event(target = Target.MICROSERVICE)
public class AccessGroupDeleted extends AbstractAccessGroupEvent {

    /**
     * @param pAccessGroup
     */
    public AccessGroupDeleted(AccessGroup pAccessGroup) {
        super(pAccessGroup);
    }

}
