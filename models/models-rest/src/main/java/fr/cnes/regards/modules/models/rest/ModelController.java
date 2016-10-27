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

import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.core.annotation.ModuleInfo;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.ModelType;

/**
 *
 * REST interface for managing data model
 *
 * @author msordi
 *
 */
@RestController
@ModuleInfo(name = "models", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS SI", documentation = "http://test")
@RequestMapping("/models")
public class ModelController {

    /**
     *
     * Get the project model list
     *
     * @param pType
     *            model type
     * @return model list filtered by type if not null
     */
    @ResourceAccess(description = "List all models")
    @GetMapping
    public ResponseEntity<?> getModels(@RequestParam(value = "type", required = false) final ModelType pType) {
        // TODO
        return ResponseEntity.ok(null);
    }

    @ResourceAccess(description = "Add a model")
    @PostMapping
    public ResponseEntity<?> addModel(@RequestBody final Model pModel) {
        // TODO
        return null;
    }

    // TODO gérer l'import de modèle

    @ResourceAccess(description = "Get a model")
    @GetMapping("/{pModelId}")
    public ResponseEntity<?> getModel(@PathVariable final Integer pModelId) {
        // TODO
        return null;
    }

    // TODO gérer l'export de modèle

    @ResourceAccess(description = "Delete a model")
    @DeleteMapping("/{pModelId}")
    public ResponseEntity<?> deleteModel(@PathVariable final Integer pModelId) {
        // TODO
        return null;
    }

    @ResourceAccess(description = "Get all model attributes")
    @GetMapping("/{pModelId}/attributes")
    public ResponseEntity<?> getModelAttributes(@PathVariable final Integer pModelId) {
        // TODO : get all ModelAttributes
        return null;
    }

    @ResourceAccess(description = "Assign an attribute to a model")
    @PostMapping("/{pModelId}/attributes")
    public ResponseEntity<?> assignAttributeToModel(@PathVariable final Integer pModelId,
            @RequestBody final ModelAttribute pModelAttribute) {
        // TODO : associate attribute to a model through ModelAttribute (with calculation properties)
        // Only available for NO NAMESPACE ATTRIBUTES
        return null;
    }

    @ResourceAccess(description = "Get a single model attribute")
    @GetMapping("/{pModelId}/attributes/{pAttributeId}")
    public ResponseEntity<?> getModelAttribute(@PathVariable final Integer pModelId,
            @PathVariable final Integer pAttributeId) {
        // TODO : get a single ModelAttribute
        return null;
    }

    @ResourceAccess(description = "Update a model attribute")
    @PutMapping("/{pModelId}/attributes/{pAttributeId}")
    public ResponseEntity<?> updateModelAttribute(@PathVariable final Integer pModelId,
            @PathVariable final Integer pAttributeId) {
        // TODO : update ModelAttribute (change calculation properties)
        return null;
    }

    @ResourceAccess(description = "Dissociate an attribute from a model")
    @DeleteMapping("/{pModelId}/attributes/{pAttributeId}")
    public ResponseEntity<?> deleteModelAttribute(@PathVariable final Integer pModelId,
            @PathVariable final Integer pAttributeId) {
        // TODO : dissociate attribute from the model / just delete the link
        // Only available for NO NAMESPACE ATTRIBUTES
        return null;
    }

    // Attributes grouped by namespace

    @ResourceAccess(description = "Assign all attributes of a namespace to a model")
    @PostMapping("/{pModelId}/attributes/namespaces/{pNamespace}")
    public ResponseEntity<Void> assignNSAttributesToModel(@PathVariable final Integer pModelId,
            @PathVariable final Integer pNamespace, @RequestBody final List<ModelAttribute> pModelAttributes) {
        // TODO : associate attributes to a model through ModelAttribute (with calculation properties)
        // Only available for NAMESPACE ATTRIBUTES : all attributes of a the namespace must be specified
        return null;
    }

    @ResourceAccess(description = "Dissociate all attributes of a namespace")
    @DeleteMapping("/{pModelId}/attributes/namespaces/{pNamespace}")
    public ResponseEntity<?> deleteNSModelAttributes(@PathVariable final Integer pModelId,
            @PathVariable final Integer pNamespace, @PathVariable final Integer pAttributeId) {
        // TODO : dissociate attribute from the model / just delete the link
        // Only available for NAMESPACE ATTRIBUTES
        // Dissociate all attributes of the given namespace
        return null;
    }
}
