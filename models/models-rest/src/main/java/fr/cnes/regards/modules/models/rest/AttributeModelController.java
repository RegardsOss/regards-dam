/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.modules.entities.domain.StaticProperties;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.RestrictionService;

/**
 *
 * REST controller for managing {@link AttributeModel}
 *
 * @author msordi
 *
 */
@RestController
@RequestMapping(AttributeModelController.TYPE_MAPPING)
public class AttributeModelController implements IResourceController<AttributeModel> {

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/models/attributes";

    /**
     * Entity type mapping
     */
    public static final String ENTITY_TYPE_MAPPING = "/modeltype/{pModelType}";

    /**
     * Request parameter : attribute type
     */
    public static final String PARAM_TYPE = "type";

    /**
     * Request parameter : fragment name
     */
    public static final String PARAM_FRAGMENT_NAME = "fragmentName";

    /**
     * Attribute service
     */
    private final IAttributeModelService attributeService;

    /**
     * Model attribute association service
     */
    private final IModelAttrAssocService modelAttrAssocService;

    /**
     * Restriction service
     */
    private final RestrictionService restrictionService;

    /**
     * Resource service
     */
    private final IResourceService resourceService;

    /**
     * Constructor
     *
     * @param pAttributeService
     *            Attribute service
     * @param pResourceService
     *            Resource service
     * @param pRestrictionService
     *            Restriction service
     */
    public AttributeModelController(final IAttributeModelService pAttributeService,
            final IResourceService pResourceService, IModelAttrAssocService pModelAttrAssocService,
            final RestrictionService pRestrictionService) {
        this.attributeService = pAttributeService;
        this.resourceService = pResourceService;
        this.restrictionService = pRestrictionService;
        this.modelAttrAssocService = pModelAttrAssocService;
    }

    /**
     * Retrieve all attributes. The request can be filtered by {@link AttributeType}
     *
     * @param pType
     *            filter by type
     * @param pFragmentName
     *            filter by fragment
     * @return list of {@link AttributeModel}
     */
    @ResourceAccess(description = "List all attributes", role = DefaultRole.PUBLIC)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<AttributeModel>>> getAttributes(
            @RequestParam(value = PARAM_TYPE, required = false) final AttributeType pType,
            @RequestParam(value = PARAM_FRAGMENT_NAME, required = false) final String pFragmentName) {
        final List<AttributeModel> attributes = attributeService.getAttributes(pType, pFragmentName);
        // Build JSON path
        attributes.forEach(attModel -> attModel.buildJsonPath(StaticProperties.PROPERTIES));
        return ResponseEntity.ok(toResources(attributes));
    }

    /**
     * Retrieve all {@link Model}. The request can be filtered by {@link EntityType}.
     *
     * @param pType
     *            filter
     * @return a list of {@link Model}
     */
    @ResourceAccess(description = "List all models", role = DefaultRole.PUBLIC)
    @RequestMapping(method = RequestMethod.GET, value = ENTITY_TYPE_MAPPING)
    public ResponseEntity<List<Resource<AttributeModel>>> getModelsAttributes(
            @PathVariable final EntityType pModelType) {
        Collection<ModelAttrAssoc> assocs = modelAttrAssocService.getModelAttrAssocsFor(pModelType);
        List<AttributeModel> attributes = assocs.stream().map(attrAssoc -> attrAssoc.getAttribute())
                .collect(Collectors.toList());
        attributes.forEach(attModel -> attModel.buildJsonPath(StaticProperties.PROPERTIES));
        return ResponseEntity.ok(toResources(attributes));

    }

    /**
     * Add a new attribute.
     *
     * @param pAttributeModel
     *            the attribute to create
     * @return the created {@link AttributeModel}
     * @throws ModuleException
     *             if error occurs!
     */
    @ResourceAccess(description = "Add an attribute")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<AttributeModel>> addAttribute(
            @Valid @RequestBody final AttributeModel pAttributeModel) throws ModuleException {
        return ResponseEntity.ok(toResource(attributeService.addAttribute(pAttributeModel, false)));
    }

    /**
     * Get an attribute
     *
     * @param pAttributeId
     *            attribute identifier
     * @return the retrieved {@link AttributeModel}
     * @throws ModuleException
     *             if error occurs!
     */
    @ResourceAccess(description = "Get an attribute", role = DefaultRole.PUBLIC)
    @RequestMapping(method = RequestMethod.GET, value = "/{pAttributeId}")
    public ResponseEntity<Resource<AttributeModel>> getAttribute(@PathVariable final Long pAttributeId)
            throws ModuleException {
        AttributeModel attribute = attributeService.getAttribute(pAttributeId);

        attribute.buildJsonPath(StaticProperties.PROPERTIES);
        return ResponseEntity.ok(toResource(attribute));
    }

    /**
     * Update an attribute
     *
     * @param pAttributeId
     *            attribute identifier
     * @param pAttributeModel
     *            attribute
     * @return the updated {@link AttributeModel}
     * @throws ModuleException
     *             if error occurs!
     */
    @ResourceAccess(description = "Update an attribute")
    @RequestMapping(method = RequestMethod.PUT, value = "/{pAttributeId}")
    public ResponseEntity<Resource<AttributeModel>> updateAttribute(@PathVariable final Long pAttributeId,
            @Valid @RequestBody final AttributeModel pAttributeModel) throws ModuleException {
        return ResponseEntity.ok(toResource(attributeService.updateAttribute(pAttributeId, pAttributeModel)));
    }

    /**
     * Delete an attribute
     *
     * @param pAttributeId
     *            attribute identifier
     * @return nothing
     */
    @ResourceAccess(description = "Delete an attribute")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pAttributeId}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable final Long pAttributeId) {
        attributeService.deleteAttribute(pAttributeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all restriction by {@link AttributeType}
     *
     * @param pType
     *            filter on attribute type
     * @return list of restriction name
     */
    @ResourceAccess(description = "List available restriction by attribute model type")
    @RequestMapping(method = RequestMethod.GET, value = "/restrictions")
    public ResponseEntity<List<String>> getRestrictions(@RequestParam(value = "type") final AttributeType pType) {
        return ResponseEntity.ok(restrictionService.getRestrictions(pType));
    }

    /**
     * Get all attribute types
     *
     * @return list of type names
     */
    @ResourceAccess(description = "List all attribute model types")
    @RequestMapping(method = RequestMethod.GET, value = "/types")
    public ResponseEntity<List<String>> getAttributeTypes() {
        final List<String> types = new ArrayList<>();
        for (final AttributeType type : AttributeType.values()) {
            types.add(type.name());
        }
        return ResponseEntity.ok(types);
    }

    @Override
    public Resource<AttributeModel> toResource(final AttributeModel pAttributeModel, final Object... pExtras) {
        final Resource<AttributeModel> resource = resourceService.toResource(pAttributeModel);
        resourceService.addLink(resource, this.getClass(), "getAttribute", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pAttributeModel.getId()));
        resourceService.addLink(resource, this.getClass(), "updateAttribute", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, pAttributeModel.getId()),
                                MethodParamFactory.build(AttributeModel.class));
        resourceService.addLink(resource, this.getClass(), "deleteAttribute", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pAttributeModel.getId()));
        resourceService.addLink(resource, this.getClass(), "getAttributes", LinkRels.LIST,
                                MethodParamFactory.build(AttributeType.class), MethodParamFactory.build(String.class));
        return resource;
    }
}
