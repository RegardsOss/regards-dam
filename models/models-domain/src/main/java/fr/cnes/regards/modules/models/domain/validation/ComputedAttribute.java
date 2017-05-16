/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import fr.cnes.regards.modules.models.domain.EntityType;
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
