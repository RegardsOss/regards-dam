/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.test;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;

import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.security.autoconfigure.MethodAuthorizationServiceAutoConfiguration;
import fr.cnes.regards.framework.security.autoconfigure.MethodSecurityAutoConfiguration;
import fr.cnes.regards.framework.security.autoconfigure.SecurityVoterAutoConfiguration;
import fr.cnes.regards.framework.security.autoconfigure.WebSecurityAutoConfiguration;
import fr.cnes.regards.modules.models.client.IAttributeModelClient;
import fr.cnes.regards.modules.models.client.IModelAttrAssocClient;
import fr.cnes.regards.modules.opensearch.service.IOpenSearchService;
import fr.cnes.regards.modules.project.client.rest.IProjectsClient;

@Configuration
@ComponentScan(basePackages = { "fr.cnes.regards.modules.crawler.service", "fr.cnes.regards.modules.indexer",
        "fr.cnes.regards.modules.entities", "fr.cnes.regards.modules.models", "fr.cnes.regards.modules.datasources",
        "fr.cnes.regards.modules.search", "fr.cnes.regards.framework.modules.plugins.service" })
@EnableAutoConfiguration(
        exclude = { MethodAuthorizationServiceAutoConfiguration.class, MethodSecurityAutoConfiguration.class,
                SecurityVoterAutoConfiguration.class, WebSecurityAutoConfiguration.class })
@PropertySource(value = { "classpath:test.properties", "classpath:test_${user.name}.properties" },
        ignoreResourceNotFound = true)
public class CrawlerConfiguration {

    @Bean
    public IAttributeModelClient attributeModelClient() {
        return Mockito.mock(IAttributeModelClient.class);
    }

    @Bean
    @Primary
    public IOpenSearchService openSearchService() {
        return Mockito.mock(IOpenSearchService.class);
    }

    @Bean
    public IProjectsClient projectsClient() {
        return Mockito.mock(IProjectsClient.class);
    }

    @Bean
    public IModelAttrAssocClient modelAttrAssocClient() {
        return Mockito.mock(IModelAttrAssocClient.class);
    }

    @Bean
    public IResourceService getResourceService() {
        return Mockito.mock(IResourceService.class);
    }
}
