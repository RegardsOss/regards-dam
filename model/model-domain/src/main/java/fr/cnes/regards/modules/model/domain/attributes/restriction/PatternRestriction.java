/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.model.domain.attributes.restriction;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.model.domain.schema.Restriction;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * Manage pattern restriction for attribute of type :
 * <ul>
 * <li>{@link PropertyType#STRING}</li>
 * <li>{@link PropertyType#STRING_ARRAY}</li>
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
    public Boolean supports(PropertyType pPropertyType) {
        return PropertyType.STRING.equals(pPropertyType) || PropertyType.STRING_ARRAY.equals(pPropertyType);
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
