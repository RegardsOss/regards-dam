/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 *
 * Manage global attribute life cycle
 *
 * @author msordi
 *
 */
@Service
public class AttributeService implements IAttributeService {

    @Override
    public List<AttributeModel> getAttributes(AttributeType pType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AttributeModel addAttribute(AttributeModel pAttributeModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AttributeModel getAttribute(Integer pAttributeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AttributeModel updateAttribute(Integer pAttributeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteAttribute(Integer pAttributeId) {
        // TODO Auto-generated method stub

    }

}
