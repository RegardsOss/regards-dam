/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.dao;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = { "fr.cnes.regards.modules" })
@EnableJpaRepositories(basePackages = { "fr.cnes.regards.modules" })
@ComponentScan(basePackages = { "fr.cnes.regards.modules" })
@PropertySource("classpath:application-test.properties")
public class CollectionTestConfiguration {

}
