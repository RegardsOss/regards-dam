/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.signature;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Attribute management API
 *
 * @author msordi
 *
 */
@RequestMapping("/models/attributes")
public interface IAttributeSignature {

    /**
     * Get all attributes or attributes for a particular type
     *
     * @param pType
     *            {@link AttributeType}
     * @return list of {@link AttributeModel}
     */
    @GetMapping
    public ResponseEntity<List<AttributeModel>> getAttributes(
            @RequestParam(value = "type", required = false) AttributeType pType);

    /**
     * Create a new attribute
     *
     * @param pAttributeModel
     *            element to create
     * @return the {@link AttributeModel}
     */
    @PostMapping
    public ResponseEntity<AttributeModel> addAttribute(@RequestBody AttributeModel pAttributeModel);

    /**
     * Get an attribute
     *
     * @param pAttributeId
     *            the attribute identifier
     * @return the {@link AttributeModel}
     */
    @GetMapping("/{pAttributeId}")
    public ResponseEntity<AttributeModel> getAttribute(@PathVariable Integer pAttributeId);

    /**
     * Update an attribute
     *
     * @param pAttributeId
     *            the attribute identifier
     * @return the {@link AttributeModel}
     */
    @PutMapping("/{pAttributeId}")
    public ResponseEntity<AttributeModel> updateAttribute(@PathVariable Integer pAttributeId);

    /**
     * Delete an attribute
     *
     * @param pAttributeId
     *            the attribute identifier
     * @return TODO
     */
    @DeleteMapping("/{pAttributeId}")
    public ResponseEntity<?> deleteAttribute(@PathVariable Integer pAttributeId);
}
