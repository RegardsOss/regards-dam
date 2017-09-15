package fr.cnes.regards.modules.entities.domain.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;

/**
 * Broadcast entity event to be sent to all microservices
 * @author oroussel
 */
@Event(target = Target.ALL)
public class BroadcastEntityEvent implements ISubscribable {

    /**
     * Business id identifying an entity
     */
    private UniformResourceName[] ipIds;

    private EventType eventType;

    private BroadcastEntityEvent() {
        super();
    }

    public BroadcastEntityEvent(EventType pEventType, UniformResourceName... pIpIds) {
        this();
        eventType = pEventType;
        ipIds = pIpIds;
    }

    public UniformResourceName[] getIpIds() {
        return ipIds;
    }

    @SuppressWarnings("unused")
    private void setIpIds(UniformResourceName... pIpIds) {
        ipIds = pIpIds;
    }

    public EventType getEventType() {
        return eventType;
    }

    @SuppressWarnings("unused")
    private void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
