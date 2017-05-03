/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 *
 * Deletion event
 *
 * @author Marc Sordi
 *
 */
@Event(target = Target.MICROSERVICE)
public class AttributeModelDeleted extends AbstractAttributeModelEvent {

    public AttributeModelDeleted() {
        // Json constructor
    }

    public AttributeModelDeleted(AttributeModel pAttributeModel) {
        super(pAttributeModel);
    }
}
