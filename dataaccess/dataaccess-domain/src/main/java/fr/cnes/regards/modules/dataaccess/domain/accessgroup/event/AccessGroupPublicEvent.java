/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessgroup.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;

/**
 * Published when a public group is defined or removed
 * @author Marc Sordi
 *
 */
@Event(target = Target.ALL)
public class AccessGroupPublicEvent implements ISubscribable {

    /**
     * The source of the event
     */
    private AccessGroup accessGroup;

    public AccessGroupPublicEvent() {
        // Deserialization constructor
    }

    public AccessGroupPublicEvent(AccessGroup pAccessGroup) {
        accessGroup = pAccessGroup;
    }

    public AccessGroup getAccessGroup() {
        return accessGroup;
    }

    public void setAccessGroup(AccessGroup pAccessGroup) {
        accessGroup = pAccessGroup;
    }

}
