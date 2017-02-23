/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.microservices.catalog.plugin.client.ICatalogPluginClient;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = { "fr.cnes.regards.modules" }) // , "fr.cnes.regards.framework.amqp" })
public class ServiceConfiguration {

    // @Bean
    // public IPublisher publisher() {
    // return Mockito.mock(IPublisher.class);
    // }
    //
    // @Bean
    // public ISubscriber subscriber() {
    // return Mockito.mock(ISubscriber.class);
    // }

    @Bean
    public IPluginService pluginService() {
        return Mockito.mock(IPluginService.class);
    }

    @Bean
    public ICatalogPluginClient catalogPluginClient() {
        return Mockito.mock(ICatalogPluginClient.class);
    }

    @Bean
    public IResourceService resourceService() {
        return Mockito.mock(IResourceService.class);
    }
}
