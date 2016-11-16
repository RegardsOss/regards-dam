/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.signature;

import java.util.List;

import javax.validation.Valid;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * {@link AttributeModel} management API
 *
 * @author msordi
 *
 */
@RequestMapping("/models/attributes")
public interface IAttributeModelSignature {

    /**
     * Retrieve all attributes. The request can be filtered by {@link AttributeType}
     *
     * @param pType
     *            filter
     * @return list of {@link AttributeModel}
     */
    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<List<Resource<AttributeModel>>> getAttributes(
            @RequestParam(value = "type", required = false) AttributeType pType);

    /**
     * Add a new attribute.
     *
     * @param pAttributeModel
     *            the attribute to create
     * @return the created {@link AttributeModel}
     * @throws ModuleException
     *             if error occurs!
     */
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Resource<AttributeModel>> addAttribute(@Valid @RequestBody AttributeModel pAttributeModel)
            throws ModuleException;

    /**
     * Get an attribute
     *
     * @param pAttributeId
     *            attribute identifier
     * @return the retrieved {@link AttributeModel}
     * @throws ModuleException
     *             if error occurs!
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{pAttributeId}")
    ResponseEntity<Resource<AttributeModel>> getAttribute(@PathVariable Long pAttributeId) throws ModuleException;

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
    @RequestMapping(method = RequestMethod.PUT, value = "/{pAttributeId}")
    ResponseEntity<Resource<AttributeModel>> updateAttribute(@PathVariable Long pAttributeId,
            @Valid @RequestBody AttributeModel pAttributeModel) throws ModuleException;

    /**
     * Delete an attribute
     *
     * @param pAttributeId
     *            attribute identifier
     * @return nothing
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pAttributeId}")
    ResponseEntity<Void> deleteAttribute(@PathVariable Long pAttributeId);

    /**
     * Get all restriction by {@link AttributeType}
     *
     * @param pType
     *            filter on attribute type
     * @return list of restriction name
     */
    @RequestMapping(method = RequestMethod.GET, value = "/restrictions")
    ResponseEntity<List<String>> getRestrictions(@RequestParam(value = "type") AttributeType pType);

    /**
     * Get all attribute types
     *
     * @return list of type names
     */
    @RequestMapping(method = RequestMethod.GET, value = "/types")
    ResponseEntity<List<String>> getAttributeTypes();
}
