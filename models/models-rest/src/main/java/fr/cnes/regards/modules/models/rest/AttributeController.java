/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.core.rest.AbstractController;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.exception.ModelException;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.RestrictionService;
import fr.cnes.regards.modules.models.signature.IAttributeSignature;

/**
 *
 * REST interface for managing model attributes
 *
 * @author msordi
 *
 */
@RestController
public class AttributeController extends AbstractController
        implements IAttributeSignature, IResourceController<AttributeModel> {

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

    public AttributeController(IAttributeModelService pAttributeService, IResourceService pResourceService,
            RestrictionService pRestrictionService) {
        this.attributeService = pAttributeService;
        this.resourceService = pResourceService;
        this.restrictionService = pRestrictionService;
    }

    @Override
    @ResourceAccess(description = "List all attributes")
    public ResponseEntity<List<Resource<AttributeModel>>> getAttributes(
            @RequestParam(value = "type", required = false) AttributeType pType) {
        final List<AttributeModel> attributes = attributeService.getAttributes(pType);
        return ResponseEntity.ok(toResources(attributes));
    }

    @Override
    @ResourceAccess(description = "Add an attribute")
    public ResponseEntity<Resource<AttributeModel>> addAttribute(@RequestBody AttributeModel pAttributeModel)
            throws ModelException {
        final AttributeModel attribute = attributeService.addAttribute(pAttributeModel);
        return ResponseEntity.ok(toResource(attribute));
    }

    @Override
    @ResourceAccess(description = "Get an attribute")
    public ResponseEntity<Resource<AttributeModel>> getAttribute(@PathVariable Long pAttributeId) {
        final AttributeModel attribute = attributeService.getAttribute(pAttributeId);
        return ResponseEntity.ok(toResource(attribute));
    }

    @Override
    @ResourceAccess(description = "Update an attribute")
    public ResponseEntity<Resource<AttributeModel>> updateAttribute(@RequestBody AttributeModel pAttributeModel)
            throws ModelException {
        final AttributeModel attribute = attributeService.updateAttribute(pAttributeModel);
        return ResponseEntity.ok(toResource(attribute));
    }

    @Override
    @ResourceAccess(description = "Delete an attribute")
    public ResponseEntity<Void> deleteAttribute(@PathVariable Long pAttributeId) {
        attributeService.deleteAttribute(pAttributeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @ResourceAccess(description = "List available restriction by attribute model type")
    public ResponseEntity<List<String>> getRestrictions(@RequestParam(value = "type") AttributeType pType) {
        return ResponseEntity.ok(restrictionService.getRestrictions(pType));
    }

    @Override
    @ResourceAccess(description = "List all attribute model types")
    public ResponseEntity<List<String>> getAttributeTypes() {
        final List<String> types = new ArrayList<>();
        for (AttributeType type : AttributeType.values()) {
            types.add(type.name());
        }
        return ResponseEntity.ok(types);
    }

    @Override
    public Resource<AttributeModel> toResource(AttributeModel pAttributeModel) {
        final Resource<AttributeModel> resource = resourceService.toResource(pAttributeModel);
        resourceService.addLink(resource, this.getClass(), "getAttribute", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pAttributeModel.getId()));
        resourceService.addLink(resource, this.getClass(), "updateAttribute", LinkRels.UPDATE,
                                MethodParamFactory.build(AttributeModel.class));
        resourceService.addLink(resource, this.getClass(), "deleteAttribute", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pAttributeModel.getId()));
        resourceService.addLink(resource, this.getClass(), "getAttributes", LinkRels.LIST,
                                MethodParamFactory.build(AttributeType.class));
        return resource;
    }

    // TODO : gérer l'import/export d'attributs
    // à partir d'une sélection d'attributs simples ou de namespaces :
    // - les attributs NO NAMESPACE sont exportés à l'unité
    // - tous les attributs d'un même NAMESPACE sont exportés
}
