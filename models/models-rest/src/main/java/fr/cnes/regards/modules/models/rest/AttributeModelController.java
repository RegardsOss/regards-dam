/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.ArrayList;
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

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
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
     * Request parameter : attribute type
     */
    public static final String PARAM_TYPE = "type";

    /**
     * Request parameter : fragement name
     */
    public static final String PARAM_FRAGMENT_NAME = "fragmentName";

    /**
     * Attribute service
     */
    private final IAttributeModelService attributeService;

    /**
     * Restriction service
     */
    private final RestrictionService restrictionService;

    /**
     * Resource service
     */
    private final IResourceService resourceService;

    public AttributeModelController(IAttributeModelService pAttributeService, IResourceService pResourceService,
            RestrictionService pRestrictionService) {
        this.attributeService = pAttributeService;
        this.resourceService = pResourceService;
        this.restrictionService = pRestrictionService;
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
    @ResourceAccess(description = "List all attributes")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<AttributeModel>>> getAttributes(
            @RequestParam(value = PARAM_TYPE, required = false) AttributeType pType,
            @RequestParam(value = PARAM_FRAGMENT_NAME, required = false) String pFragmentName) {
        final List<AttributeModel> attributes = attributeService.getAttributes(pType, pFragmentName);
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
    public ResponseEntity<Resource<AttributeModel>> addAttribute(@Valid @RequestBody AttributeModel pAttributeModel)
            throws ModuleException {
        return ResponseEntity.ok(toResource(attributeService.addAttribute(pAttributeModel)));
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
    @ResourceAccess(description = "Get an attribute")
    @RequestMapping(method = RequestMethod.GET, value = "/{pAttributeId}")
    public ResponseEntity<Resource<AttributeModel>> getAttribute(@PathVariable Long pAttributeId)
            throws ModuleException {
        return ResponseEntity.ok(toResource(attributeService.getAttribute(pAttributeId)));
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
    public ResponseEntity<Resource<AttributeModel>> updateAttribute(@PathVariable Long pAttributeId,
            @Valid @RequestBody AttributeModel pAttributeModel) throws ModuleException {
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
    public ResponseEntity<Void> deleteAttribute(@PathVariable Long pAttributeId) {
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
    public ResponseEntity<List<String>> getRestrictions(@RequestParam(value = "type") AttributeType pType) {
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
        for (AttributeType type : AttributeType.values()) {
            types.add(type.name());
        }
        return ResponseEntity.ok(types);
    }

    @Override
    public Resource<AttributeModel> toResource(AttributeModel pAttributeModel, Object... pExtras) {
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
