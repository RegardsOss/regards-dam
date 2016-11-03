/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.utils.IterableUtils;
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
public class AttributeModelService implements IAttributeModelService {

    /**
     * {@link AttributeModel} repository
     */
    @Autowired
    private IAttributeModelRepository attModleRepository;

    @Override
    public List<AttributeModel> getAttributes(AttributeType pType) {
        final Iterable<AttributeModel> attModels;
        if (pType != null) {
            attModels = attModleRepository.findByType(pType);
        } else {
            attModels = attModleRepository.findAll();
        }
        return IterableUtils.toList(attModels);
    }

    @Override
    public AttributeModel addAttribute(AttributeModel pAttributeModel) {
        return attModleRepository.save(pAttributeModel);
    }

    @Override
    public AttributeModel getAttribute(Long pAttributeId) {
        return attModleRepository.findOne(pAttributeId);
    }

    @Override
    public AttributeModel updateAttribute(AttributeModel pAttributeModel) {
        return attModleRepository.save(pAttributeModel);
    }

    @Override
    public void deleteAttribute(Long pAttributeId) {
        attModleRepository.delete(pAttributeId);
    }

}
