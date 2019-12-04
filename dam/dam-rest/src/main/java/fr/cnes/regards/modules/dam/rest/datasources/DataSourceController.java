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
package fr.cnes.regards.modules.dam.rest.datasources;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

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
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDBConnectionPlugin;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDataSourcePlugin;
import fr.cnes.regards.modules.dam.rest.datasources.dto.DataSourceDTO;
import fr.cnes.regards.modules.dam.rest.datasources.exception.AssociatedDatasetExistsException;
import fr.cnes.regards.modules.dam.service.datasources.IDataSourceService;
import fr.cnes.regards.modules.dam.service.entities.IDatasetService;

/**
 * REST interface for Datasources plugin configuration ie only {@link IDataSourcePlugin} are concerned
 * @author Christophe Mertz
 * @author oroussel
 */
@RestController
@RequestMapping(DataSourceController.TYPE_MAPPING)
public class DataSourceController implements IResourceController<DataSourceDTO> {

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/datasources";

    /**
     * Datasource service
     */
    @Autowired
    private IDataSourceService dataSourceService;

    @Autowired
    private IDatasetService datasetService;

    /**
     * Resource service
     */
    @Autowired
    private IResourceService resourceService;

    /**
     * Retrieve all {@link IDataSourcePlugin} {@link PluginConfiguration}s.
     * @return a list of {@link PluginConfiguration}
     */
    @ResourceAccess(description = "List all plugin configurations of type IDataSourcePlugin")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<DataSourceDTO>>> getAllDataSources() {
        return ResponseEntity.ok(toResources(dataSourceService.getAllDataSources()));
    }

    /**
     * Create a data source.</br>
     * A {@link PluginConfiguration} for the plugin type {@link IDBConnectionPlugin} is created.
     * @param datasource the DataSource used to create the {@link PluginConfiguration}
     * @return the created data source
     * @throws ModuleException if problem occurs during plugin configuration creation
     */
    @ResourceAccess(description = "Create a DataSource")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<DataSourceDTO>> createDataSource(@Valid @RequestBody PluginConfiguration datasource)
            throws ModuleException {
        return ResponseEntity.ok(toResource(dataSourceService.createDataSource(datasource)));
    }

    /**
     * Get a data source
     * @param businessId {@link PluginConfiguration} identifier
     * @return a {@link PluginConfiguration}
     * @throws ModuleException if plugin configuration cannot be retrieved
     */
    @ResourceAccess(description = "Get a DataSource ie a PluginConfiguration of type IDataSourcePlugin")
    @RequestMapping(method = RequestMethod.GET, value = "/{businessId}")
    public ResponseEntity<Resource<DataSourceDTO>> getDataSource(@PathVariable(name = "businessId") String businessId)
            throws ModuleException {
        return ResponseEntity.ok(toResource(dataSourceService.getDataSource(businessId)));
    }

    /**
     * Allow to update {@link PluginConfiguration} for the plugin type {@link IDataSourcePlugin}
     * @param businessId {@link PluginConfiguration} identifier
     * @param dataSource data source to update
     * @return updated {@link PluginConfiguration}
     * @throws ModuleException if plugin configuration cannot be updated
     */
    @ResourceAccess(description = "Update a plugin configuration of type IDataSourcePlugin")
    @RequestMapping(method = RequestMethod.PUT, value = "/{businessId}")
    public ResponseEntity<Resource<DataSourceDTO>> updateDataSource(
            @PathVariable(name = "businessId") String businessId, @Valid @RequestBody PluginConfiguration dataSource)
            throws ModuleException {
        if (!businessId.equals(dataSource.getBusinessId())) {
            throw new EntityInconsistentIdentifierException(businessId, dataSource.getBusinessId(),
                    PluginConfiguration.class);
        }
        return ResponseEntity.ok(toResource(dataSourceService.updateDataSource(dataSource)));
    }

    /**
     * Delete a {@link PluginConfiguration} defined for the plugin type {@link IDataSourcePlugin}
     * @param businessId {@link PluginConfiguration} identifier
     * @return nothing
     * @throws AssociatedDatasetExistsException
     * @throws ModuleException if {@link PluginConfiguration} cannot be deleted
     */
    @ResourceAccess(description = "Delete a plugin configuration of type IDataSourcePlugin")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{businessId}")
    public ResponseEntity<Void> deleteDataSource(@PathVariable(name = "businessId") String businessId)
            throws AssociatedDatasetExistsException, ModuleException {
        try {
            dataSourceService.deleteDataSource(businessId);
        } catch (RuntimeException e) {
            // Ugliest method to manage constraints on entites which are associated to this datasource but because
            // of the overuse of plugins everywhere a billion of dependencies exist with some cyclics if we try to
            // do things cleanly so let's be pigs and do shit without any problems....
            // And ugliest of the ugliest, this exception is thrown at transaction commit that's why it is done here and
            // not into service
            if (e.getMessage().contains("fk_ds_plugin_conf_id")) {
                throw new AssociatedDatasetExistsException();
            }
            throw e;
        }
        return ResponseEntity.noContent().build();
    }

    private List<Resource<DataSourceDTO>> toResources(Collection<PluginConfiguration> confs) {
        return confs.stream().map(conf -> toResource(conf)).collect(Collectors.toList());
    }

    private Resource<DataSourceDTO> toResource(PluginConfiguration conf, Object... pExtras) {
        DataSourceDTO dto = new DataSourceDTO(datasetService.countByDataSource(conf.getId()), conf);
        return toResource(dto, pExtras);
    }

    @Override
    public Resource<DataSourceDTO> toResource(DataSourceDTO conf, Object... pExtras) {
        Resource<DataSourceDTO> resource = resourceService.toResource(conf);
        resourceService.addLink(resource, this.getClass(), "getDataSource", LinkRels.SELF,
                                MethodParamFactory.build(String.class, conf.getBusinessId()));
        if (conf.getAssociatedDatasets() == 0) {
            resourceService.addLink(resource, this.getClass(), "deleteDataSource", LinkRels.DELETE,
                                    MethodParamFactory.build(String.class, conf.getBusinessId()));
        }
        resourceService.addLink(resource, this.getClass(), "updateDataSource", LinkRels.UPDATE,
                                MethodParamFactory.build(String.class, conf.getBusinessId()),
                                MethodParamFactory.build(PluginConfiguration.class));
        resourceService.addLink(resource, this.getClass(), "getAllDataSources", LinkRels.LIST);
        return resource;
    }

}
