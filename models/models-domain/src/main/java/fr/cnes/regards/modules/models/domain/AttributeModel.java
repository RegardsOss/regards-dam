/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

/**
 * @author msordi
 *
 */
public class AttributeModel {

    /**
     * Attribute name
     */
    private String name_;

    /**
     * Optional attribute description
     */
    private String description_;

    /**
     * Attribute type
     */
    private AttributeType type_;

    /**
     * Optional namespace
     */
    private String namespace_ = null;

    /**
     * Whether this attribute is a search criterion
     */
    private Boolean isCriterion_;

    /**
     * Whether this attribute is a facet<br/>
     * Only criterion attribute can be a facet!
     */
    private Boolean isFacet_;

    /**
     * @return the name
     */
    public String getName() {
        return name_;
    }

    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        name_ = pName;
    }

    /**
     * @return the type
     */
    public AttributeType getType() {
        return type_;
    }

    /**
     * @param pType
     *            the type to set
     */
    public void setType(AttributeType pType) {
        type_ = pType;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace_;
    }

    /**
     * @param pNamespace
     *            the namespace to set
     */
    public void setNamespace(String pNamespace) {
        namespace_ = pNamespace;
    }

    /**
     * @return the isCriterion
     */
    public Boolean getIsCriterion() {
        return isCriterion_;
    }

    /**
     * @param pIsCriterion
     *            the isCriterion to set
     */
    public void setIsCriterion(Boolean pIsCriterion) {
        isCriterion_ = pIsCriterion;
    }

    /**
     * @return the isFacet
     */
    public Boolean getIsFacet() {
        return isFacet_;
    }

    /**
     * @param pIsFacet
     *            the isFacet to set
     */
    public void setIsFacet(Boolean pIsFacet) {
        isFacet_ = pIsFacet;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description_;
    }

    /**
     * @param pDescription
     *            the description to set
     */
    public void setDescription(String pDescription) {
        description_ = pDescription;
    }
}
