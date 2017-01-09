/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.DoubleRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionType;
import fr.cnes.regards.modules.models.domain.xml.IXmlisable;
import fr.cnes.regards.modules.models.schema.Attribute;
import fr.cnes.regards.modules.models.schema.Property;
import fr.cnes.regards.modules.models.schema.Restriction;
import fr.cnes.regards.modules.models.schema.Type;

/**
 * @author msordi
 *
 */
@Entity
@Table(name = "t_attribute_model", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "fragment_id" }))
@SequenceGenerator(name = "attModelSequence", initialValue = 1, sequenceName = "seq_att_model")
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
    @Column(nullable = false, updatable = false, length = Model.NAME_MAX_SIZE)
    private String name;

    /**
     * Optional attribute description
     */
    @Column
    @org.hibernate.annotations.Type(type = "text")
    private String description;

    /**
     * Default value
     */
    private String defaultValue;

    /**
     * Attribute type
     */
    @NotNull
    @Column(length = 32, nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private AttributeType type;

    /**
     * Unit useful for number based attributes
     */
    @Column(length = 16)
    private String unit;

    /**
     * Precision useful for double based attributes
     */
    @Column
    private Integer precision;

    /**
     * Array size useful for array based attributes
     */
    @Column
    private Integer arraysize;

    /**
     * Optional fragment
     */
    @ManyToOne
    @JoinColumn(name = "fragment_id", foreignKey = @ForeignKey(name = "fk_fragment_id"), nullable = false, updatable = false)
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
    @Column
    private boolean facetable;

    /**
     * Whether this attribute can be alterate by users
     */
    @Column
    private boolean alterable;

    /**
     * Whether this attribute is optional
     */
    @Column
    private boolean optional;

    /**
     * Applicable restriction
     */
    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "restriction_id", foreignKey = @ForeignKey(name = "fk_restriction_id"))
    @Valid
    private AbstractRestriction restriction;

    /**
     * Optional group for displaying purpose
     */
    @Pattern(regexp = Model.NAME_REGEXP, message = "Group name must conform to regular expression \""
            + Model.NAME_REGEXP + "\".")
    @Size(min = Model.NAME_MIN_SIZE, max = Model.NAME_MAX_SIZE, message = "Group name must be between "
            + Model.NAME_MIN_SIZE + " and " + Model.NAME_MAX_SIZE + " length.")
    @Column(name = "group_name", length = Model.NAME_MAX_SIZE)
    private String group;

    /**
     * Custom attribute properties
     */
    @Valid
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", foreignKey = @ForeignKey(name = "fk_att_ppty_att"))
    private List<AttributeProperty> properties;

    /**
     * Reference of a root attribute (i.e. in default fragment). Identical to name.
     */
    // @Pattern(regexp = Model.NAME_REGEXP, message = "Attribute name reference must conform to regular expression \""
    // + Model.NAME_REGEXP + "\".")
    // @Size(min = Model.NAME_MIN_SIZE, max = Model.NAME_MAX_SIZE, message = "Attribute name reference must be between "
    // + Model.NAME_MIN_SIZE + " and " + Model.NAME_MAX_SIZE + " length.")
    // @Column(name = "refname", length = Model.NAME_MAX_SIZE)
    // private String ref;

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

    public String getGroup() {
        return group;
    }

    public void setGroup(String pGroup) {
        group = pGroup;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String pUnit) {
        unit = pUnit;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer pPrecision) {
        precision = pPrecision;
    }

    public Integer getArraysize() {
        return arraysize;
    }

    public void setArraysize(Integer pArraysize) {
        arraysize = pArraysize;
    }

    @Override
    public Attribute toXml() {
        final Attribute xmlAtt = new Attribute();
        xmlAtt.setName(name);
        xmlAtt.setDescription(description);
        xmlAtt.setDefaultValue(defaultValue);
        xmlAtt.setAlterable(alterable);
        xmlAtt.setFacetable(facetable);
        xmlAtt.setOptional(optional);
        xmlAtt.setQueryable(queryable);
        if (restriction != null) {
            xmlAtt.setRestriction(restriction.toXml());
        }
        Type xmlType = new Type();
        if (arraysize != null) {
            xmlType.setArraysize(BigInteger.valueOf(arraysize));
        }
        if (precision != null) {
            xmlType.setPrecision(BigInteger.valueOf(precision));
        }
        xmlType.setUnit(unit);
        xmlType.setValue(fr.cnes.regards.modules.models.schema.RestrictionType.valueOf(type.toString()));
        xmlAtt.setType(xmlType);
        xmlAtt.setGroup(group);

        if (properties != null) {
            for (AttributeProperty ppty : properties) {
                Property xmlProperty = new Property();
                xmlProperty.setKey(ppty.getKey());
                xmlProperty.setValue(ppty.getValue());
                xmlAtt.getProperty().add(xmlProperty);
            }
        }
        return xmlAtt;
    }

    @Override
    public void fromXml(Attribute pXmlElement) {
        setName(pXmlElement.getName());
        setDescription(pXmlElement.getDescription());
        setDefaultValue(pXmlElement.getDefaultValue());
        setAlterable(pXmlElement.isAlterable());
        setFacetable(pXmlElement.isFacetable());
        setOptional(pXmlElement.isOptional());
        setQueryable(pXmlElement.isQueryable());
        if (pXmlElement.getRestriction() != null) {
            final Restriction xmlRestriction = pXmlElement.getRestriction();
            if (xmlRestriction.getEnumeration() != null) {
                restriction = new EnumerationRestriction();
            } else
                if (xmlRestriction.getDoubleRange() != null) {
                    restriction = new DoubleRangeRestriction();
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
        Type xmlType = pXmlElement.getType();
        if (xmlType.getArraysize() != null) {
            setArraysize(xmlType.getArraysize().intValueExact());
        }
        if (xmlType.getPrecision() != null) {
            setPrecision(xmlType.getPrecision().intValueExact());
        }
        setUnit(xmlType.getUnit());
        setType(AttributeType.valueOf(xmlType.getValue().toString()));
        setGroup(pXmlElement.getGroup());

        if (!pXmlElement.getProperty().isEmpty()) {
            List<AttributeProperty> ppts = new ArrayList<>();
            for (Property xmlProperty : pXmlElement.getProperty()) {
                AttributeProperty attPpty = new AttributeProperty();
                attPpty.setKey(xmlProperty.getKey());
                attPpty.setValue(xmlProperty.getValue());
                ppts.add(attPpty);
            }
            setProperties(ppts);
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String pDefaultValue) {
        defaultValue = pDefaultValue;
    }

    // public String getRef() {
    // return ref;
    // }
    //
    // public void setRef(String pRef) {
    // ref = pRef;
    // }

    public List<AttributeProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<AttributeProperty> pProperties) {
        properties = pProperties;
    }
}
