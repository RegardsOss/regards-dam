package fr.cnes.regards.modules.entities.domain.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

@Event(target = Target.MICROSERVICE)
public class CreateEntityEvent extends AbstractEntityEvent {

    public CreateEntityEvent() {
    }

    public CreateEntityEvent(UniformResourceName pIpId) {
        super(pIpId);
    }

}
