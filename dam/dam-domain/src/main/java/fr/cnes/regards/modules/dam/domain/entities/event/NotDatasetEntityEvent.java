package fr.cnes.regards.modules.dam.domain.entities.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.framework.urn.UniformResourceName;

/**
 * AbstractEntityEvent specialization for AbstractEntity other than Dataset
 * @author oroussel
 */
@Event(target = Target.MICROSERVICE)
public class NotDatasetEntityEvent extends AbstractEntityEvent {

    private NotDatasetEntityEvent() {
        super();
    }

    public NotDatasetEntityEvent(UniformResourceName... ipIds) {
        super(ipIds);
    }
}
