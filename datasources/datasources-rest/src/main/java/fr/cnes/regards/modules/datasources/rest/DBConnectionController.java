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
package fr.cnes.regards.modules.datasources.rest;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
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
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.rest.representation.GenericResponseBody;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.DBConnection;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.service.IDBConnectionService;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * REST interface for managing data {@link Model}
 * @author Christophe Mertz
 */
@RestController
@ModuleInfo(name = "dbconnection", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS SI",
        documentation = "http://test")
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
    public ResponseEntity<List<Resource<PluginConfiguration>>> getAllDBConnections() {
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
    public ResponseEntity<Resource<PluginConfiguration>> createDBConnection(
            @Valid @RequestBody PluginConfiguration dbConnection) throws ModuleException {
        return ResponseEntity.ok(toResource(dbConnectionService.createDBConnection(dbConnection)));
    }

    /**
     * Get a {@link PluginConfiguration}
     * @param connectionId a {@link PluginConfiguration} identifier
     * @return a {@link PluginConfiguration}
     * @throws ModuleException if plugin configuration cannot be retrieved
     */
    @ResourceAccess(description = "Get a plugin configuration for a plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.GET, value = "/{connectionId}")
    public ResponseEntity<Resource<PluginConfiguration>> getDBConnection(@PathVariable Long connectionId)
            throws ModuleException {
        return ResponseEntity.ok(toResource(dbConnectionService.getDBConnection(connectionId)));
    }

    /**
     * Allows to update {@link PluginConfiguration} for the plugin type {@link IDBConnectionPlugin}
     * @param connectionId a {@link PluginConfiguration} identifier
     * @param pDbConnection the {@link DBConnection} to update
     * @return updated {@link PluginConfiguration}
     * @throws ModuleException if plugin configuration cannot be updated
     */
    @ResourceAccess(description = "Update a plugin configuration defined for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.PUT, value = "/{connectionId}")
    public ResponseEntity<Resource<PluginConfiguration>> updateDBConnection(@PathVariable Long connectionId,
            @Valid @RequestBody PluginConfiguration dbConnection) throws ModuleException {
        if (!connectionId.equals(dbConnection.getId())) {
            throw new EntityInconsistentIdentifierException(connectionId, dbConnection.getId(),
                                                            PluginConfiguration.class);
        }
        return ResponseEntity.ok(toResource(dbConnectionService.updateDBConnection(dbConnection)));
    }

    /**
     * Delete a {@link PluginConfiguration} defined for the plugin type {@link IDBConnectionPlugin}
     * @param connectionId {@link PluginConfiguration} identifier
     * @return nothing
     * @throws ModuleException if plugin configuration cannot be deleted
     */
    @ResourceAccess(description = "Delete a plugin configuration defined for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{connectionId}")
    public ResponseEntity<Void> deleteDBConnection(@PathVariable Long connectionId) throws ModuleException {
        dbConnectionService.deleteDBConnection(connectionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Test a {@link PluginConfiguration} defined for the plugin type {@link IDBConnectionPlugin}
     * @param connectionId {@link PluginConfiguration} identifier
     * @return nothing
     * @throws ModuleException if problem occurs during test of the connection
     */
    @ResourceAccess(description = "Test the connection to the database")
    @RequestMapping(method = RequestMethod.POST, value = "/{connectionId}")
    public ResponseEntity<GenericResponseBody> testDBConnection(@PathVariable Long connectionId)
            throws ModuleException {

        if (dbConnectionService.testDBConnection(connectionId)) {
            return ResponseEntity.ok(new GenericResponseBody("Valid connection"));
        } else {
            return ResponseEntity.badRequest().body(new GenericResponseBody("Invalid connection"));
        }
    }

    /**
     * Get the database's tables
     * @param connectionId {@link PluginConfiguration} identifier
     * @return a {@link Map} that contains the database's tables
     * @throws ModuleException if problem occurs during retrieve the database's tables
     */
    @ResourceAccess(description = "Get the tables of the database")
    @RequestMapping(method = RequestMethod.GET, value = "/{connectionId}/tables")
    public ResponseEntity<Map<String, Table>> getTables(@PathVariable Long connectionId) throws ModuleException {
        Map<String, Table> tables = dbConnectionService.getTables(connectionId);
        return ResponseEntity.ok(tables);
    }

    /**
     * Get the column of a table
     * @param connectionId {@link PluginConfiguration} identifier
     * @param tableName a database table name
     * @return a {@link Map} that contains the columns of a table
     * @throws ModuleException if problem occurs during retrieve the columns of a table
     */
    @ResourceAccess(description = "Get the columns of a specific table of the database")
    @RequestMapping(method = RequestMethod.GET, value = "/{connectionId}/tables/{tableName}/columns")
    public ResponseEntity<Map<String, Column>> getColumns(@PathVariable Long connectionId,
            @PathVariable String tableName) throws ModuleException {
        return ResponseEntity.ok(dbConnectionService.getColumns(connectionId, tableName));
    }

    @Override
    public Resource<PluginConfiguration> toResource(PluginConfiguration element, Object... extras) {
        final Resource<PluginConfiguration> resource = resourceService.toResource(element);
        resourceService.addLink(resource, this.getClass(), "getDBConnection", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, element.getId()));
        resourceService.addLink(resource, this.getClass(), "deleteDBConnection", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, element.getId()));
        resourceService.addLink(resource, this.getClass(), "updateDBConnection", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, element.getId()),
                                MethodParamFactory.build(PluginConfiguration.class));
        resourceService.addLink(resource, this.getClass(), "getAllDBConnections", LinkRels.LIST);
        return resource;
    }

}
