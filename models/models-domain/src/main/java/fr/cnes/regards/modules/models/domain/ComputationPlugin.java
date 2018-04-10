package fr.cnes.regards.modules.models.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Specific annotation to add to {@link fr.cnes.regards.framework.modules.plugins.annotations.Plugin}
 * for {@link IComputedAttribute} to specify which type the implementation can support.
 *
 * @author Sylvain VISSIERE-GUERINET
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComputationPlugin {

    /**
     * @return {@link AttributeType} supported by the plugin annotation
     */
    AttributeType supportedType();

}
