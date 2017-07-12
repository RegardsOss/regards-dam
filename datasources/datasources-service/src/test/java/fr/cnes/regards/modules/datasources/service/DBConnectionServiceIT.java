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
package fr.cnes.regards.modules.datasources.service;

import org.junit.Ignore;
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
 *
 * FIXME
 * - test connection
 * - test that plugin conf is cleaned from plugin cache if test does not succeed
 *
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
    @Ignore
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
