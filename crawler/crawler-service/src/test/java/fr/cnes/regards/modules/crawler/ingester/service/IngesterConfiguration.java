package fr.cnes.regards.modules.crawler.ingester.service;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.security.autoconfigure.MethodAuthorizationServiceAutoConfiguration;
import fr.cnes.regards.framework.security.autoconfigure.MethodSecurityAutoConfiguration;
import fr.cnes.regards.framework.security.autoconfigure.SecurityVoterAutoConfiguration;
import fr.cnes.regards.framework.security.autoconfigure.WebSecurityAutoConfiguration;
import fr.cnes.regards.modules.crawler.service.CrawlerConfiguration;

@Configuration
@ComponentScan(basePackages = { "fr.cnes.regards.modules.crawler", "fr.cnes.regards.modules.indexer",
        "fr.cnes.regards.modules.entities", "fr.cnes.regards.modules.models", "fr.cnes.regards.modules.datasources",
        "fr.cnes.regards.modules.search", "fr.cnes.regards.framework.modules.plugins.service" })
@EnableAutoConfiguration(exclude = { CrawlerConfiguration.class, MethodAuthorizationServiceAutoConfiguration.class,
        MethodSecurityAutoConfiguration.class, SecurityVoterAutoConfiguration.class,
        WebSecurityAutoConfiguration.class })
@PropertySource(value = { "classpath:test2.properties", "classpath:test2_${user.name}.properties" },
        ignoreResourceNotFound = true)
@EnableAsync
//@EnableScheduling <-- Do not set that, this will activate IngesterService during all tests
public class IngesterConfiguration {

    @Bean
    public IResourceService getResourceService() {
        return Mockito.mock(IResourceService.class);
    }
}