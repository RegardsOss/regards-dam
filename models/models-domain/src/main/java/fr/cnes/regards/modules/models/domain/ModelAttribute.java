/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 *
 * A {@link ModelAttribute} is linked to a {@link Model}.<br/>
 * It contains the reference to a global {@link AttributeModel} and adds the capacity to define if its value is set
 * manually or calculated threw a calculation plugin.<<br/>
 * Thus, a same {@link AttributeModel} may be linked to different model and can either be set manually or calculated
 * depending the model.
 *
 * @author msordi
 *
 */
public class ModelAttribute implements Comparable<ModelAttribute> {

    /**
     * Common attribute model
     */
    private AttributeModel attribute;

    /**
     * Whether this attribute in computed or not
     */
    // TODO link to a calculation plugin
    private Boolean isCalculated = Boolean.FALSE;

    /**
     * Position (allows to sort attribute in model)
     */
    private Short position = 0;

    public AttributeModel getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeModel pAttribute) {
        attribute = pAttribute;
    }

    public Boolean getIsCalculated() {
        return isCalculated;
    }

    public void setIsCalculated(Boolean pIsCalculated) {
        isCalculated = pIsCalculated;
    }

    public Short getPosition() {
        return position;
    }

    public void setPosition(Short pPosition) {
        position = pPosition;
    }

    @Override
    public int compareTo(ModelAttribute pOther) {
        return this.position - pOther.getPosition();
    }

}
