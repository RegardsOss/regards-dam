/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.utils.IterableUtils;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
import fr.cnes.regards.modules.models.dao.IRestrictionRepository;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IRestriction;

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
    private final IAttributeModelRepository attModelRepository;

    /**
     * {@link IRestriction} repository
     */
    private final IRestrictionRepository restrictionRepository;

    /**
     * {@link Fragment} repository
     */
    private final IFragmentRepository fragmentRepository;

    public AttributeModelService(IAttributeModelRepository pAttModelRepository,
            IRestrictionRepository pRestrictionRepository, IFragmentRepository pFragmentRepository) {
        this.attModelRepository = pAttModelRepository;
        this.restrictionRepository = pRestrictionRepository;
        this.fragmentRepository = pFragmentRepository;
    }

    @Override
    public List<AttributeModel> getAttributes(AttributeType pType) {
        final Iterable<AttributeModel> attModels;
        if (pType != null) {
            attModels = attModelRepository.findByType(pType);
        } else {
            attModels = attModelRepository.findAll();
        }
        return IterableUtils.toList(attModels);
    }

    @Override
    public AttributeModel addAttribute(AttributeModel pAttributeModel) {
        return saveTransientEntities(pAttributeModel);
    }

    @Override
    public AttributeModel getAttribute(Long pAttributeId) {
        return attModelRepository.findOne(pAttributeId);
    }

    @Override
    public AttributeModel updateAttribute(AttributeModel pAttributeModel) {
        return saveTransientEntities(pAttributeModel);
    }

    @Override
    public void deleteAttribute(Long pAttributeId) {
        attModelRepository.delete(pAttributeId);
    }

    private AttributeModel saveTransientEntities(AttributeModel pAttributeModel) {
        // Manage restriction
        if (pAttributeModel.getRestriction() != null) {
            restrictionRepository.save(pAttributeModel.getRestriction());
        }
        // Manage fragment
        if (pAttributeModel.getFragment() != null) {
            fragmentRepository.save(pAttributeModel.getFragment());
        }
        // Finally, save attribute model
        return attModelRepository.save(pAttributeModel);
    }

}
