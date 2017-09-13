package fr.cnes.regards.modules.entities.domain.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;

/**
 * AbstractEntityEvent specialization for AbstractEntity other than Dataset
 * @author oroussel
 */
@Event(target = Target.MICROSERVICE)
public class NotDatasetEntityEvent extends AbstractEntityEvent {

    private NotDatasetEntityEvent() {
        super();
    }

    public NotDatasetEntityEvent(UniformResourceName... pIpIds) {
        super(pIpIds);
    }
}
