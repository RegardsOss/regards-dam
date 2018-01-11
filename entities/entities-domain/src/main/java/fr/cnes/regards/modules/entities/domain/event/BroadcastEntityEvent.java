package fr.cnes.regards.modules.entities.domain.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;

/**
 * Broadcast entity event to be sent to all microservices (in fact one per microservice type)
 * @author oroussel
 */
@Event(target = Target.ONE_PER_MICROSERVICE_TYPE)
public class BroadcastEntityEvent implements ISubscribable {

    /**
     * Business id identifying an entity
     */
    private UniformResourceName[] ipIds;

    private EventType eventType;

    private BroadcastEntityEvent() {
        super();
    }

    public BroadcastEntityEvent(EventType eventType, UniformResourceName... ipIds) {
        this();
        this.eventType = eventType;
        this.ipIds = ipIds;
    }

    public UniformResourceName[] getIpIds() {
        return ipIds;
    }

    @SuppressWarnings("unused")
    private void setIpIds(UniformResourceName... ipIds) {
        this.ipIds = ipIds;
    }

    public EventType getEventType() {
        return eventType;
    }

    @SuppressWarnings("unused")
    private void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
