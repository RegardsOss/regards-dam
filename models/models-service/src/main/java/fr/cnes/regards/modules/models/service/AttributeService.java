/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
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

    /**
     * {@link AttributeModel} repository
     */
    @Autowired
    private IAttributeModelRepository repository;

    @Override
    public List<AttributeModel> getAttributes(AttributeType pType) {
        return IterableUtils.toList(repository.findAll());
    }

    @Override
    public AttributeModel addAttribute(AttributeModel pAttributeModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AttributeModel getAttribute(Long pAttributeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AttributeModel updateAttribute(AttributeModel pAttributeModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteAttribute(Long pAttributeId) {
        // TODO Auto-generated method stub

    }

}
