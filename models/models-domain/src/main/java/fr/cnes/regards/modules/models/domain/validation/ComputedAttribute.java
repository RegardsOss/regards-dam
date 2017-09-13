/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.models.domain.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

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
    String CLASS_NAME = "fr.cnes.regards.modules.models.domain.validation.ComputedAttribute";

    /**
     * @return error message key
     */
    String message() default "{Validation annotation @" + CLASS_NAME
            + " validating ModelAttrAssoc (attribute %s): mode is COMPUTED but associated PluginConfiguration %s is null "
            + "or not implementing IComputedAttribute or has an incompatible attribute type}";

    /**
     * @return validation groups
     */
    Class<?>[] groups() default {};

    /**
     * @return custom payload
     */
    Class<? extends Payload>[] payload() default {};

}
