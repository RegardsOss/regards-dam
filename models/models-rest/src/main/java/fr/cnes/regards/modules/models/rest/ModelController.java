/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
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
import fr.cnes.regards.modules.models.domain.ModelType;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.modules.models.signature.IModelSignature;

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
public class ModelController implements IModelSignature, IResourceController<Model> {

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

    @Override
    @ResourceAccess(description = "List all models")
    public ResponseEntity<List<Resource<Model>>> getModels(ModelType pType) {
        return ResponseEntity.ok(toResources(modelService.getModels(pType)));
    }

    @Override
    @ResourceAccess(description = "Create a model")
    public ResponseEntity<Resource<Model>> createModel(Model pModel) throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.createModel(pModel)));
    }

    @Override
    @ResourceAccess(description = "Get a model")
    public ResponseEntity<Resource<Model>> getModel(Long pModelId) throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.getModel(pModelId)));
    }

    @Override
    @ResourceAccess(description = "Update a model")
    public ResponseEntity<Resource<Model>> updateModel(Long pModelId, Model pModel) throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.updateModel(pModelId, pModel)));
    }

    @Override
    @ResourceAccess(description = "Delete a model")
    public ResponseEntity<Void> deleteModel(Long pModelId) throws ModuleException {
        modelService.deleteModel(pModelId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @ResourceAccess(description = "Duplicate a model")
    public ResponseEntity<Resource<Model>> duplicateModel(Long pModelId, Model pModel) throws ModuleException {
        return ResponseEntity.ok(toResource(modelService.duplicateModel(pModelId, pModel)));
    }

    @Override
    @ResourceAccess(description = "Export a model")
    public void exportModel(HttpServletRequest pRequest, HttpServletResponse pResponse, Long pModelId)
            throws ModuleException {
        // TODO Auto-generated method stub

    }

    @Override
    @ResourceAccess(description = "Import a model")
    public ResponseEntity<String> importModel(MultipartFile pFile) throws ModuleException {
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
