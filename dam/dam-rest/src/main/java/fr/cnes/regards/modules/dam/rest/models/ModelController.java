/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.models;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.service.models.FragmentService;
import fr.cnes.regards.modules.dam.service.models.IModelService;

/**
 * REST interface for managing data {@link Model}
 * @author msordi
 * @author Xavier-Alexandre Brochard
 */
@RestController

@ModuleInfo(name = "models", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS SI",
        documentation = "http://test")
@RequestMapping(ModelController.TYPE_MAPPING)
public class ModelController implements IResourceController<Model> {

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/models";

    /**
     * Model management mapping
     */
    public static final String MODEL_MAPPING = "/{modelName}";

    /**
     * Prefix for imported/exported filename
     */
    private static final String MODEL_FILE_PREFIX = "model-";

    /**
     * Suffix for imported/exported filename
     */
    private static final String MODEL_EXTENSION = ".xml";

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

    /**
     * Constructor
     * @param pModelService Model attribute service
     * @param pResourceService Resource service
     */
    public ModelController(IModelService pModelService, IResourceService pResourceService) {
        this.modelService = pModelService;
        this.resourceService = pResourceService;
    }

    /**
     * Retrieve all {@link Model}. The request can be filtered by {@link EntityType}.
     * @param pType filter
     * @return a list of {@link Model}
     */
    @ResourceAccess(description = "List all models")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<Model>>> getModels(
            @RequestParam(value = "type", required = false) EntityType pType) {
        return ResponseEntity.ok(toResources(modelService.getModels(pType)));
    }

    /**
     * Create a {@link Model}
     * @param pModel the {@link Model} to create
     * @return the created {@link Model}
     * @throws ModuleException if problem occurs during model creation
     */
    @ResourceAccess(description = "Create a model")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<Model>> createModel(@Valid @RequestBody Model pModel) throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.createModel(pModel)));
    }

    /**
     * Get a {@link Model} without its attributes
     * @param pModelId {@link Model} identifier
     * @return a {@link Model}
     * @throws ModuleException if model cannot be retrieved
     */
    @ResourceAccess(description = "Get a model", role = DefaultRole.PUBLIC)
    @RequestMapping(method = RequestMethod.GET, value = MODEL_MAPPING)
    public ResponseEntity<Resource<Model>> getModel(@PathVariable String modelName) throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.getModelByName(modelName)));
    }

    /**
     * Allow to update {@link Model} description
     * @param pModelId {@link Model} identifier
     * @param pModel {@link Model} to update
     * @return updated {@link Model}
     * @throws ModuleException if model cannot be updated
     */
    @ResourceAccess(description = "Update a model")
    @RequestMapping(method = RequestMethod.PUT, value = MODEL_MAPPING)
    public ResponseEntity<Resource<Model>> updateModel(@PathVariable String modelName, @Valid @RequestBody Model pModel)
            throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.updateModel(modelName, pModel)));
    }

    /**
     * Delete a {@link Model} and detach all {@link ModelAttrAssoc}
     * @param pModelId {@link Model} identifier
     * @return nothing
     * @throws ModuleException if model cannot be deleted
     */
    @ResourceAccess(description = "Delete a model")
    @RequestMapping(method = RequestMethod.DELETE, value = MODEL_MAPPING)
    public ResponseEntity<Void> deleteModel(@PathVariable String modelName) throws ModuleException {
        modelService.deleteModel(modelName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Duplicate a model
     * @param pModelId {@link Model} to duplicate
     * @param pModel new model to create with its own name, description and type
     * @return a new model based on actual one
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Duplicate a model")
    @RequestMapping(method = RequestMethod.POST, value = MODEL_MAPPING + "/duplicate")
    public ResponseEntity<Resource<Model>> duplicateModel(@PathVariable String modelName,
            @Valid @RequestBody Model pModel) throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.duplicateModel(modelName, pModel)));
    }

    /**
     * Export a model
     * @param pRequest HTTP request
     * @param pResponse HTTP response
     * @param pModelId model to export
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Export a model")
    @RequestMapping(method = RequestMethod.GET, value = MODEL_MAPPING + "/export")
    public void exportModel(HttpServletRequest pRequest, HttpServletResponse pResponse, @PathVariable String modelName)
            throws ModuleException {
        final Model model = modelService.getModelByName(modelName);
        final String exportedFilename = MODEL_FILE_PREFIX + model.getName() + MODEL_EXTENSION;

        // Produce octet stream to force navigator opening "save as" dialog
        pResponse.setContentType(MediaType.APPLICATION_XML_VALUE);
        pResponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportedFilename + "\"");

        try {
            modelService.exportModel(modelName, pResponse.getOutputStream());
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
     * @param pFile model to import
     * @return nothing
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Import a model")
    @RequestMapping(method = RequestMethod.POST, value = "/import")
    public ResponseEntity<Resource<Model>> importModel(@RequestParam("file") MultipartFile pFile)
            throws ModuleException {
        try {
            Model model = modelService.importModel(pFile.getInputStream());
            return new ResponseEntity<>(toResource(model), HttpStatus.CREATED);
        } catch (IOException e) {
            final String message = "Error with file stream while importing model.";
            LOGGER.error(message, e);
            throw new ModuleException(e);
        }
    }

    @Override
    public Resource<Model> toResource(Model pElement, Object... pExtras) {
        final Resource<Model> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "getModel", LinkRels.SELF,
                                MethodParamFactory.build(String.class, pElement.getName()));
        resourceService.addLink(resource, this.getClass(), "updateModel", LinkRels.UPDATE,
                                MethodParamFactory.build(String.class, pElement.getName()),
                                MethodParamFactory.build(Model.class));
        resourceService.addLink(resource, this.getClass(), "deleteModel", LinkRels.DELETE,
                                MethodParamFactory.build(String.class, pElement.getName()));
        resourceService.addLink(resource, this.getClass(), "getModels", LinkRels.LIST,
                                MethodParamFactory.build(EntityType.class));
        // Export
        resourceService.addLink(resource, this.getClass(), "exportModel", "export",
                                MethodParamFactory.build(HttpServletRequest.class),
                                MethodParamFactory.build(HttpServletResponse.class),
                                MethodParamFactory.build(String.class, pElement.getName()));
        return resource;
    }

}
