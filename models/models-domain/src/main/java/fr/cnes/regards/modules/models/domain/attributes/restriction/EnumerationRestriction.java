/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.schema.Enumeration;
import fr.cnes.regards.modules.models.schema.Restriction;

/**
 *
 * Manage pattern restriction for attribute of type :
 * <ul>
 * <li>{@link AttributeType#STRING}</li>
 * <li>{@link AttributeType#STRING_ARRAY}</li>
 * </ul>
 *
 * @author msordi
 *
 */
@Entity(name = "EnumerationRestriction")
@DiscriminatorValue("Enumeration")
public class EnumerationRestriction extends AbstractRestriction {

    /**
     * Acceptable values, relevant for {@link AttributeType#ENUMERATION} attributes
     */
    @ElementCollection
    @NotNull
    private Set<String> acceptableValues;

    public EnumerationRestriction() {
        super();
        setType(RestrictionType.ENUMERATION);
        acceptableValues = new HashSet<>();
    }

    public Set<String> getAcceptableValues() {
        return acceptableValues;
    }

    public void setAcceptableValues(Set<String> pAcceptableValues) {
        acceptableValues = pAcceptableValues;
    }

    public void addAcceptableValues(String pValue) {
        if (pValue != null) {
            acceptableValues.add(pValue);
        }
    }

    @Override
    public Boolean supports(AttributeType pAttributeType) {
        return AttributeType.STRING.equals(pAttributeType) || AttributeType.STRING_ARRAY.equals(pAttributeType);
    }

    @Override
    public Restriction toXml() {

        final Restriction restriction = new Restriction();
        final Enumeration enumeration = new Enumeration();
        if (acceptableValues != null) {
            for (String val : acceptableValues) {
                enumeration.getValue().add(val);
            }
        }
        restriction.setEnumeration(enumeration);
        return restriction;
    }

    @Override
    public void fromXml(Restriction pXmlElement) {
        for (String val : pXmlElement.getEnumeration().getValue()) {
            addAcceptableValues(val);
        }
    }
}
