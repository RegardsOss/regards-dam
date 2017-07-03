/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright.event;

import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Access right event.
 *
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
public class AccessRightEvent implements ISubscribable {

    private final UniformResourceName datasetIpId;

    private final AccessRightEventType eventType;

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
