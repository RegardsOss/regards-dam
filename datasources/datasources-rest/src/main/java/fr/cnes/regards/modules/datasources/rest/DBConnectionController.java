/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.rest;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import fr.cnes.regards.modules.datasources.domain.DBConnection;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.service.IDBConnectionService;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.FragmentService;

/**
 *
 * REST interface for managing data {@link Model}
 *
 * @author Christophe Mertz
 *
 */
@RestController
// CHECKSTYLE:OFF
@ModuleInfo(name = "dbconnection", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS SI",
        documentation = "http://test")
// CHECKSTYLE:ON
@RequestMapping(DBConnectionController.TYPE_MAPPING)
public class DBConnectionController implements IResourceController<PluginConfiguration> {

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/connections";

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentService.class);

    /**
     * DBConnectionService attribute service
     */
    private final IDBConnectionService dbConnectionService;

    /**
     * Resource service
     */
    private final IResourceService resourceService;

    public DBConnectionController(IDBConnectionService pDBConnectionService, IResourceService pResourceService) {
        this.dbConnectionService = pDBConnectionService;
        this.resourceService = pResourceService;
    }

    /**
     * Retrieve all {@link PluginConfiguration} used by the plugin type {@link IDBConnectionPlugin}
     *
     * @return a list of {@link PluginConfiguration}
     */
    @ResourceAccess(description = "List all plugins configurations defined for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<PluginConfiguration>>> getAllDBConnections() {
        return ResponseEntity.ok(toResources(dbConnectionService.getAllDBConnections()));
    }

    /**
     * Create a {@link PluginConfiguration} for the plugin type {@link IDBConnectionPlugin}
     *
     * @param pDbConnection
     *            the database connection used to create the {@link PluginConfiguration}
     * @return the created {@link PluginConfiguration}
     * @throws ModuleException
     *             if problem occurs during plugin configuration creation
     */
    @ResourceAccess(description = "Create a plugin configuration for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<PluginConfiguration>> createDBConnection(
            @Valid @RequestBody DBConnection pDbConnection) throws ModuleException {
        return ResponseEntity.ok(toResource(dbConnectionService.createDBConnection(pDbConnection)));
    }

    /**
     * Get a {@link PluginConfiguration}
     *
     * @param pConnectionId
     *            {@link PluginConfiguration} identifier
     * @return a {@link PluginConfiguration}
     * @throws ModuleException
     *             if plugin configuration cannot be retrieved
     */
    @ResourceAccess(description = "Get a model")
    @RequestMapping(method = RequestMethod.GET, value = "/{pConnectionId}")
    public ResponseEntity<Resource<PluginConfiguration>> getDBConnection(@PathVariable Long pConnectionId)
            throws ModuleException {
        return ResponseEntity.ok(toResource(dbConnectionService.getDBConnection(pConnectionId)));
    }

    /**
     * Allow to update {@link PluginConfiguration} for the plugin type {@link IDBConnectionPlugin}
     *
     * @param pConnectionId
     *            {@link PluginConfiguration} identifier
     * @param pDbConnection
     *            {@link DBConnection} to update
     * @return updated {@link PluginConfiguration}
     * @throws ModuleException
     *             if plugin configuration cannot be updated
     */
    @ResourceAccess(description = "Update a plugin configuration defined for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.PUT, value = "/{pConnectionId}")
    public ResponseEntity<Resource<PluginConfiguration>> updateDBConnection(@PathVariable Long pConnectionId,
            @Valid @RequestBody DBConnection pDbConnection) throws ModuleException {
        if (!pConnectionId.equals(pDbConnection.getPluginConfigurationId())) {
            throw new EntityInconsistentIdentifierException(pConnectionId, pDbConnection.getPluginConfigurationId(),
                    PluginConfiguration.class);
        }
        return ResponseEntity.ok(toResource(dbConnectionService.updateDBConnection(pDbConnection)));
    }

    /**
     * Delete a {@link PluginConfiguration} defined for the plugin type {@link IDBConnectionPlugin}
     *
     * @param pConnectionId
     *            {@link PluginConfiguration} identifier
     * @return nothing
     * @throws ModuleException
     *             if plugin configuration cannot be deleted
     */
    @ResourceAccess(description = "Delete a plugin configuration defined for the plugin type IDBConnectionPlugin")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pConnectionId}")
    public ResponseEntity<Void> deleteDBConnection(@PathVariable Long pConnectionId) throws ModuleException {
        dbConnectionService.deleteDBConnection(pConnectionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Test a {@link PluginConfiguration} defined for the plugin type {@link IDBConnectionPlugin}
     *
     * @param pConnectionId
     *            {@link PluginConfiguration} identifier
     * @return nothing
     * @throws ModuleException
     *             if problem occurs during test of the connection
     */
    @ResourceAccess(description = "Test the connection to the database")
    @RequestMapping(method = RequestMethod.POST, value = "/{pConnectionId}")
    public ResponseEntity<Boolean> testDBConnection(@PathVariable Long pConnectionId) throws ModuleException {
        return ResponseEntity.ok(dbConnectionService.testDBConnection(pConnectionId));
    }  
    
    @Override
    public Resource<PluginConfiguration> toResource(PluginConfiguration pElement, Object... pExtras) {
        final Resource<PluginConfiguration> resource = resourceService.toResource(pElement);
        // resourceService.addLink(resource, this.getClass(), "getModel", LinkRels.SELF,
        // MethodParamFactory.build(Long.class, pElement.getId()));
        // resourceService.addLink(resource, this.getClass(), "updateModel", LinkRels.UPDATE,
        // MethodParamFactory.build(Long.class, pElement.getId()),
        // MethodParamFactory.build(Model.class));
        // resourceService.addLink(resource, this.getClass(), "deleteModel", LinkRels.DELETE,
        // MethodParamFactory.build(Long.class, pElement.getId()));
        // resourceService.addLink(resource, this.getClass(), "getModels", LinkRels.LIST,
        // MethodParamFactory.build(EntityType.class));
        // // Import / Export
        // resourceService.addLink(resource, this.getClass(), "exportModel", "export",
        // MethodParamFactory.build(HttpServletRequest.class),
        // MethodParamFactory.build(HttpServletResponse.class),
        // MethodParamFactory.build(Long.class, pElement.getId()));
        // resourceService.addLink(resource, this.getClass(), "importModel", "import",
        // MethodParamFactory.build(MultipartFile.class));
        return resource;
    }

}
