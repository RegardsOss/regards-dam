package fr.cnes.regards.modules.crawler.service;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.gson.autoconfigure.GsonAutoConfiguration;
import fr.cnes.regards.modules.entities.service.adapters.gson.FlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

@Configuration
@ComponentScan(basePackages = { "fr.cnes.regards.modules.crawler" }, basePackageClasses = {
        FlattenedAttributeAdapterFactory.class, GsonAutoConfiguration.class })
public class CrawlerConfiguration {

    @Bean
    public IAttributeModelService modelService() {
        return Mockito.mock(IAttributeModelService.class);
    }

    @Bean
    public ISubscriber subscriber() {
        return Mockito.mock(ISubscriber.class);
    }
}
