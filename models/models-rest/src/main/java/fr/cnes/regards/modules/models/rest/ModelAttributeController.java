/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.service.IModelAttributeService;
import fr.cnes.regards.modules.models.signature.IModelAttributeSignature;

/**
 * REST controller for managing {@link ModelAttribute}
 *
 * @author Marc Sordi
 *
 */
public class ModelAttributeController implements IModelAttributeSignature, IResourceController<ModelAttribute> {

    /**
     * Model attribute service
     */
    private final IModelAttributeService modelAttributeService;

    /**
     * Resource service
     */
    private final IResourceService resourceService;

    public ModelAttributeController(IModelAttributeService pModelAttributeService, IResourceService pResourceService) {
        this.modelAttributeService = pModelAttributeService;
        this.resourceService = pResourceService;
    }

    @Override
    @ResourceAccess(description = "List all model attributes")
    public ResponseEntity<List<Resource<ModelAttribute>>> getModelAttributes(Long pModelId) throws ModuleException {
        return ResponseEntity.ok(toResources(modelAttributeService.getModelAttributes(pModelId), pModelId));
    }

    @Override
    @ResourceAccess(description = "Bind an attribute to a model")
    public ResponseEntity<Resource<ModelAttribute>> bindAttributeToModel(Long pModelId, ModelAttribute pModelAttribute)
            throws ModuleException {
        return ResponseEntity
                .ok(toResource(modelAttributeService.bindAttributeToModel(pModelId, pModelAttribute), pModelId));
    }

    @Override
    @ResourceAccess(description = "Get a model attribute")
    public ResponseEntity<Resource<ModelAttribute>> getModelAttribute(Long pModelId, Long pAttributeId)
            throws ModuleException {
        return ResponseEntity.ok(toResource(modelAttributeService.getModelAttribute(pModelId, pAttributeId), pModelId));
    }

    @Override
    @ResourceAccess(description = "Update a model attribute")
    public ResponseEntity<Resource<ModelAttribute>> updateModelAttribute(Long pModelId, Long pAttributeId,
            ModelAttribute pModelAttribute) throws ModuleException {
        return ResponseEntity
                .ok(toResource(modelAttributeService.updateModelAttribute(pModelId, pAttributeId, pModelAttribute),
                               pModelId));
    }

    @Override
    @ResourceAccess(description = "Unbind an attribute from a model")
    public ResponseEntity<Void> unbindAttributeFromModel(Long pModelId, Long pAttributeId) throws ModuleException {
        modelAttributeService.unbindAttributeFromModel(pModelId, pAttributeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @ResourceAccess(description = "Bind fragment attributes to a model")
    public ResponseEntity<List<Resource<ModelAttribute>>> bindNSAttributeToModel(Long pModelId, Long pFragmentId)
            throws ModuleException {
        return ResponseEntity
                .ok(toResources(modelAttributeService.bindNSAttributeToModel(pModelId, pFragmentId), pModelId));
    }

    @Override
    @ResourceAccess(description = "Unbind fragment attributes from a model")
    public ResponseEntity<Void> unbindNSAttributeFromModel(Long pModelId, Long pFragmentId) throws ModuleException {
        modelAttributeService.unbindNSAttributeToModel(pModelId, pFragmentId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public Resource<ModelAttribute> toResource(ModelAttribute pElement, Object... pExtras) {
        final Resource<ModelAttribute> resource = resourceService.toResource(pElement);

        final Long modelId = (Long) pExtras[0];

        resourceService.addLink(resource, this.getClass(), "getModelAttribute", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, modelId),
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "updateModelAttribute", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, modelId),
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(ModelAttribute.class));
        resourceService.addLink(resource, this.getClass(), "unbindAttributeFromModel", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, modelId),
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "getModelAttributes", LinkRels.LIST,
                                MethodParamFactory.build(Long.class, modelId));
        return resource;
    }
}
