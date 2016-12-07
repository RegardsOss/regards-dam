/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.validator;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Marc Sordi
 *
 */
@ComponentScan(basePackages = "fr.cnes.regards.modules")
@EnableAutoConfiguration
@PropertySource("classpath:application.properties")
public class CustomValidatorConfiguration {

}
