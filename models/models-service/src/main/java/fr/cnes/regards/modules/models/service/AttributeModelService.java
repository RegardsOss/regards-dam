/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.utils.IterableUtils;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotIdentifiableException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
import fr.cnes.regards.modules.models.dao.IRestrictionRepository;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
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
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AttributeModelService.class);

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
    public List<AttributeModel> getAttributes(AttributeType pType, String pFragmentName) {
        final Iterable<AttributeModel> attModels;
        if (pFragmentName != null) {
            if (pType != null) {
                attModels = attModelRepository.findByTypeAndFragmentName(pType, pFragmentName);
            } else {
                attModels = attModelRepository.findByFragmentName(pFragmentName);
            }
        } else {
            if (pType != null) {
                attModels = attModelRepository.findByType(pType);
            } else {
                attModels = attModelRepository.findAll();
            }
        }
        return IterableUtils.toList(attModels);
    }

    @Override
    public AttributeModel addAttribute(AttributeModel pAttributeModel) throws ModuleException {
        manageRestriction(pAttributeModel);
        // final Fragment fragment =
        manageFragment(pAttributeModel);
        manageAttributeModel(pAttributeModel);
        // if (!fragment.isDefaultFragment()) {
        // // TODO modelAttributeService.updateNSBind(fragment.getId());
        // // Attention au référence cyclique entre service
        // }
        return pAttributeModel;
    }

    @Override
    public AttributeModel getAttribute(Long pAttributeId) throws ModuleException {
        if (!attModelRepository.exists(pAttributeId)) {
            throw new EntityNotFoundException(pAttributeId, AttributeModel.class);
        }
        return attModelRepository.findOne(pAttributeId);
    }

    @Override
    public AttributeModel updateAttribute(Long pAttributeId, AttributeModel pAttributeModel) throws ModuleException {
        if (!pAttributeModel.isIdentifiable()) {
            throw new EntityNotIdentifiableException(
                    String.format("Unknown identifier for attribute model \"%s\"", pAttributeModel.getName()));
        }
        if (!pAttributeId.equals(pAttributeModel.getId())) {
            throw new EntityInconsistentIdentifierException(pAttributeId, pAttributeModel.getId(),
                    pAttributeModel.getClass());
        }
        if (!attModelRepository.exists(pAttributeId)) {
            throw new EntityNotFoundException(pAttributeModel.getId(), AttributeModel.class);
        }
        return attModelRepository.save(pAttributeModel);
    }

    @Override
    public void deleteAttribute(Long pAttributeId) {
        if (attModelRepository.exists(pAttributeId)) {
            attModelRepository.delete(pAttributeId);
        }
    }

    /**
     * Manage attribute model restriction
     *
     * @param pAttributeModel
     *            attribute model
     */
    private void manageRestriction(AttributeModel pAttributeModel) {
        final AbstractRestriction restriction = pAttributeModel.getRestriction();
        if (restriction != null) {
            if (restriction.supports(pAttributeModel.getType())) {
                if (!restriction.isIdentifiable()) {
                    restrictionRepository.save(restriction);
                }
            } else {
                // Unset restriction
                pAttributeModel.setRestriction(null);
                LOG.warn("Restriction type \"{}\" does not support attribute of type \"{}\".", restriction.getType(),
                         pAttributeModel.getType());
            }
        }
    }

    /**
     * Manage attribute model fragment (fallback to default fragment)
     *
     * @param pAttributeModel
     *            attribute model
     * @return fragment
     */
    private Fragment manageFragment(AttributeModel pAttributeModel) {
        Fragment fragment = pAttributeModel.getFragment();
        if (fragment != null) {
            fragment = initOrRetrieveFragment(fragment);
        } else {
            // Fallback to default fragment
            fragment = initOrRetrieveFragment(Fragment.buildDefault());
        }
        pAttributeModel.setFragment(fragment);
        return fragment;
    }

    private Fragment initOrRetrieveFragment(Fragment pFragment) {
        Fragment fragment = fragmentRepository.findByName(pFragment.getName());
        if (fragment == null) {
            pFragment.setId(null);
            fragment = fragmentRepository.save(pFragment);
        }
        return fragment;
    }

    /**
     * Manage a single attribute model
     *
     * @param pAttributeModel
     *            the attribute model
     * @return the persisted attribute model
     * @throws ModuleException
     *             if conflict detected
     */
    private AttributeModel manageAttributeModel(AttributeModel pAttributeModel) throws ModuleException {
        if (!pAttributeModel.isIdentifiable()) {
            // Check potential conflict
            final AttributeModel attributeModel = attModelRepository
                    .findByNameAndFragmentName(pAttributeModel.getName(), pAttributeModel.getFragment().getName());
            if (attributeModel != null) {
                final String message;
                if (pAttributeModel.getFragment().isDefaultFragment()) {
                    message = MessageFormat.format("Attribute model with name \"{0}\" already exists.",
                                                   pAttributeModel.getName());
                } else {
                    // CHECKSTYLE:OFF
                    message = MessageFormat.format(
                                                   "Attribute model with name \"{0}\" in fragment \"{1}\" already exists.",
                                                   pAttributeModel.getName(), pAttributeModel.getFragment().getName());
                    // CHECKSTYLE:ON
                }
                LOG.error(message);
                throw new EntityAlreadyExistsException(message);
            }
        }
        return attModelRepository.save(pAttributeModel);
    }

    @Override
    public boolean isFragmentAttribute(Long pAttributeId) throws ModuleException {
        final AttributeModel attModel = attModelRepository.findOne(pAttributeId);
        if (attModel == null) {
            throw new EntityNotFoundException(pAttributeId, AttributeModel.class);
        }
        return !attModel.getFragment().isDefaultFragment();
    }

    @Override
    public List<AttributeModel> findByFragmentId(Long pFragmentId) throws ModuleException {
        return IterableUtils.toList(attModelRepository.findByFragmentId(pFragmentId));
    }
}
