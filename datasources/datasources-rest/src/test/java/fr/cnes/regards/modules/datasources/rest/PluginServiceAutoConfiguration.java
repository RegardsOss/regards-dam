/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import fr.cnes.regards.framework.jpa.multitenant.autoconfigure.MultitenantJpaAutoConfiguration;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.modules.plugins.service.PluginService;

/**
 *
 * Class PluginServiceAutoConfiguration
 *
 * A bean used to defined a implementation of {@link PluginService}.
 *
 * @author Christophe Mertz
 */
@Configuration
@ComponentScan(basePackages = { "fr.cnes.regards.framework.modules.plugins" })
@PropertySource("classpath:datasource-test.properties")
@AutoConfigureAfter({ MultitenantJpaAutoConfiguration.class })
public class PluginServiceAutoConfiguration {

    /**
     * {@link List} of the package to scan to find plugins
     */
    private List<String> packagesToScan;

    @Bean
    public IPluginService pluginService(IPluginConfigurationRepository pluginConfRepo) {
        packagesToScan = new ArrayList<>();
        packagesToScan.add("fr.cnes.regards.modules.datasources.plugins");
        return new PluginService(pluginConfRepo, packagesToScan);
    }

}
