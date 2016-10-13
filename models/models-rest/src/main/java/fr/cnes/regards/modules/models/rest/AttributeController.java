/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.security.utils.endpoint.annotation.ResourceAccess;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeService;
import fr.cnes.regards.modules.models.signature.IAttributeSignature;

/**
 *
 * REST interface for managing model attributes
 *
 * @author msordi
 *
 */
@RestController
public class AttributeController implements IAttributeSignature {

    /**
     * Attribute service
     */
    @Autowired
    private IAttributeService attributeService;

    @Override
    @ResourceAccess(description = "List all attributes")
    public ResponseEntity<List<AttributeModel>> getAttributes(AttributeType pType) {
        final List<AttributeModel> attributes = attributeService.getAttributes(pType);
        return ResponseEntity.ok(attributes);
    }

    @Override
    @ResourceAccess(description = "Add an attribute")
    public ResponseEntity<AttributeModel> addAttribute(@RequestBody AttributeModel pAttributeModel) {
        // TODO
        return null;
    }

    @Override
    @ResourceAccess(description = "Get an attribute")
    public ResponseEntity<AttributeModel> getAttribute(@PathVariable Integer pAttributeId) {
        // TODO
        return null;
    }

    @Override
    @ResourceAccess(description = "Update an attribute")
    public ResponseEntity<AttributeModel> updateAttribute(@PathVariable Integer pAttributeId) {
        // TODO
        return null;
    }

    @Override
    @ResourceAccess(description = "Delete an attribute")
    public ResponseEntity<?> deleteAttribute(@PathVariable Integer pAttributeId) {
        // TODO
        return null;
    }

    public IAttributeService getAttributeService() {
        return attributeService;
    }

    public void setAttributeService(IAttributeService pAttributeService) {
        attributeService = pAttributeService;
    }

    // TODO : gérer l'import/export d'attributs
    // à partir d'une sélection d'attributs simples ou de namespaces :
    // - les attributs NO NAMESPACE sont exportés à l'unité
    // - tous les attributs d'un même NAMESPACE sont exportés
}
