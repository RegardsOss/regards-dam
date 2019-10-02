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
package fr.cnes.regards.modules.model.service.validation.validator;

import java.util.Objects;

import org.springframework.validation.Errors;

import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.dto.properties.AbstractProperty;

/**
 * Validate not alterable attribute
 * @author Marc Sordi
 */
public class NotAlterableAttributeValidator extends AbstractPropertyValidator {

    /**
     * Attribute model
     */
    private final AttributeModel attribute;

    /**
     * old attribute
     */
    private final AbstractProperty<?> oldValue;

    /**
     * new attribute
     */
    private final AbstractProperty<?> newValue;

    /**
     * Constructor
     * @param attributeKey
     * @param attribute  {@link AttributeModel}
     * @param oldValue
     * @param newValue
     */
    public NotAlterableAttributeValidator(String attributeKey, AttributeModel attribute, AbstractProperty<?> oldValue,
            AbstractProperty<?> newValue) {
        super(attributeKey);
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.attribute = attribute;
    }

    @Override
    public void validate(Object target, Errors errors) {
        //if the attribute should be present then lets reject if any of them doesn't exist
        if (!attribute.isOptional() && newValue == null) {
            errors.reject("error.attribute.not.alterable.not.present.message",
                          String.format("Attribute \"%s\" is required", attributeKey));
        } else {
            if (newValue != null) {
                //if the attribute is optional and there was no value before, lets accept the new one
                if (attribute.isOptional() && oldValue == null) {
                    return;
                }
                if (!Objects.equals(oldValue.getValue(), newValue.getValue())) {
                    errors.reject("error.attribute.not.alterable.message",
                                  String.format("Attribute \"%s\" is not alterable.", attributeKey));
                }
            }
        }
    }

}
