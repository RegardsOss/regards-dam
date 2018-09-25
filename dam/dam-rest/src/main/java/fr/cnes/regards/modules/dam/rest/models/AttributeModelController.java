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

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.modules.dam.domain.entities.StaticProperties;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;
import fr.cnes.regards.modules.dam.service.models.IModelAttrAssocService;
import fr.cnes.regards.modules.dam.service.models.RestrictionService;

/**
 * REST controller for managing {@link AttributeModel}
 * @author msordi
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

    public static final String ATTRIBUTE_MAPPING = "/{pAttributeId}";

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
     * @param attributeModelService Attribute service
     * @param pResourceService Resource service
     * @param restrictionService Restriction service
     */
    public AttributeModelController(final IAttributeModelService attributeModelService,
            final IResourceService pResourceService, IModelAttrAssocService modelAttrAssocService,
            final RestrictionService restrictionService) {
        this.attributeService = attributeModelService;
        this.resourceService = pResourceService;
        this.restrictionService = restrictionService;
        this.modelAttrAssocService = modelAttrAssocService;
    }

    /**
     * Retrieve all attributes. The request can be filtered by {@link AttributeType}
     * @param type filter by type
     * @param fragmentName filter by fragment
     * @return list of {@link AttributeModel}
     */
    @ResourceAccess(description = "List all attributes", role = DefaultRole.PUBLIC)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<AttributeModel>>> getAttributes(
            @RequestParam(value = PARAM_TYPE, required = false) AttributeType type,
            @RequestParam(value = PARAM_FRAGMENT_NAME, required = false) String fragmentName,
            @RequestParam(name = "modelIds", required = false) Set<Long> modelIds) {
        List<AttributeModel> attributes = attributeService.getAttributes(type, fragmentName, modelIds);
        // Build JSON path
        attributes.forEach(attModel -> attModel.buildJsonPath(StaticProperties.FEATURE_PROPERTIES));
        return ResponseEntity.ok(toResources(attributes));
    }

    /**
     * Retrieve all {@link Model}. The request can be filtered by {@link EntityType}.
     * @param modelType filter
     * @return a list of {@link Model}
     */
    @ResourceAccess(description = "List all models", role = DefaultRole.PUBLIC)
    @RequestMapping(method = RequestMethod.GET, value = ENTITY_TYPE_MAPPING)
    public ResponseEntity<List<Resource<AttributeModel>>> getModelsAttributes(@PathVariable EntityType modelType) {
        Collection<ModelAttrAssoc> assocs = modelAttrAssocService.getModelAttrAssocsFor(modelType);
        List<AttributeModel> attributes = assocs.stream().map(attrAssoc -> attrAssoc.getAttribute())
                .collect(Collectors.toList());
        attributes.forEach(attModel -> attModel.buildJsonPath(StaticProperties.FEATURE_PROPERTIES));
        return ResponseEntity.ok(toResources(attributes));

    }

    /**
     * Add a new attribute.
     * @param attributeModel the attribute to create
     * @return the created {@link AttributeModel}
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Add an attribute")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<AttributeModel>> addAttribute(
            @Valid @RequestBody final AttributeModel attributeModel) throws ModuleException {
        return ResponseEntity.ok(toResource(attributeService.addAttribute(attributeModel, false)));
    }

    /**
     * Get an attribute
     * @param id attribute identifier
     * @return the retrieved {@link AttributeModel}
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Get an attribute", role = DefaultRole.PUBLIC)
    @RequestMapping(method = RequestMethod.GET, value = ATTRIBUTE_MAPPING)
    public ResponseEntity<Resource<AttributeModel>> getAttribute(@PathVariable final Long id) throws ModuleException {
        AttributeModel attribute = attributeService.getAttribute(id);

        attribute.buildJsonPath(StaticProperties.FEATURE_PROPERTIES);
        return ResponseEntity.ok(toResource(attribute));
    }

    /**
     * Update an attribute
     * @param id attribute identifier
     * @param attributeModel attribute
     * @return the updated {@link AttributeModel}
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Update an attribute")
    @RequestMapping(method = RequestMethod.PUT, value = ATTRIBUTE_MAPPING)
    public ResponseEntity<Resource<AttributeModel>> updateAttribute(@PathVariable final Long id,
            @Valid @RequestBody final AttributeModel attributeModel) throws ModuleException {
        return ResponseEntity.ok(toResource(attributeService.updateAttribute(id, attributeModel)));
    }

    /**
     * Delete an attribute
     * @param id attribute identifier
     * @return nothing
     */
    @ResourceAccess(description = "Delete an attribute")
    @RequestMapping(method = RequestMethod.DELETE, value = ATTRIBUTE_MAPPING)
    public ResponseEntity<Void> deleteAttribute(@PathVariable final Long id) throws ModuleException {
        attributeService.deleteAttribute(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all restriction by {@link AttributeType}
     * @param type filter on attribute type
     * @return list of restriction name
     */
    @ResourceAccess(description = "List available restriction by attribute model type")
    @RequestMapping(method = RequestMethod.GET, value = "/restrictions")
    public ResponseEntity<List<String>> getRestrictions(@RequestParam(value = "type") final AttributeType type) {
        return ResponseEntity.ok(restrictionService.getRestrictions(type));
    }

    /**
     * Get all attribute types
     * @return list of type names
     */
    @ResourceAccess(description = "List all attribute model types")
    @RequestMapping(method = RequestMethod.GET, value = "/types")
    public ResponseEntity<List<String>> getAttributeTypes() {
        List<String> types = new ArrayList<>();
        for (AttributeType type : AttributeType.values()) {
            types.add(type.name());
        }
        return ResponseEntity.ok(types);
    }

    @Override
    public Resource<AttributeModel> toResource(final AttributeModel attributeModel, final Object... pExtras) {
        Resource<AttributeModel> resource = resourceService.toResource(attributeModel);
        resourceService.addLink(resource, this.getClass(), "getAttribute", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, attributeModel.getId()));
        resourceService.addLink(resource, this.getClass(), "updateAttribute", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, attributeModel.getId()),
                                MethodParamFactory.build(AttributeModel.class));
        if (attributeService.isDeletable(attributeModel.getId())) {
            resourceService.addLink(resource, this.getClass(), "deleteAttribute", LinkRels.DELETE,
                                    MethodParamFactory.build(Long.class, attributeModel.getId()));
        }
        resourceService.addLink(resource, this.getClass(), "getAttributes", LinkRels.LIST,
                                MethodParamFactory.build(AttributeType.class), MethodParamFactory.build(String.class),
                                MethodParamFactory.build(Set.class));
        return resource;
    }
}
