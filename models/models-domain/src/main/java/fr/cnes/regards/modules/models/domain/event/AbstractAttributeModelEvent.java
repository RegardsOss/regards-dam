/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.event;

import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 * {@link AttributeModel} event common information
 *
 * @author Marc Sordi
 *
 */
public abstract class AbstractAttributeModelEvent implements ISubscribable {

    /**
     * {@link Fragment} name
     */
    private String fragmentName;

    /**
     * {@link AttributeModel} name
     */
    private String attributeName;

    /**
     * {@link AttributeType}
     */
    private AttributeType attributeType;

    public AbstractAttributeModelEvent() {
    }

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

    public void setFragmentName(String pFragmentName) {
        fragmentName = pFragmentName;
    }

    public void setAttributeName(String pAttributeName) {
        attributeName = pAttributeName;
    }

    public void setAttributeType(AttributeType pAttributeType) {
        attributeType = pAttributeType;
    }

}
