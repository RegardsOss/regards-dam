/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes;

import javax.persistence.Column;
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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.FloatRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionType;
import fr.cnes.regards.modules.models.domain.xml.IXmlisable;
import fr.cnes.regards.modules.models.schema.Attribute;
import fr.cnes.regards.modules.models.schema.Restriction;

/**
 * @author msordi
 *
 */
@Entity
@Table(name = "T_ATT_MODEL", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "fragment_id" }))
@SequenceGenerator(name = "attModelSequence", initialValue = 1, sequenceName = "SEQ_ATT_MODEL")
public class AttributeModel implements IIdentifiable<Long>, IXmlisable<Attribute> {

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
    @Pattern(regexp = Model.NAME_REGEXP, message = "Attribute name must conform to regular expression \""
            + Model.NAME_REGEXP + "\".")
    @Size(min = Model.NAME_MIN_SIZE, max = Model.NAME_MAX_SIZE, message = "Attribute name must be between "
            + Model.NAME_MIN_SIZE + " and " + Model.NAME_MAX_SIZE + " length.")
    @Column(nullable = false, updatable = false)
    private String name;

    /**
     * Optional attribute description
     */
    private String description;

    /**
     * Attribute type
     */
    @NotNull
    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private AttributeType type;

    /**
     * Optional fragment
     */
    @ManyToOne
    // CHECKSTYLE:OFF
    @JoinColumn(name = "fragment_id", foreignKey = @ForeignKey(name = "FRAGMENT_ID_FK"), nullable = false, updatable = false)
    // CHECKSTYLE:ON
    private Fragment fragment;

    /**
     * Whether this attribute is a search criterion
     */
    @Column(nullable = false)
    private boolean queryable;

    /**
     * Whether this attribute can be used for facet<br/>
     * Only queryable attribute can be a facet!
     */
    private boolean facetable;

    /**
     * Whether this attribute can be alterate by users
     */
    private boolean alterable;

    /**
     * Whether this attribute is optional
     */
    private boolean optional;

    /**
     * Applicable restriction
     */
    @OneToOne(orphanRemoval = true)
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

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment pFragment) {
        fragment = pFragment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public AbstractRestriction getRestriction() {
        return restriction;
    }

    public void setRestriction(AbstractRestriction pRestriction) {
        restriction = pRestriction;
    }

    public Boolean hasRestriction() {
        return (restriction != null) && !restriction.getType().equals(RestrictionType.NO_RESTRICTION);
    }

    @Override
    public boolean equals(Object pObj) {
        if (pObj instanceof AttributeModel) {
            final AttributeModel attmod = (AttributeModel) pObj;
            boolean hasSameFragment = true;
            if (fragment != null) {
                hasSameFragment = fragment.equals(attmod.getFragment());
            }
            return attmod.getName().equals(name) && hasSameFragment;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (fragment != null) {
            return name.hashCode() + fragment.hashCode();
        }
        return name.hashCode();
    }

    public boolean isAlterable() {
        return alterable;
    }

    public void setAlterable(boolean pAlterable) {
        alterable = pAlterable;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean pOptional) {
        optional = pOptional;
    }

    public boolean isQueryable() {
        return queryable;
    }

    public void setQueryable(boolean pQueryable) {
        queryable = pQueryable;
    }

    public boolean isFacetable() {
        return facetable;
    }

    public void setFacetable(boolean pFacetable) {
        facetable = pFacetable;
    }

    @Override
    public Attribute toXml() {
        final Attribute xmlAtt = new Attribute();
        xmlAtt.setName(name);
        xmlAtt.setDescription(description);
        xmlAtt.setAlterable(alterable);
        xmlAtt.setFacetable(facetable);
        xmlAtt.setOptional(optional);
        xmlAtt.setQueryable(queryable);
        if (restriction != null) {
            xmlAtt.setRestriction(restriction.toXml());
        }
        xmlAtt.setType(type.toString());
        return xmlAtt;
    }

    @Override
    public void fromXml(Attribute pXmlElement) {
        setName(pXmlElement.getName());
        setDescription(pXmlElement.getDescription());
        setAlterable(pXmlElement.isAlterable());
        setFacetable(pXmlElement.isFacetable());
        setOptional(pXmlElement.isOptional());
        setQueryable(pXmlElement.isQueryable());
        if (pXmlElement.getRestriction() != null) {
            final Restriction xmlRestriction = pXmlElement.getRestriction();
            if (xmlRestriction.getEnumeration() != null) {
                restriction = new EnumerationRestriction();
            } else
                if (xmlRestriction.getFloatRange() != null) {
                    restriction = new FloatRangeRestriction();
                } else
                    if (xmlRestriction.getIntegerRange() != null) {
                        restriction = new IntegerRangeRestriction();
                    } else
                        if (xmlRestriction.getPattern() != null) {
                            restriction = new PatternRestriction();
                        }

            // Cause null pointer exception if implementation not consistent with XSD
            restriction.fromXml(pXmlElement.getRestriction());
        }
        setType(AttributeType.valueOf(pXmlElement.getType()));
    }
}
