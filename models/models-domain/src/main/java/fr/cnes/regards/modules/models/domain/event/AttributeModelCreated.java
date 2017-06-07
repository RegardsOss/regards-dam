/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * Creation event
 *
 * @author Marc Sordi
 *
 */
@Event(target = Target.ALL)
public class AttributeModelCreated extends AbstractAttributeModelEvent {

    public AttributeModelCreated() {
        // Json constructor
    }

    public AttributeModelCreated(AttributeModel pAttributeModel) {
        super(pAttributeModel);
    }
}
