/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.ModelType;
import fr.cnes.regards.modules.models.service.FragmentService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 *
 * REST interface for managing data {@link Model}
 *
 * @author msordi
 *
 */
@RestController
// CHECKSTYLE:OFF
@ModuleInfo(name = "models", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS SI", documentation = "http://test")
// CHECKSTYLE:ON
@RequestMapping(ModelController.TYPE_MAPPING)
public class ModelController implements IResourceController<Model> {

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/models";

    /**
     * Prefix for imported/exported filename
     */
    private static final String MODEL_FILE_PREFIX = "model-";

    /**
     * Suffix for imported/exported filename
     */
    private static final String JSON_EXTENSION = ".json";

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentService.class);

    /**
     * Model attribute service
     */
    private final IModelService modelService;

    /**
     * Resource service
     */
    private final IResourceService resourceService;

    public ModelController(IModelService pModelService, IResourceService pResourceService) {
        this.modelService = pModelService;
        this.resourceService = pResourceService;
    }

    /**
     * Retrieve all {@link Model}. The request can be filtered by {@link ModelType}.
     *
     * @param pType
     *            filter
     * @return a list of {@link Model}
     */
    @ResourceAccess(description = "List all models")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<Model>>> getModels(
            @RequestParam(value = "type", required = false) ModelType pType) {
        return ResponseEntity.ok(toResources(modelService.getModels(pType)));
    }

    /**
     * Create a {@link Model}
     *
     * @param pModel
     *            the {@link Model} to create
     * @return the created {@link Model}
     * @throws ModuleException
     *             if problem occurs during model creation
     */
    @ResourceAccess(description = "Create a model")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<Model>> createModel(@Valid @RequestBody Model pModel) throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.createModel(pModel)));
    }

    /**
     * Get a {@link Model} without its attributes
     *
     * @param pModelId
     *            {@link Model} identifier
     * @return a {@link Model}
     * @throws ModuleException
     *             if model cannot be retrieved
     */
    @ResourceAccess(description = "Get a model")
    @RequestMapping(method = RequestMethod.GET, value = "/{pModelId}")
    public ResponseEntity<Resource<Model>> getModel(@PathVariable Long pModelId) throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.getModel(pModelId)));
    }

    /**
     * Allow to update {@link Model} description
     *
     * @param pModelId
     *            {@link Model} identifier
     * @param pModel
     *            {@link Model} to update
     * @return updated {@link Model}
     * @throws ModuleException
     *             if model cannot be updated
     */
    @ResourceAccess(description = "Update a model")
    @RequestMapping(method = RequestMethod.PUT, value = "/{pModelId}")
    public ResponseEntity<Resource<Model>> updateModel(@PathVariable Long pModelId, @Valid @RequestBody Model pModel)
            throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.updateModel(pModelId, pModel)));
    }

    /**
     * Delete a {@link Model} and detach all {@link ModelAttribute}
     *
     * @param pModelId
     *            {@link Model} identifier
     * @return nothing
     * @throws ModuleException
     *             if model cannot be deleted
     */
    @ResourceAccess(description = "Delete a model")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pModelId}")
    public ResponseEntity<Void> deleteModel(@PathVariable Long pModelId) throws ModuleException {
        modelService.deleteModel(pModelId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Duplicate a model
     *
     * @param pModelId
     *            {@link Model} to duplicate
     * @param pModel
     *            new model to create with its own name and description (other informations are skipped)
     * @return a new model based on actual one
     * @throws ModuleException
     *             if error occurs!
     */
    @ResourceAccess(description = "Duplicate a model")
    @RequestMapping(method = RequestMethod.POST, value = "/{pModelId}/duplicate")
    public ResponseEntity<Resource<Model>> duplicateModel(@PathVariable Long pModelId, @Valid @RequestBody Model pModel)
            throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.duplicateModel(pModelId, pModel)));
    }

    /**
     * Export a model
     *
     * @param pRequest
     *            HTTP request
     * @param pResponse
     *            HTTP response
     * @param pModelId
     *            model to export
     * @throws ModuleException
     *             if error occurs!
     */
    @ResourceAccess(description = "Export a model")
    @RequestMapping(method = RequestMethod.GET, value = "/{pModelId}/export")
    public void exportModel(HttpServletRequest pRequest, HttpServletResponse pResponse, @PathVariable Long pModelId)
            throws ModuleException {
        final Model model = modelService.getModel(pModelId);
        final String exportedFilename = MODEL_FILE_PREFIX + model.getName() + JSON_EXTENSION;

        // Produce octet stream to force navigator opening "save as" dialog
        pResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        pResponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportedFilename + "\"");

        try {
            modelService.exportModel(pModelId, pResponse.getOutputStream());
            pResponse.getOutputStream().flush();
        } catch (IOException e) {
            final String message = String.format("Error with servlet output stream while exporting model %s.",
                                                 model.getName());
            LOGGER.error(message, e);
            throw new ModuleException(e);
        }
    }

    /**
     * Import a model
     *
     * @param pFile
     *            model to import
     * @return TODO
     * @throws ModuleException
     *             if error occurs!
     */
    // TODO adapt signature / see Spring MVC doc p.22.10
    @ResourceAccess(description = "Import a model")
    @RequestMapping(method = RequestMethod.POST, value = "/import")
    public ResponseEntity<String> importModel(@RequestParam("file") MultipartFile pFile) throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Resource<Model> toResource(Model pElement, Object... pExtras) {
        final Resource<Model> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "getModel", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "updateModel", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Model.class));
        resourceService.addLink(resource, this.getClass(), "deleteModel", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "getModels", LinkRels.LIST,
                                MethodParamFactory.build(ModelType.class));
        // Import / Export
        resourceService.addLink(resource, this.getClass(), "exportModel", "export",
                                MethodParamFactory.build(HttpServletRequest.class),
                                MethodParamFactory.build(HttpServletResponse.class),
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "importModel", "import",
                                MethodParamFactory.build(MultipartFile.class));
        return resource;
    }

}
