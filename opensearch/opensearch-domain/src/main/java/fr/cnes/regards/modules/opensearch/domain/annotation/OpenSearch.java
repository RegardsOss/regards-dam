/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.domain.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allowing us to declare a method as being an OpenSearch Endpoint
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Inherited
public @interface OpenSearch {

}
