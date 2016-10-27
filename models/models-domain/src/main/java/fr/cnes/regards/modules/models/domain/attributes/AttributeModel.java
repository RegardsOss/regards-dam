/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes;

import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionType;

/**
 * @author msordi
 *
 */
@Entity
@Table(name = "T_ATT_MODEL")
@SequenceGenerator(name = "attModelSequence", initialValue = 1, sequenceName = "SEQ_ATT_MODEL")
public class AttributeModel implements IIdentifiable<Long> {

    /**
     * Internal identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attModelSequence")
    private Long id;

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

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType pType) {
        type = pType;
    }

    public Boolean getIsCriterion() {
        return isCriterion;
    }

    public void setIsCriterion(Boolean pIsCriterion) {
        isCriterion = pIsCriterion;
    }

    public Boolean getIsFacet() {
        return isFacet;
    }

    public void setIsFacet(Boolean pIsFacet) {
        isFacet = pIsFacet;
    }

    public Boolean getIsAlterable() {
        return isAlterable;
    }

    public void setIsAlterable(Boolean pIsAlterable) {
        isAlterable = pIsAlterable;
    }

    public Boolean getIsOptional() {
        return isOptional;
    }

    public void setIsOptional(Boolean pIsOptional) {
        isOptional = pIsOptional;
    }

    public Optional<Fragment> getFragment() {
        return fragment;
    }

    public void setFragment(Optional<Fragment> pFragment) {
        fragment = pFragment;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public void setDescription(Optional<String> pDescription) {
        description = pDescription;
    }

    public Optional<IRestriction> getRestriction() {
        return restriction;
    }

    public void setRestriction(Optional<IRestriction> pRestriction) {
        restriction = pRestriction;
    }

    public Boolean hasRestriction() {
        return (restriction != null) && restriction.isPresent()
                && !restriction.get().getType().equals(RestrictionType.NO_RESTRICTION);
    }
}
