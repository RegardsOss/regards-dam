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
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.datasources.service.IDataSourceService;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * REST interface for managing data {@link Model}
 * @author Christophe Mertz
 */
@RestController
@ModuleInfo(name = "datasource", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS SI",
        documentation = "http://test")
@RequestMapping(DataSourceController.TYPE_MAPPING)
public class DataSourceController implements IResourceController<DataSource> {

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/datasources";

    /**
     * DBConnectionService attribute service
     */
    @Autowired
    private IDataSourceService dataSourceService;

    /**
     * Resource service
     */
    @Autowired
    private IResourceService resourceService;

    /**
     * Retrieve all {@link DataSource}.
     * @return a list of {@link PluginConfiguration}
     */
    @ResourceAccess(description = "List all the datasources defined for the plugin type IDataSourcePlugin")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<DataSource>>> getAllDataSources() {
        return ResponseEntity.ok(toResources(dataSourceService.getAllDataSources()));
    }

    /**
     * Create a {@link DataSource}.</br>
     * A {@link PluginConfiguration} for the plugin type {@link IDBConnectionPlugin} is created.
     * @param pDatasource the DataSource used to create the {@link PluginConfiguration}
     * @return the created {@link DataSource}
     * @throws ModuleException if problem occurs during plugin configuration creation
     */
    @ResourceAccess(description = "Create a DataSource")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<DataSource>> createDataSource(@Valid @RequestBody DataSource pDatasource)
            throws ModuleException {
        return ResponseEntity.ok(toResource(dataSourceService.createDataSource(pDatasource)));
    }

    /**
     * Get a {@link DataSource}
     * @param pPluginConfId {@link PluginConfiguration} identifier
     * @return a {@link PluginConfiguration}
     * @throws ModuleException if plugin configuration cannot be retrieved
     */
    @ResourceAccess(
            description = "Get a DataSource ie a identifier of a PluginConfiguration for a plugin type IDataSourcePlugin")
    @RequestMapping(method = RequestMethod.GET, value = "/{pPluginConfId}")
    public ResponseEntity<Resource<DataSource>> getDataSource(@PathVariable Long pPluginConfId) throws ModuleException {
        return ResponseEntity.ok(toResource(dataSourceService.getDataSource(pPluginConfId)));
    }

    /**
     * Allow to update {@link PluginConfiguration} for the plugin type {@link IDataSourcePlugin}
     * @param pPluginConfId {@link PluginConfiguration} identifier
     * @param pDataSource {@link DataSource} to update
     * @return updated {@link PluginConfiguration}
     * @throws ModuleException if plugin configuration cannot be updated
     */
    @ResourceAccess(description = "Update a plugin configuration defined for the plugin type IDataSourcePlugin")
    @RequestMapping(method = RequestMethod.PUT, value = "/{pPluginConfId}")
    public ResponseEntity<Resource<DataSource>> updateDataSource(@PathVariable Long pPluginConfId,
            @Valid @RequestBody DataSource pDataSource) throws ModuleException {
        if (!pPluginConfId.equals(pDataSource.getPluginConfigurationId())) {
            throw new EntityInconsistentIdentifierException(pPluginConfId, pDataSource.getPluginConfigurationId(),
                                                            PluginConfiguration.class);
        }
        return ResponseEntity.ok(toResource(dataSourceService.updateDataSource(pDataSource)));
    }

    /**
     * Delete a {@link PluginConfiguration} defined for the plugin type {@link IDataSourcePlugin}
     * @param pPluginConfId {@link PluginConfiguration} identifier
     * @return nothing
     * @throws ModuleException if {@link PluginConfiguration} cannot be deleted
     */
    @ResourceAccess(description = "Delete a plugin configuration defined for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pPluginConfId}")
    public ResponseEntity<Void> deleteDataSource(@PathVariable Long pPluginConfId) throws ModuleException {
        dataSourceService.deleteDataSouce(pPluginConfId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public Resource<DataSource> toResource(DataSource pElement, Object... pExtras) {
        final Resource<DataSource> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "getDataSource", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pElement.getPluginConfigurationId()));
        resourceService.addLink(resource, this.getClass(), "deleteDataSource", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pElement.getPluginConfigurationId()));
        resourceService.addLink(resource, this.getClass(), "updateDataSource", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, pElement.getPluginConfigurationId()),
                                MethodParamFactory.build(DataSource.class));
        resourceService.addLink(resource, this.getClass(), "getAllDataSources", LinkRels.LIST);
        return resource;
    }

}
