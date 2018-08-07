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
package fr.cnes.regards.modules.dam.service.entities.validator;

import java.util.Objects;

import org.springframework.validation.Errors;

import fr.cnes.regards.modules.dam.domain.entities.attribute.AbstractAttribute;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;

/**
 * Validate not alterable attribute
 * @author Marc Sordi
 */
public class NotAlterableAttributeValidator extends AbstractAttributeValidator {

    /**
     * Attribute model
     */
    private final AttributeModel attribute;

    /**
     * old attribute
     */
    private final AbstractAttribute<?> oldValue;

    /**
     * new attribute
     */
    private final AbstractAttribute<?> newValue;

    /**
     * Constructor
     */
    public NotAlterableAttributeValidator(String attributeKey, AttributeModel attribute, AbstractAttribute<?> oldValue,
            AbstractAttribute<?> newValue) {
        super(attributeKey);
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.attribute = attribute;
    }

    @Override
    public void validate(Object target, Errors errors) {
        //if the attribute should be present then lets reject if any of them doesn't exist
        if (!attribute.isOptional() && (newValue == null)) {
            errors.reject("error.attribute.not.alterable.not.present.message",
                          String.format("Attribute \"%s\" is required", attributeKey));
        } else {
            if (newValue != null) {
                //if the attribute is optional and there was no value before, lets accept the new one
                if (attribute.isOptional() && (oldValue == null)) {
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
