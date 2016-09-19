/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

/**
 * @author msordi
 *
 */
public class ModelAttribute {

    /**
     * Whether this attribute in computed or not
     */
    // TODO link to a calculation plugin
    private Boolean isCalculated_ = Boolean.FALSE;

    private AttributeModel attribute_;

    /**
     * @return the attribute
     */
    public AttributeModel getAttribute() {
        return attribute_;
    }

    /**
     * @param pAttribute
     *            the attribute to set
     */
    public void setAttribute(AttributeModel pAttribute) {
        attribute_ = pAttribute;
    }

    /**
     * @return the isCalculated
     */
    public Boolean getIsCalculated() {
        return isCalculated_;
    }

    /**
     * @param pIsCalculated
     *            the isCalculated to set
     */
    public void setIsCalculated(Boolean pIsCalculated) {
        isCalculated_ = pIsCalculated;
    }

}
