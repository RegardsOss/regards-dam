/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.dam.domain.models.attributes.restriction;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.domain.models.schema.Restriction;

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
        type = RestrictionType.PATTERN;
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
