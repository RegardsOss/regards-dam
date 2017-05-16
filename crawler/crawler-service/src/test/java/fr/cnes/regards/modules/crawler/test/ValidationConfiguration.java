package fr.cnes.regards.modules.crawler.test;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

import fr.cnes.regards.framework.amqp.*;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.security.autoconfigure.MethodAuthorizationServiceAutoConfiguration;
import fr.cnes.regards.framework.security.autoconfigure.MethodSecurityAutoConfiguration;
import fr.cnes.regards.framework.security.autoconfigure.SecurityVoterAutoConfiguration;
import fr.cnes.regards.framework.security.autoconfigure.WebSecurityAutoConfiguration;

/**
 * @author oroussel
 */
@Configuration
@ComponentScan(basePackages = { "fr.cnes.regards.modules.crawler.service", "fr.cnes.regards.modules.indexer",
        "fr.cnes.regards.modules.entities", "fr.cnes.regards.modules.models", "fr.cnes.regards.modules.datasources",
        "fr.cnes.regards.modules.search", "fr.cnes.regards.framework.modules.plugins.service" })
@EnableAutoConfiguration(
        exclude = { MethodAuthorizationServiceAutoConfiguration.class, MethodSecurityAutoConfiguration.class,
                SecurityVoterAutoConfiguration.class, WebSecurityAutoConfiguration.class })
@PropertySource(value = { "classpath:validation.properties", "classpath:validation_${user.name}.properties" },
        ignoreResourceNotFound = true)
@EnableAsync
//@EnableScheduling <-- Do not set that, this will activate IngesterService during all tests
public class ValidationConfiguration {
    @Bean
    public IResourceService getResourceService() {
        return Mockito.mock(IResourceService.class);
    }

    @Bean
    public IPoller getPoller() {
        return Mockito.mock(IPoller.class);
    }

    @Bean
    public IPublisher getPublisher() {
        return Mockito.mock(IPublisher.class);
    }

    @Bean
    public ISubscriber getSubscriber() {
        return Mockito.mock(ISubscriber.class);
    }

    @Bean
    public IInstanceSubscriber getInstanceSubscriber() {
        return Mockito.mock(IInstanceSubscriber.class);
    }

    @Bean
    public IInstancePublisher getInstancePublisher() {
        return Mockito.mock(IInstancePublisher.class);
    }

}
