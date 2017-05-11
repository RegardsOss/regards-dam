/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.datasources.domain.DBConnection;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;

/**
 * @author Marc Sordi
 *
 */
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = { "fr.cnes.regards.modules" })
@EnableAutoConfiguration
@TestPropertySource("/dbConnectionServiceIT.properties")
public class DBConnectionServiceIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnectionServiceIT.class);

    @Autowired
    private IDBConnectionService dbConnectionService;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Test
    public void createWrongDbConnection() throws ModuleException {

        // Working tenant
        runtimeTenantResolver.forceTenant("test");

        // Initialize wrong connection
        DBConnection connection = new DBConnection();
        connection.setDbHost("unknown");
        connection.setDbName("regards");
        connection.setDbPort("5432");
        connection.setLabel("Unknown host connection");
        connection.setPassword("password");
        connection.setPluginClassName(DefaultPostgreConnectionPlugin.class.getName());
        connection.setUser("user");

        dbConnectionService.createDBConnection(connection);

        LOGGER.debug("connection created");
    }

    @Configuration
    public static class DBConnectionServiceITConfiguration {

        // No bean at the moment
    }
}
