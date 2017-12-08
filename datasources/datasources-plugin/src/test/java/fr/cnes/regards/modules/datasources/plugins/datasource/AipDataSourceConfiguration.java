/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.datasources.plugins.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;

import fr.cnes.regards.framework.amqp.IInstanceSubscriber;
import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.modules.storage.client.IAipClient;
import fr.cnes.regards.modules.storage.domain.AIP;
import fr.cnes.regards.modules.storage.domain.AIPState;

/**
 * @author oroussel
 */
@Configuration
@EnableAutoConfiguration
public class AipDataSourceConfiguration {

    @Bean
    public IInstanceSubscriber instanceSubscriber() {
        return Mockito.mock(IInstanceSubscriber.class);
    }

    @Bean
    public IPublisher publisher() {
        return Mockito.mock(IPublisher.class);
    }

    @Bean
    public ISubscriber subscriber() {
        return Mockito.mock(ISubscriber.class);
    }

    @Bean
    public IAipClient aipClient() {
//        return Mockito.mock(IAipClient.class);

        AipClientProxy aipClientProxy = new AipClientProxy();
        InvocationHandler handler = (proxy, method, args) -> {
            for (Method aipClientProxyMethod : aipClientProxy.getClass().getMethods()) {
                if (aipClientProxyMethod.getName().equals(method.getName())) {
                    return aipClientProxyMethod.invoke(aipClientProxy, args);
                }
            }
            return null;
        };
        return (IAipClient) Proxy
                .newProxyInstance(IAipClient.class.getClassLoader(), new Class<?>[] { IAipClient.class }, handler);
    }

    private class AipClientProxy {

        public ResponseEntity<PagedResources<Resource<AIP>>> retrieveAIPs(AIPState state, OffsetDateTime fromDate,
                OffsetDateTime toDate, int page, int size) {
            return null;
        }
    }

}
