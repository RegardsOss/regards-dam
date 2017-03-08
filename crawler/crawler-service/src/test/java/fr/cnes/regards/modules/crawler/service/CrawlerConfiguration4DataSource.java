package fr.cnes.regards.modules.crawler.service;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

import fr.cnes.regards.framework.hateoas.IResourceService;

@Configuration
@ComponentScan(
        basePackages = { "fr.cnes.regards.modules.crawler", "fr.cnes.regards.modules.entities",
                "fr.cnes.regards.modules.models", "fr.cnes.regards.modules.datasources",
                "fr.cnes.regards.framework.modules.plugins.service" },
        excludeFilters = { @Filter(classes = { CrawlerConfiguration.class }, type = FilterType.ASSIGNABLE_TYPE) })
@EnableAutoConfiguration
@PropertySource(value = { "classpath:test.properties", "classpath:test_ds.properties",
        "classpath:test_${user.name}.properties" }, ignoreResourceNotFound = true)
@EnableAsync
public class CrawlerConfiguration4DataSource {

    @Bean
    public IResourceService getResourceService() {
        return Mockito.mock(IResourceService.class);
    }
}
