/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Manage pattern restriction for attribute of type :
 * <ul>
 * <li>{@link AttributeType#STRING}</li>
 * <li>{@link AttributeType#STRING_ARRAY}</li>
 * </ul>
 *
 * @author msordi
 *
 */
@Entity(name = "PatternRestriction")
@DiscriminatorValue("Pattern")
public class PatternRestriction extends AbstractRestriction {

    /**
     * Validation pattern
     */
    private String pattern;

    /**
     * Constructor
     */
    public PatternRestriction() {
        super();
        setType(RestrictionType.PATTERN);
    }

    /**
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @param pPattern
     *            the pattern to set
     */
    public void setPattern(String pPattern) {
        pattern = pPattern;
    }

    @Override
    public Boolean supports(AttributeType pAttributeType) {
        return AttributeType.STRING.equals(pAttributeType) || AttributeType.STRING_ARRAY.equals(pAttributeType);
    }

    @Override
    public Boolean isPublic() {
        return Boolean.TRUE;
    }

}
