/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.client.dataaccess;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.cnes.regards.modules.accessrights.client.IProjectUsersClient;
import fr.cnes.regards.modules.dam.client.models.IAttributeModelClient;
import fr.cnes.regards.modules.notification.client.INotificationClient;
import fr.cnes.regards.modules.project.client.rest.IProjectsClient;

@Configuration
public class MockConfiguration {

    @Bean
    public IProjectsClient pClient() {
        return Mockito.mock(IProjectsClient.class);
    }

    @Bean
    public IProjectUsersClient client() {
        return Mockito.mock(IProjectUsersClient.class);
    }

    @Bean
    public IAttributeModelClient attrClient() {
        return Mockito.mock(IAttributeModelClient.class);
    }

    @Bean
    public INotificationClient getNotificationClient() {
        return Mockito.mock(INotificationClient.class);
    }

}
