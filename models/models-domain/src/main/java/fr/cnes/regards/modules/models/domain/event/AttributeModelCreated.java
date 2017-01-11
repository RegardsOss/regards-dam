/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.event;

import fr.cnes.regards.framework.amqp.event.EventProperties;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * Creation event
 *
 * @author Marc Sordi
 *
 */
@EventProperties(target = Target.MICROSERVICE)
public class AttributeModelCreated extends AbstractAttributeModelEvent {

    public AttributeModelCreated(AttributeModel pAttributeModel) {
        super(pAttributeModel);
    }
}
