/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Access right event.
 *
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Event(target = Target.ALL)
public class AccessRightEvent implements ISubscribable {

    private UniformResourceName datasetIpId;

    private AccessRightEventType eventType;

    @SuppressWarnings("unused")
    private AccessRightEvent() {
        super();
    }

    public AccessRightEvent(UniformResourceName datasetIpId, AccessRightEventType eventType) {
        this.datasetIpId = datasetIpId;
        this.eventType = eventType;
    }

    public UniformResourceName getDatasetIpId() {
        return datasetIpId;
    }

    public AccessRightEventType getEventType() {
        return eventType;
    }
}
