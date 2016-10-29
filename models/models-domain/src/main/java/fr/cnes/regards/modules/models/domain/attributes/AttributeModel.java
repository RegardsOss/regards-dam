/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes;

import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
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
    private String description;

    /**
     * Attribute type
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private AttributeType type;

    /**
     * Optional fragment
     */
    @ManyToOne
    @JoinColumn(name = "fragment_id", foreignKey = @ForeignKey(name = "FRAGMENT_ID_FK"))
    private Fragment fragment;

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
    @OneToOne
    @JoinColumn(name = "restriction_id", foreignKey = @ForeignKey(name = "RESTRICTION_ID_FK"))
    private AbstractRestriction restriction;

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
        return Optional.ofNullable(fragment);
    }

    public void setFragment(Fragment pFragment) {
        fragment = pFragment;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public Optional<IRestriction> getRestriction() {
        return Optional.ofNullable(restriction);
    }

    public void setRestriction(AbstractRestriction pRestriction) {
        restriction = pRestriction;
    }

    public Boolean hasRestriction() {
        return (restriction != null) && !restriction.getType().equals(RestrictionType.NO_RESTRICTION);
    }

    @Override
    public boolean equals(Object pObj) {
        Boolean result = Boolean.FALSE;
        if (pObj instanceof AttributeModel) {
            final AttributeModel attmod = (AttributeModel) pObj;
            result = attmod.getName().equals(name);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
