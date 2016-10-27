/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.signature;

import java.util.List;

import javax.validation.Valid;

import org.springframework.hateoas.Resource;
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

    @GetMapping
    ResponseEntity<List<Resource<AttributeModel>>> getAttributes(
            @RequestParam(value = "type", required = false) AttributeType pType);

    @PostMapping
    ResponseEntity<Resource<AttributeModel>> addAttribute(@Valid @RequestBody AttributeModel pAttributeModel);

    @GetMapping("/{pAttributeId}")
    ResponseEntity<Resource<AttributeModel>> getAttribute(@PathVariable Long pAttributeId);

    @PutMapping("/{pAttributeId}")
    ResponseEntity<Resource<AttributeModel>> updateAttribute(@Valid @RequestBody AttributeModel pAttributeModel);

    @DeleteMapping("/{pAttributeId}")
    ResponseEntity<Void> deleteAttribute(@PathVariable Long pAttributeId);
}
