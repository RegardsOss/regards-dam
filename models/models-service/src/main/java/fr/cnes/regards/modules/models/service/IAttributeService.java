/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Attribute management service
 *
 * @author msordi
 *
 */
public interface IAttributeService {

    List<AttributeModel> getAttributes(AttributeType pType);

    AttributeModel addAttribute(AttributeModel pAttributeModel);

    AttributeModel getAttribute(@PathVariable Integer pAttributeId);

    AttributeModel updateAttribute(@PathVariable Integer pAttributeId);

    void deleteAttribute(@PathVariable Integer pAttributeId);
}
