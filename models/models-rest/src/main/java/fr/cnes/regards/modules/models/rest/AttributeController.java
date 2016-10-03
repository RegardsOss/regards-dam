/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

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
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.modules.models.domain.AttributeModel;
import fr.cnes.regards.modules.models.domain.AttributeType;
import fr.cnes.regards.security.utils.endpoint.annotation.ResourceAccess;

/**
 *
 * REST interface for managing model attributes
 *
 * @author msordi
 *
 */
@RestController
@RequestMapping("/models/attributes")
public class AttributeController {

    /**
     *
     * Get the project model list
     *
     * @param pType
     *            model type
     * @return model list filtered by type if not null
     */
    @ResourceAccess(description = "List all attributes")
    @GetMapping
    public ResponseEntity<List<AttributeModel>> getAttributes(
            @RequestParam(value = "type", required = false) AttributeType pType) {
        // TODO : get all AttributeModel
        return ResponseEntity.ok(null);
    }

    @ResourceAccess(description = "Add an attribute")
    @PostMapping
    public ResponseEntity<AttributeModel> addAttribute(@RequestBody AttributeModel pAttributeModel) {
        // TODO
        return null;
    }

    @ResourceAccess(description = "Get an attribute")
    @GetMapping("/{pAttributeId}")
    public ResponseEntity<AttributeModel> getAttribute(@PathVariable Integer pAttributeId) {
        // TODO
        return null;
    }

    @ResourceAccess(description = "Update an attribute")
    @PutMapping("/{pAttributeId}")
    public ResponseEntity<AttributeModel> updateAttribute(@PathVariable Integer pAttributeId) {
        // TODO
        return null;
    }

    @ResourceAccess(description = "Delete an attribute")
    @DeleteMapping("/{pAttributeId}")
    public ResponseEntity<?> deleteAttribute(@PathVariable Integer pAttributeId) {
        // TODO
        return null;
    }

    // TODO : gérer l'import/export d'attributs
    // à partir d'une sélection d'attributs simples ou de namespaces :
    // - les attributs NO NAMESPACE sont exportés à l'unité
    // - tous les attributs d'un même NAMESPACE sont exportés
}
