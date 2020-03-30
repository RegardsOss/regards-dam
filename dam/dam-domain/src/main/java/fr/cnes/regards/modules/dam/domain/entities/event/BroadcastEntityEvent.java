package fr.cnes.regards.modules.dam.domain.entities.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.framework.urn.UniformResourceName;

/**
 * Broadcast entity event to be sent to all microservices (in fact one per microservice type)
 * @author oroussel
 */
@Event(target = Target.ONE_PER_MICROSERVICE_TYPE)
public class BroadcastEntityEvent implements ISubscribable {

    /**
     * Business id identifying an entity
     */
    private UniformResourceName[] aipIds;

    private EventType eventType;

    private BroadcastEntityEvent() {
        super();
    }

    public BroadcastEntityEvent(EventType eventType, UniformResourceName... aipIds) {
        this();
        this.eventType = eventType;
        this.aipIds = aipIds;
    }

    public UniformResourceName[] getAipIds() {
        return aipIds;
    }

    @SuppressWarnings("unused")
    private void setIpIds(UniformResourceName... aipIds) {
        this.aipIds = aipIds;
    }

    public EventType getEventType() {
        return eventType;
    }

    @SuppressWarnings("unused")
    private void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
