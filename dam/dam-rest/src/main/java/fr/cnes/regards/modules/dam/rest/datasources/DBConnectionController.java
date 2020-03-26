/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.datasources;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.rest.representation.GenericResponseBody;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.dam.domain.datasources.Column;
import fr.cnes.regards.modules.dam.domain.datasources.Table;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDBConnectionPlugin;
import fr.cnes.regards.modules.dam.service.datasources.IDBConnectionService;

/**
 * @author Christophe Mertz
 */
@RestController
@RequestMapping(DBConnectionController.TYPE_MAPPING)
public class DBConnectionController implements IResourceController<PluginConfiguration> {

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/connections";

    /**
     * DBConnectionService attribute service
     */
    @Autowired
    private IDBConnectionService dbConnectionService;

    /**
     * Resource service
     */
    @Autowired
    private IResourceService resourceService;

    /**
     * Retrieve all {@link PluginConfiguration} used by the plugin type {@link IDBConnectionPlugin}
     * @return a list of {@link PluginConfiguration}
     */
    @ResourceAccess(description = "List all plugin's configurations defined for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<EntityModel<PluginConfiguration>>> getAllDBConnections() {
        return ResponseEntity.ok(toResources(dbConnectionService.getAllDBConnections()));
    }

    /**
     * Create a {@link PluginConfiguration} for the plugin type {@link IDBConnectionPlugin}
     * @param dbConnection the database connection used to create the {@link PluginConfiguration}
     * @return the created {@link PluginConfiguration}
     * @throws ModuleException if problem occurs during plugin configuration creation
     */
    @ResourceAccess(description = "Create a plugin configuration for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<EntityModel<PluginConfiguration>> createDBConnection(
            @Valid @RequestBody PluginConfiguration dbConnection) throws ModuleException {
        return ResponseEntity.ok(toResource(dbConnectionService.createDBConnection(dbConnection)));
    }

    /**
     * Get a {@link PluginConfiguration}
     * @param plgBusinessId a {@link PluginConfiguration} identifier
     * @return a {@link PluginConfiguration}
     * @throws ModuleException if plugin configuration cannot be retrieved
     */
    @ResourceAccess(description = "Get a plugin configuration for a plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.GET, value = "/{plgBusinessId}")
    public ResponseEntity<EntityModel<PluginConfiguration>> getDBConnection(
            @PathVariable(name = "plgBusinessId") String plgBusinessId) throws ModuleException {
        return ResponseEntity.ok(toResource(dbConnectionService.getDBConnection(plgBusinessId)));
    }

    /**
     * Allows to update {@link PluginConfiguration} for the plugin type {@link IDBConnectionPlugin}
     * @param plgBusinessId a {@link PluginConfiguration} identifier
     * @param dbConnection the {@link PluginConfiguration} to update
     * @return updated {@link PluginConfiguration}
     * @throws ModuleException if plugin configuration cannot be updated
     */
    @ResourceAccess(description = "Update a plugin configuration defined for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.PUT, value = "/{plgBusinessId}")
    public ResponseEntity<EntityModel<PluginConfiguration>> updateDBConnection(
            @PathVariable(name = "plgBusinessId") String plgBusinessId,
            @Valid @RequestBody PluginConfiguration dbConnection) throws ModuleException {
        if (!plgBusinessId.equals(dbConnection.getBusinessId())) {
            throw new EntityInconsistentIdentifierException(plgBusinessId, dbConnection.getBusinessId(),
                    PluginConfiguration.class);
        }
        return ResponseEntity.ok(toResource(dbConnectionService.updateDBConnection(dbConnection)));
    }

    /**
     * Delete a {@link PluginConfiguration} defined for the plugin type {@link IDBConnectionPlugin}
     * @param plgBusinessId {@link PluginConfiguration} identifier
     * @return nothing
     * @throws ModuleException if plugin configuration cannot be deleted
     */
    @ResourceAccess(description = "Delete a plugin configuration defined for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{plgBusinessId}")
    public ResponseEntity<Void> deleteDBConnection(@PathVariable(name = "plgBusinessId") String plgBusinessId)
            throws ModuleException {
        dbConnectionService.deleteDBConnection(plgBusinessId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Test a {@link PluginConfiguration} defined for the plugin type {@link IDBConnectionPlugin}
     * @param plgBusinessId {@link PluginConfiguration} identifier
     * @return nothing
     * @throws ModuleException if problem occurs during test of the connection
     */
    @ResourceAccess(description = "Test the connection to the database")
    @RequestMapping(method = RequestMethod.POST, value = "/{plgBusinessId}")
    public ResponseEntity<GenericResponseBody> testDBConnection(
            @PathVariable(name = "plgBusinessId") String plgBusinessId) throws ModuleException {

        if (dbConnectionService.testDBConnection(plgBusinessId)) {
            return ResponseEntity.ok(new GenericResponseBody("Valid connection"));
        } else {
            return ResponseEntity.badRequest().body(new GenericResponseBody("Invalid connection"));
        }
    }

    /**
     * Get the database's tables
     * @param plgBusinessId {@link PluginConfiguration} identifier
     * @return a {@link Map} that contains the database's tables
     * @throws ModuleException if problem occurs during retrieve the database's tables
     * @throws NotAvailablePluginConfigurationException
     */
    @ResourceAccess(description = "Get the tables of the database")
    @RequestMapping(method = RequestMethod.GET, value = "/{plgBusinessId}/tables")
    public ResponseEntity<Map<String, Table>> getTables(@PathVariable(name = "plgBusinessId") String plgBusinessId)
            throws ModuleException, NotAvailablePluginConfigurationException {
        Map<String, Table> tables;
        tables = dbConnectionService.getTables(plgBusinessId);
        return ResponseEntity.ok(tables);
    }

    /**
     * Get the column of a table
     * @param plgBusinessId {@link PluginConfiguration} identifier
     * @param tableName a database table name
     * @return a {@link Map} that contains the columns of a table
     * @throws ModuleException if problem occurs during retrieve the columns of a table
     * @throws NotAvailablePluginConfigurationException
     */
    @ResourceAccess(description = "Get the columns of a specific table of the database")
    @RequestMapping(method = RequestMethod.GET, value = "/{plgBusinessId}/tables/{tableName}/columns")
    public ResponseEntity<Map<String, Column>> getColumns(@PathVariable(name = "plgBusinessId") String plgBusinessId,
            @PathVariable(name = "tableName") String tableName)
            throws ModuleException, NotAvailablePluginConfigurationException {
        return ResponseEntity.ok(dbConnectionService.getColumns(plgBusinessId, tableName));
    }

    @Override
    public EntityModel<PluginConfiguration> toResource(PluginConfiguration element, Object... extras) {
        final EntityModel<PluginConfiguration> resource = resourceService.toResource(element);
        resourceService.addLink(resource, this.getClass(), "getDBConnection", LinkRels.SELF,
                                MethodParamFactory.build(String.class, element.getBusinessId()));
        resourceService.addLink(resource, this.getClass(), "deleteDBConnection", LinkRels.DELETE,
                                MethodParamFactory.build(String.class, element.getBusinessId()));
        resourceService.addLink(resource, this.getClass(), "updateDBConnection", LinkRels.UPDATE,
                                MethodParamFactory.build(String.class, element.getBusinessId()),
                                MethodParamFactory.build(PluginConfiguration.class));
        resourceService.addLink(resource, this.getClass(), "getAllDBConnections", LinkRels.LIST);
        return resource;
    }

}
