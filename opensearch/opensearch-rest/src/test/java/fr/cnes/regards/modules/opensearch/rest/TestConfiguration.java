/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.opensearch.rest;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.cnes.regards.modules.dam.client.models.IAttributeModelClient;
import fr.cnes.regards.modules.dam.gson.entities.IAttributeHelper;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;

/**
 * @author sbinda
 *
 */
@Configuration
public class TestConfiguration {

    @Bean
    IAttributeFinder finder() {
        return Mockito.mock(IAttributeFinder.class);
    }

    @Bean
    IAttributeModelClient attMClient() {
        return Mockito.mock(IAttributeModelClient.class);
    }

    @Bean
    IAttributeHelper helper() {
        return Mockito.mock(IAttributeHelper.class);
    }

}
