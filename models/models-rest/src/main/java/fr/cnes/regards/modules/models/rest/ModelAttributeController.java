/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IModelAttributeService;

/**
 * REST controller for managing {@link ModelAttribute}
 *
 * @author Marc Sordi
 *
 */
@RequestMapping(ModelAttributeController.TYPE_MAPPING)
public class ModelAttributeController implements IResourceController<ModelAttribute> {

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/models/{pModelId}/attributes";

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

    /**
     * Get all {@link ModelAttribute}
     *
     * @param pModelId
     *            {@link Model} identifier
     * @return list of linked {@link ModelAttribute}
     * @throws ModuleException
     *             if model unknown
     */
    @ResourceAccess(description = "List all model attributes")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<ModelAttribute>>> getModelAttributes(@PathVariable Long pModelId)
            throws ModuleException {
        return ResponseEntity.ok(toResources(modelAttributeService.getModelAttributes(pModelId), pModelId));
    }

    /**
     * Link an {@link AttributeModel} to a {@link Model} with a {@link ModelAttribute}.<br/>
     * This method is only available for {@link AttributeModel} in <b>default</b> {@link Fragment} (i.e. without name
     * space).
     *
     * @param pModelId
     *            {@link Model} identifier
     * @param pModelAttribute
     *            {@link ModelAttribute} to link
     * @return the {@link ModelAttribute} representing the link between the {@link Model} and the {@link AttributeModel}
     * @throws ModuleException
     *             if assignation cannot be done
     */
    @ResourceAccess(description = "Bind an attribute to a model")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<ModelAttribute>> bindAttributeToModel(@PathVariable Long pModelId,
            @Valid @RequestBody ModelAttribute pModelAttribute) throws ModuleException {
        return ResponseEntity
                .ok(toResource(modelAttributeService.bindAttributeToModel(pModelId, pModelAttribute), pModelId));
    }

    /**
     * Retrieve a {@link ModelAttribute} linked to a {@link Model} id
     *
     * @param pModelId
     *            model identifier
     * @param pAttributeId
     *            attribute id
     * @return linked model attribute
     * @throws ModuleException
     *             if attribute cannot be retrieved
     */
    @ResourceAccess(description = "Get a model attribute")
    @RequestMapping(method = RequestMethod.GET, value = "/{pAttributeId}")
    public ResponseEntity<Resource<ModelAttribute>> getModelAttribute(@PathVariable Long pModelId,
            @PathVariable Long pAttributeId) throws ModuleException {
        return ResponseEntity.ok(toResource(modelAttributeService.getModelAttribute(pModelId, pAttributeId), pModelId));
    }

    /**
     * Allow to update calculation properties
     *
     * @param pModelId
     *            model identifier
     * @param pAttributeId
     *            attribute id
     * @param pModelAttribute
     *            attribute
     * @return update model attribute
     * @throws ModuleException
     *             if attribute cannot be updated
     */
    @ResourceAccess(description = "Update a model attribute")
    @RequestMapping(method = RequestMethod.PUT, value = "/{pAttributeId}")
    public ResponseEntity<Resource<ModelAttribute>> updateModelAttribute(@PathVariable Long pModelId,
            @PathVariable Long pAttributeId, @Valid @RequestBody ModelAttribute pModelAttribute)
            throws ModuleException {
        return ResponseEntity
                .ok(toResource(modelAttributeService.updateModelAttribute(pModelId, pAttributeId, pModelAttribute),
                               pModelId));
    }

    /**
     * Unlink a {@link ModelAttribute} from a {@link Model}.<br/>
     * This method is only available for {@link AttributeModel} in <b>default</b> {@link Fragment} (i.e. without
     * namespace).
     *
     * @param pModelId
     *            model identifier
     * @param pAttributeId
     *            attribute id
     * @return nothing
     * @throws ModuleException
     *             if attribute cannot be removed
     */
    @ResourceAccess(description = "Unbind an attribute from a model")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pAttributeId}")
    public ResponseEntity<Void> unbindAttributeFromModel(@PathVariable Long pModelId, @PathVariable Long pAttributeId)
            throws ModuleException {
        modelAttributeService.unbindAttributeFromModel(pModelId, pAttributeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Link all {@link AttributeModel} of a particular {@link Fragment} to a model creating {@link ModelAttribute}.<br/>
     * This method is only available for {@link AttributeModel} in a <b>particular</b> {@link Fragment} (i.e. with name
     * space, not default one).
     *
     * @param pModelId
     *            model identifier
     * @param pFragmentId
     *            fragment identifier
     * @return linked model attributes
     * @throws ModuleException
     *             if binding cannot be done
     */
    @ResourceAccess(description = "Bind fragment attributes to a model")
    @RequestMapping(method = RequestMethod.POST, value = "/fragments/{pFragmentId}")
    public ResponseEntity<List<Resource<ModelAttribute>>> bindNSAttributeToModel(@PathVariable Long pModelId,
            @PathVariable Long pFragmentId) throws ModuleException {
        return ResponseEntity
                .ok(toResources(modelAttributeService.bindNSAttributeToModel(pModelId, pFragmentId), pModelId));
    }

    /**
     * Unlink all {@link AttributeModel} of a particular {@link Fragment} from a model deleting all associated
     * {@link ModelAttribute}.<br/>
     * This method is only available for {@link AttributeModel} in a <b>particular</b> {@link Fragment} (i.e. with name
     * space, not default one).
     *
     * @param pModelId
     *            model identifier
     * @param pFragmentId
     *            fragment identifier
     * @return linked model attributes
     * @throws ModuleException
     *             if binding cannot be done
     */
    @ResourceAccess(description = "Unbind fragment attributes from a model")
    @RequestMapping(method = RequestMethod.DELETE, value = "/fragments/{pFragmentId}")
    public ResponseEntity<Void> unbindNSAttributeFromModel(@PathVariable Long pModelId, @PathVariable Long pFragmentId)
            throws ModuleException {
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
