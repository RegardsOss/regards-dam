/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.event;

import fr.cnes.regards.framework.amqp.event.ISubscribableEvent;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 * {@link AttributeModel} event common information
 *
 * @author Marc Sordi
 *
 */
public abstract class AbstractAttributeModelEvent implements ISubscribableEvent {

    /**
     * {@link Fragment} name
     */
    private final String fragmentName;

    /**
     * {@link AttributeModel} name
     */
    private final String attributeName;

    /**
     * {@link AttributeType}
     */
    private final AttributeType attributeType;

    public AbstractAttributeModelEvent(AttributeModel pAttributeModel) {
        this.fragmentName = pAttributeModel.getFragment().getName();
        this.attributeName = pAttributeModel.getName();
        this.attributeType = pAttributeModel.getType();
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getFragmentName() {
        return fragmentName;
    }

}
