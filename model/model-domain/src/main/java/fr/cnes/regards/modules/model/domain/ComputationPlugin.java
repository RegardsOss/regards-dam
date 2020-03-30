package fr.cnes.regards.modules.model.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * Specific annotation to add to plugin
 * for {@link IComputedAttribute} to specify which type the implementation can support.
 *
 * @author Sylvain VISSIERE-GUERINET
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComputationPlugin {

    /**
     * @return {@link PropertyType} supported by the plugin annotation
     */
    PropertyType supportedType();

}
