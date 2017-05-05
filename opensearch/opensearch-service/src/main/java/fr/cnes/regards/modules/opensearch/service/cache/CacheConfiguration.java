/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enable caching support in Spring.<br>
 * Put here specific cache configuration, such as your choice of {@link CacheManager} implementation if your are not happy with Spring default strategy.
 *
 * @author Xavier-Alexandre Brochard
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

}
