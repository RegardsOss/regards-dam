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
package fr.cnes.regards.modules.entities.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.google.gson.Gson;

import fr.cnes.regards.modules.project.client.rest.IProjectsClient;
import fr.cnes.regards.modules.storage.client.IAipClient;
import fr.cnes.regards.modules.storage.domain.AIP;
import fr.cnes.regards.modules.storage.domain.AIPCollection;
import fr.cnes.regards.modules.storage.domain.RejectedAip;

/**
 * This configuration defined a proxy for the {@link IAipClient}.
 * The response to the call of the methods are {@link HttpStatus#PARTIAL_CONTENT}.
 * 
 * @author Christophe Mertz
 *
 */
@Configuration
class AipClientPartialResponseConfigurationMock {

    private final static Logger LOGGER = LoggerFactory.getLogger(AipClientPartialResponseConfigurationMock.class);

    @Autowired
    private Gson gson;

    @Bean
    public IProjectsClient projectsClient() {
        return Mockito.mock(IProjectsClient.class);
    }

    @Bean
    public IAipClient aipClient() {
        AipClientPartialProxy aipClientProxy = new AipClientPartialProxy();
        InvocationHandler handler = (proxy, method, args) -> {
            for (Method aipClientProxyMethod : aipClientProxy.getClass().getMethods()) {
                if (aipClientProxyMethod.getName().equals(method.getName())) {
                    return aipClientProxyMethod.invoke(aipClientProxy, args);
                }
            }
            return null;
        };
        return (IAipClient) Proxy.newProxyInstance(IAipClient.class.getClassLoader(),
                                                   new Class<?>[] { IAipClient.class }, handler);
    }

    private class AipClientPartialProxy {

        @SuppressWarnings("unused")
        public ResponseEntity<List<RejectedAip>> store(@RequestBody AIPCollection aips) {

            LOGGER.debug("==========>  CREATION ERROR ===> " + aips.getFeatures().get(0).getId() + " =============");

            List<RejectedAip> rejectedAip = new ArrayList<>();
            rejectedAip.add(new RejectedAip(aips.getFeatures().get(0).getId().toString(),
                    Arrays.asList("a reject cause", "an other reject cause")));
            return new ResponseEntity<>(rejectedAip, HttpStatus.PARTIAL_CONTENT);
        }

        @SuppressWarnings("unused")
        public ResponseEntity<AIP> updateAip(@PathVariable(name = "ip_id") String ipId,
                @RequestBody @Valid AIP updated) {

            String gsonString = gson.toJson(updated);
            LOGGER.debug("==========>  UPDATE   ===> " + ipId + " =============" + gsonString);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        }

    }
}