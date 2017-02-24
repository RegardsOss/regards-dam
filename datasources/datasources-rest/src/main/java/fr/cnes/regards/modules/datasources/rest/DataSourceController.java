/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
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
 *
 * REST interface for managing data {@link Model}
 *
 * @author Christophe Mertz
 *
 */
@RestController
// CHECKSTYLE:OFF
@ModuleInfo(name = "datasource", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS SI",
        documentation = "http://test")
// CHECKSTYLE:ON
@RequestMapping(DataSourceController.TYPE_MAPPING)
public class DataSourceController implements IResourceController<PluginConfiguration> {

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/datasources";

    /**
     * DBConnectionService attribute service
     */
    private final IDataSourceService dataSourceService;

    /**
     * Resource service
     */
    private final IResourceService resourceService;

    public DataSourceController(IDataSourceService pDBConnectionService, IResourceService pResourceService) {
        this.dataSourceService = pDBConnectionService;
        this.resourceService = pResourceService;
    }

    /**
     * Retrieve all {@link PluginConfiguration} used by the plugin type {@link IDataSourcePlugin}
     *
     * @return a list of {@link PluginConfiguration}
     */
    @ResourceAccess(description = "List all plugin's configurations defined for the plugin type IDataSourcePlugin")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<PluginConfiguration>>> getAllDataSources() {
        return ResponseEntity.ok(toResources(dataSourceService.getAllDataSources()));
    }

    /**
     * Create a {@link PluginConfiguration} for the plugin type {@link IDBConnectionPlugin}
     *
     * @param pDatasource
     *            the DataSource used to create the {@link PluginConfiguration}
     * @return the created {@link PluginConfiguration}
     * @throws ModuleException
     *             if problem occurs during plugin configuration creation
     */
    @ResourceAccess(description = "Create a plugin configuration for the plugin type IDataSourcePlugin")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<PluginConfiguration>> createDataSource(@Valid @RequestBody DataSource pDatasource)
            throws ModuleException {
        return ResponseEntity.ok(toResource(dataSourceService.createDataSource(pDatasource)));
    }

    /**
     * Get a {@link PluginConfiguration}
     *
     * @param pPluginConfId
     *            {@link PluginConfiguration} identifier
     * @return a {@link PluginConfiguration}
     * @throws ModuleException
     *             if plugin configuration cannot be retrieved
     */
    @ResourceAccess(description = "Get a plugin configuration for a plugin type IDataSourcePlugin")
    @RequestMapping(method = RequestMethod.GET, value = "/{pPluginConfId}")
    public ResponseEntity<Resource<PluginConfiguration>> getDataSource(@PathVariable Long pPluginConfId)
            throws ModuleException {
        return ResponseEntity.ok(toResource(dataSourceService.getDataSource(pPluginConfId)));
    }

    /**
     * Allow to update {@link PluginConfiguration} for the plugin type {@link IDataSourcePlugin}
     *
     * @param pPluginConfId
     *            {@link PluginConfiguration} identifier
     * @param pDataSource
     *            {@link DataSource} to update
     * @return updated {@link PluginConfiguration}
     * @throws ModuleException
     *             if plugin configuration cannot be updated
     */
    @ResourceAccess(description = "Update a plugin configuration defined for the plugin type IDataSourcePlugin")
    @RequestMapping(method = RequestMethod.PUT, value = "/{pPluginConfId}")
    public ResponseEntity<Resource<PluginConfiguration>> updateDBConnection(@PathVariable Long pPluginConfId,
            @Valid @RequestBody DataSource pDataSource) throws ModuleException {
        if (!pPluginConfId.equals(pDataSource.getPluginConfigurationId())) {
            throw new EntityInconsistentIdentifierException(pPluginConfId, pDataSource.getPluginConfigurationId(),
                    PluginConfiguration.class);
        }
        return ResponseEntity.ok(toResource(dataSourceService.updateDataSource(pDataSource)));
    }

    /**
     * Delete a {@link PluginConfiguration} defined for the plugin type {@link IDataSourcePlugin}
     *
     * @param pPluginConfId
     *            {@link PluginConfiguration} identifier
     * @return nothing
     * @throws ModuleException
     *             if plugin configuration cannot be deleted
     */
    @ResourceAccess(description = "Delete a plugin configuration defined for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pPluginConfId}")
    public ResponseEntity<Void> deleteDBConnection(@PathVariable Long pPluginConfId) throws ModuleException {
        dataSourceService.deleteDataSouce(pPluginConfId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public Resource<PluginConfiguration> toResource(PluginConfiguration pElement, Object... pExtras) {
        final Resource<PluginConfiguration> resource = resourceService.toResource(pElement);
        return resource;
    }

}
