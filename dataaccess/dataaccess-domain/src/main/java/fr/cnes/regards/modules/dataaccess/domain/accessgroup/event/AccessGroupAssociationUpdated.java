/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessgroup.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;

/**
 * Event to be sent when a user is associated/dissociated from an {@link AccessGroup}
 *
 * @author Xavier-Alexandre Brochard
 */
@Event(target = Target.MICROSERVICE)
public class AccessGroupAssociationUpdated extends AbstractAccessGroupEvent {

    /**
     * @param pAccessGroup
     */
    public AccessGroupAssociationUpdated(AccessGroup pAccessGroup) {
        super(pAccessGroup);
    }

}
