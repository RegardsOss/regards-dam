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
package fr.cnes.regards.modules.dam.domain.models.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.dam.domain.models.IComputedAttribute;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;

/**
 * Validate whether a {@link ModelAttrAssoc} define a link to a computed {@link AttributeModel} or not. If it does,
 * validate that the associated {@link Model} is of type {@link EntityType#DATASET}, that the PluginConfiguration is not
 * null, that it is a PluginConfiguration of a {@link IComputedAttribute} plugin and that the plugin return type is
 * coherant with the {@link AttributeModel#getType()}. Otherwise, doesn't do anything.
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Constraint(validatedBy = ComputedAttributeValidator.class)
@Documented
public @interface ComputedAttribute {

    /**
     * Class to validate
     */
    String CLASS_NAME = "fr.cnes.regards.modules.dam.domain.models.validation.ComputedAttribute";

    String TEMPLATE_START = "Validation annotation @" + CLASS_NAME
            + " validating ModelAttrAssoc (attribute %s): mode is COMPUTED but associated PluginConfiguration %s ";

    String DEFAULT_TEMPLATE = TEMPLATE_START + "is null or not implementing IComputedAttribute";

    String INCOMPATIBLE_TYPE = TEMPLATE_START + "has an incompatible attribute type";

    String WRONG_MODEL_TYPE = TEMPLATE_START + "only attributes associated with a model of type %s can be computed.";

    /**
     * @return error message key
     */
    String message() default DEFAULT_TEMPLATE;

    /**
     * @return validation groups
     */
    Class<?>[] groups() default {};

    /**
     * @return custom payload
     */
    Class<? extends Payload>[] payload() default {};

}
