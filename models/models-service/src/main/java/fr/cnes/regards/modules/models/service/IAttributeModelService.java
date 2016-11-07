/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.exception.AlreadyExistsException;

/**
 * Attribute management service
 *
 * @author msordi
 *
 */
public interface IAttributeModelService {

    List<AttributeModel> getAttributes(AttributeType pType);

    AttributeModel addAttribute(AttributeModel pAttributeModel) throws AlreadyExistsException;

    AttributeModel getAttribute(Long pAttributeId);

    AttributeModel updateAttribute(AttributeModel pAttributeModel);

    void deleteAttribute(Long pAttributeId);
}
