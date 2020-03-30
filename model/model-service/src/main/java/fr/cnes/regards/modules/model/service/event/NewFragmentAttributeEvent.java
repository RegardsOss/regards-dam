package fr.cnes.regards.modules.model.service.event;

import org.springframework.context.ApplicationEvent;

import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;

/**
 * Event navigating inside the microservice instance to be notified when a new attribute is created on a fragment so we can handle this properly in IModelAttrAssocService.
 * AttributeModel is into source attribute from inherited classes
 *
 * @author Sylvain VISSIERE-GUERINET
 */
@SuppressWarnings("serial")
public class NewFragmentAttributeEvent extends ApplicationEvent {

    /**
     * Constructor
     * @param attributeAdded
     */
    public NewFragmentAttributeEvent(AttributeModel attributeAdded) {
        super(attributeAdded);
    }

}
