package fr.cnes.regards.modules.entities.rest;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.cnes.regards.modules.models.client.IAttributeModelClient;
import fr.cnes.regards.modules.opensearch.service.geoparser.GeoParser;

/**
 * Created by oroussel on 04/05/17.
 */
@Configuration
public class ControllerITConfig {
    @Bean
    public IAttributeModelClient attributeModelClient() {
        return Mockito.mock(IAttributeModelClient.class);
    }

    @Bean
    public GeoParser geoParser() {
        return Mockito.mock(GeoParser.class);
    }
}
