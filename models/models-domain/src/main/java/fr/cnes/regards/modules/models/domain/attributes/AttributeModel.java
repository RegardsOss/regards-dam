/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.models.domain.attributes.restriction.IRestriction;

/**
 * @author msordi
 *
 */
public class AttributeModel {

    /**
     * Attribute name
     */
    @NotNull
    private String name;

    /**
     * Optional attribute description
     */
    private Optional<String> description;

    /**
     * Attribute type
     */
    @NotNull
    private AttributeType type;

    /**
     * Optional fragment
     */
    private Optional<Fragment> fragment;

    /**
     * Whether this attribute is a search criterion
     */
    @NotNull
    private Boolean isCriterion;

    /**
     * Whether this attribute is a facet<br/>
     * Only criterion attribute can be a facet!
     */
    private Boolean isFacet;

    /**
     * Whether this attribute can be alterate by users
     */
    private Boolean isAlterable;

    /**
     * Whether this attribute is optional
     */
    private Boolean isOptional;

    /**
     * Applicable restriction
     */
    private Optional<IRestriction> restriction;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        name = pName;
    }

    /**
     * @return the type
     */
    public AttributeType getType() {
        return type;
    }

    /**
     * @param pType
     *            the type to set
     */
    public void setType(AttributeType pType) {
        type = pType;
    }

    /**
     * @return the isCriterion
     */
    public Boolean getIsCriterion() {
        return isCriterion;
    }

    /**
     * @param pIsCriterion
     *            the isCriterion to set
     */
    public void setIsCriterion(Boolean pIsCriterion) {
        isCriterion = pIsCriterion;
    }

    public Boolean getIsFacet() {
        return isFacet;
    }

    /**
     * @param pIsFacet
     *            the isFacet to set
     */
    public void setIsFacet(Boolean pIsFacet) {
        isFacet = pIsFacet;
    }

    /**
     * @return the isAlterable
     */
    public Boolean getIsAlterable() {
        return isAlterable;
    }

    /**
     * @param pIsAlterable
     *            the isAlterable to set
     */
    public void setIsAlterable(Boolean pIsAlterable) {
        isAlterable = pIsAlterable;
    }

    /**
     * @return the isOptional
     */
    public Boolean getIsOptional() {
        return isOptional;
    }

    /**
     * @param pIsOptional
     *            the isOptional to set
     */
    public void setIsOptional(Boolean pIsOptional) {
        isOptional = pIsOptional;
    }

    /**
     * @return the fragment
     */
    public Optional<Fragment> getFragment() {
        return fragment;
    }

    /**
     * @param pFragment
     *            the fragment to set
     */
    public void setFragment(Optional<Fragment> pFragment) {
        fragment = pFragment;
    }

    /**
     * @return the description
     */
    public Optional<String> getDescription() {
        return description;
    }

    /**
     * @param pDescription
     *            the description to set
     */
    public void setDescription(Optional<String> pDescription) {
        description = pDescription;
    }

    /**
     * @return the restriction
     */
    public Optional<IRestriction> getRestriction() {
        return restriction;
    }

    /**
     * @param pRestriction
     *            the restriction to set
     */
    public void setRestriction(Optional<IRestriction> pRestriction) {
        restriction = pRestriction;
    }
}
