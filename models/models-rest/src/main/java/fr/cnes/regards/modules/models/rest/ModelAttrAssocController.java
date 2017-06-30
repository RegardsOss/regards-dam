/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.Collection;
import java.util.List;

import javax.validation.Valid;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.*;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.TypeMetadataConfMapping;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;

/**
 * REST controller for managing {@link ModelAttrAssoc}
 *
 * @author Marc Sordi
 */
@RestController
@RequestMapping(ModelAttrAssocController.BASE_MAPPING)
public class ModelAttrAssocController implements IResourceController<ModelAttrAssoc> {

    /**
     * Base mapping
     */
    public static final String BASE_MAPPING = "/models";

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/{pModelId}/attributes";

    public static final String FRAGMENT_BIND_MAPPING = "/fragments";

    public static final String FRAGMENT_UNBIND_MAPPING = "/fragments/{pFragmentId}";

    public static final String ASSOCS_MAPPING = "/assocs";

    public static final String COMPUTATION_TYPE_MAPPING = ASSOCS_MAPPING +"/computation/types";

    /**
     * Model attribute association service
     */
    private final IModelAttrAssocService modelAttrAssocService;

    /**
     * Resource service
     */
    private final IResourceService resourceService;

    /**
     * Constructor
     * @param pModelAttrAssocService Model attribute association service
     * @param pResourceService Resource service
     */
    public ModelAttrAssocController(IModelAttrAssocService pModelAttrAssocService, IResourceService pResourceService) {
        modelAttrAssocService = pModelAttrAssocService;
        resourceService = pResourceService;
    }

    @ResourceAccess(
            description = "endpoint allowing to retrieve all links between models and attribute for a given type of entity")
    @RequestMapping(path = ASSOCS_MAPPING, method = RequestMethod.GET)
    public ResponseEntity<Collection<ModelAttrAssoc>> getModelAttrAssocsFor(
            @RequestParam(name = "type", required = false) EntityType type) {
        Collection<ModelAttrAssoc> assocs = modelAttrAssocService.getModelAttrAssocsFor(type);
        return ResponseEntity.ok(assocs);
    }

    @ResourceAccess(
            description = "endpoint allowing to retrieve which plugin configuration can be used for which attribute type with which possible metadata")
    @RequestMapping(path = COMPUTATION_TYPE_MAPPING, method = RequestMethod.GET)
    public ResponseEntity<List<Resource<TypeMetadataConfMapping>>> getMappingForComputedAttribute() {
        return ResponseEntity.ok(HateoasUtils.wrapList(modelAttrAssocService.retrievePossibleMappingsForComputed()));
    }

    /**
     * Get all {@link ModelAttrAssoc}
     *
     * @param pModelId {@link Model} identifier
     * @return list of linked {@link ModelAttrAssoc}
     * @throws ModuleException if model unknown
     */
    @ResourceAccess(description = "List all model attributes")
    @RequestMapping(path = TYPE_MAPPING, method = RequestMethod.GET)
    public ResponseEntity<List<Resource<ModelAttrAssoc>>> getModelAttrAssocs(@PathVariable("pModelId") Long pModelId)
            throws ModuleException {
        return ResponseEntity.ok(toResources(modelAttrAssocService.getModelAttrAssocs(pModelId), pModelId));
    }

    /**
     * Link an {@link AttributeModel} to a {@link Model} with a {@link ModelAttrAssoc}.<br/>
     * This method is only available for {@link AttributeModel} in <b>default</b> {@link Fragment} (i.e. without name
     * space).
     *
     * @param pModelId {@link Model} identifier
     * @param pModelAttribute {@link ModelAttrAssoc} to link
     * @return the {@link ModelAttrAssoc} representing the link between the {@link Model} and the {@link AttributeModel}
     * @throws ModuleException if assignation cannot be done
     */
    @ResourceAccess(description = "Bind an attribute to a model")
    @RequestMapping(path = TYPE_MAPPING, method = RequestMethod.POST)
    public ResponseEntity<Resource<ModelAttrAssoc>> bindAttributeToModel(@PathVariable Long pModelId,
            @Valid @RequestBody ModelAttrAssoc pModelAttribute) throws ModuleException {
        return ResponseEntity
                .ok(toResource(modelAttrAssocService.bindAttributeToModel(pModelId, pModelAttribute), pModelId));
    }

