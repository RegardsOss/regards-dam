/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.domain.models.schema.Enumeration;
import fr.cnes.regards.modules.dam.domain.models.schema.Restriction;

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
@Entity
@DiscriminatorValue("ENUMERATION")
public class EnumerationRestriction extends AbstractRestriction {

    /**
     * Acceptable values, relevant for AttributeType#ENUMERATION
     */
    @NotNull
    @ElementCollection
    @JoinTable(name = "ta_enum_restr_accept_values", joinColumns = @JoinColumn(name = "restriction_id",
            foreignKey = @ForeignKey(name = "fk_enum_restr_accept_values_restriction_id")))
    @Column(name = "value")
    private Set<String> acceptableValues;

    public EnumerationRestriction() {
        super();
        type = RestrictionType.ENUMERATION;
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
