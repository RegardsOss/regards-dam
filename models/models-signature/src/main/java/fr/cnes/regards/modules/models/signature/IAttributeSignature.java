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

    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<List<Resource<AttributeModel>>> getAttributes(
            @RequestParam(value = "type", required = false) AttributeType pType);

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Resource<AttributeModel>> addAttribute(@Valid @RequestBody AttributeModel pAttributeModel);

    @RequestMapping(method = RequestMethod.GET, value = "/{pAttributeId}")
    ResponseEntity<Resource<AttributeModel>> getAttribute(@PathVariable Long pAttributeId);

    @RequestMapping(method = RequestMethod.PUT)
    ResponseEntity<Resource<AttributeModel>> updateAttribute(@Valid @RequestBody AttributeModel pAttributeModel);

    @RequestMapping(method = RequestMethod.DELETE, value = "/{pAttributeId}")
    ResponseEntity<Void> deleteAttribute(@PathVariable Long pAttributeId);
}