    /**
     * Retrieve a {@link ModelAttrAssoc} linked to a {@link Model} id
     *
     * @param pModelId model identifier
     * @param pAttributeId attribute id
     * @return linked model attribute
     * @throws ModuleException if attribute cannot be retrieved
     */
    @ResourceAccess(description = "Get a model attribute")
    @RequestMapping(method = RequestMethod.GET, value = TYPE_MAPPING + "/{pAttributeId}")
    public ResponseEntity<Resource<ModelAttrAssoc>> getModelAttrAssoc(@PathVariable Long pModelId,
            @PathVariable Long pAttributeId) throws ModuleException {
        return ResponseEntity.ok(toResource(modelAttrAssocService.getModelAttrAssoc(pModelId, pAttributeId), pModelId));
    }

    /**
     * Allow to update calculation properties
     *
     * @param pModelId model identifier
     * @param pAttributeId attribute id
     * @param pModelAttribute attribute
     * @return update model attribute
     * @throws ModuleException if attribute cannot be updated
     */
    @ResourceAccess(description = "Update a model attribute")
    @RequestMapping(method = RequestMethod.PUT, value = TYPE_MAPPING + "/{pAttributeId}")
    public ResponseEntity<Resource<ModelAttrAssoc>> updateModelAttrAssoc(@PathVariable Long pModelId,
            @PathVariable Long pAttributeId, @Valid @RequestBody ModelAttrAssoc pModelAttribute)
            throws ModuleException {
        return ResponseEntity
                .ok(toResource(modelAttrAssocService.updateModelAttribute(pModelId, pAttributeId, pModelAttribute),
                               pModelId));
    }

    /**
     * Unlink a {@link ModelAttrAssoc} from a {@link Model}.<br/>
     * This method is only available for {@link AttributeModel} in <b>default</b> {@link Fragment} (i.e. without
     * namespace).
     *
     * @param pModelId model identifier
     * @param pAttributeId attribute id
     * @return nothing
     * @throws ModuleException if attribute cannot be removed
     */
    @ResourceAccess(description = "Unbind an attribute from a model")
    @RequestMapping(method = RequestMethod.DELETE, value = TYPE_MAPPING + "/{pAttributeId}")
    public ResponseEntity<Void> unbindAttributeFromModel(@PathVariable Long pModelId, @PathVariable Long pAttributeId)
            throws ModuleException {
        modelAttrAssocService.unbindAttributeFromModel(pModelId, pAttributeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Link all {@link AttributeModel} of a particular {@link Fragment} to a model creating {@link ModelAttrAssoc}.<br/>
     * This method is only available for {@link AttributeModel} in a <b>particular</b> {@link Fragment} (i.e. with name
     * space, not default one).
     *
     * @param pModelId model identifier
     * @param pFragment fragment
     * @return linked model attributes
     * @throws ModuleException if binding cannot be done
     */
    @ResourceAccess(description = "Bind fragment attributes to a model")
    @RequestMapping(method = RequestMethod.POST, value = TYPE_MAPPING + FRAGMENT_BIND_MAPPING)
    public ResponseEntity<List<Resource<ModelAttrAssoc>>> bindNSAttributeToModel(@PathVariable Long pModelId,
            @Valid @RequestBody Fragment pFragment) throws ModuleException {
        return ResponseEntity
                .ok(toResources(modelAttrAssocService.bindNSAttributeToModel(pModelId, pFragment), pModelId));
    }

    /**
     * Unlink all {@link AttributeModel} of a particular {@link Fragment} from a model deleting all associated
     * {@link ModelAttrAssoc}.<br/>
     * This method is only available for {@link AttributeModel} in a <b>particular</b> {@link Fragment} (i.e. with name
     * space, not default one).
     *
     * @param pModelId model identifier
     * @param pFragmentId fragment identifier
     * @return linked model attributes
     * @throws ModuleException if binding cannot be done
     */
    @ResourceAccess(description = "Unbind fragment attributes from a model")
    @RequestMapping(method = RequestMethod.DELETE, value = TYPE_MAPPING + FRAGMENT_UNBIND_MAPPING)
    public ResponseEntity<Void> unbindNSAttributeFromModel(@PathVariable Long pModelId, @PathVariable Long pFragmentId)
            throws ModuleException {
        modelAttrAssocService.unbindNSAttributeToModel(pModelId, pFragmentId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public Resource<ModelAttrAssoc> toResource(ModelAttrAssoc pElement, Object... pExtras) {
        final Resource<ModelAttrAssoc> resource = resourceService.toResource(pElement);

        final Long modelId = (Long) pExtras[0];

        resourceService.addLink(resource, this.getClass(), "getModelAttrAssoc", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, modelId),
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "updateModelAttrAssoc", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, modelId),
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(ModelAttrAssoc.class));
        resourceService.addLink(resource, this.getClass(), "unbindAttributeFromModel", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, modelId),
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "getModelAttrAssocs", LinkRels.LIST,
                                MethodParamFactory.build(Long.class, modelId));
        return resource;
    }
}
