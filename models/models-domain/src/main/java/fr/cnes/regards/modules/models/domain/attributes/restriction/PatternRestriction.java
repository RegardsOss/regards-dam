/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.schema.Restriction;

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
@Entity
@DiscriminatorValue("PATTERN")
public class PatternRestriction extends AbstractRestriction {

    /**
     * Validation pattern
     */
    @Column
    @NotNull
    private String pattern;

    /**
     * Constructor
     */
    public PatternRestriction() { // NOSONAR
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
    public Restriction toXml() {
        final Restriction restriction = new Restriction();
        restriction.setPattern(pattern);
        return restriction;
    }

    @Override
    public void fromXml(Restriction pXmlElement) {
        setPattern(pXmlElement.getPattern());
    }

}
