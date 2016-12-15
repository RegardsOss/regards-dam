/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

import fr.cnes.regards.modules.accessrights.client.IProjectUsersClient;

/**
 * Configuration class to centralize all feign client needed to be able to mock them for integration tests
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Configuration
@EnableFeignClients(clients = { IProjectUsersClient.class })
public class FeignConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeignConfiguration.class);

    public FeignConfiguration() {
        LOGGER.info("Client Feign activated in data access");
    }
}
