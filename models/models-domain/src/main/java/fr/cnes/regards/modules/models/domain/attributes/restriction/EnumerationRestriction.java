/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 *
 * Manage enumeration restriction for attribute of type :
 * <ul>
 * <li>{@link AttributeType#ENUMERATION}</li>
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
        return AttributeType.ENUMERATION.equals(pAttributeType);
    }
}
