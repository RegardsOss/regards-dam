/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.jpa.utils.IterableUtils;
import fr.cnes.regards.framework.module.rest.exception.ModuleEntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleEntityNotIdentifiableException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.rest.exception.ModuleInconsistentEntityIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.ModuleUnexpectedEntityIdentifierException;
import fr.cnes.regards.modules.models.dao.IModelAttributeRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.service.exception.FragmentAttributeException;
import fr.cnes.regards.modules.models.service.exception.UnexpectedModelAttributeException;

/**
 *
 * Model attribute service
 *
 * @author Marc Sordi
 *
 */
@Service
public class ModelAttributeService implements IModelAttributeService {

    private static final Logger LOG = LoggerFactory.getLogger(ModelAttributeService.class);

    /**
     * Model attribute repository
     */
    private final IModelAttributeRepository modelAttributeRepository;

    /**
     * Model repository
     */
    private final IModelService modelService;

    /**
     * Attribute model service
     */
    private final IAttributeModelService attributeModelService;

    /**
     * Fragment service
     */
    private final IFragmentService fragmentService;

    public ModelAttributeService(IModelService pModelService, IModelAttributeRepository pModelAttributeRepository,
            IAttributeModelService pAttributeModelService, IFragmentService pFragmentService) {
        this.modelService = pModelService;
        this.modelAttributeRepository = pModelAttributeRepository;
        this.attributeModelService = pAttributeModelService;
        this.fragmentService = pFragmentService;
    }

    @Override
    public List<ModelAttribute> getModelAttributes(Long pModelId) throws ModuleException {
        modelService.getModel(pModelId);
        return IterableUtils.toList(modelAttributeRepository.findByModelId(pModelId));
    }

    @Override
    public ModelAttribute bindAttributeToModel(Long pModelId, ModelAttribute pModelAttribute) throws ModuleException {
        final Model model = modelService.getModel(pModelId);
        if (pModelAttribute.isIdentifiable()) {
            throw new ModuleUnexpectedEntityIdentifierException(pModelAttribute.getId(), ModelAttribute.class);
        }
        // Do not bind attribute that is part of a fragment
        if (attributeModelService.isFragmentAttribute(pModelAttribute.getAttribute().getId())) {
            throw new FragmentAttributeException(pModelAttribute.getAttribute().getId());
        }
        pModelAttribute.setModel(model);
        return modelAttributeRepository.save(pModelAttribute);
    }

    @Override
    public ModelAttribute getModelAttribute(Long pModelId, Long pAttributeId) throws ModuleException {
        final ModelAttribute modelAtt = modelAttributeRepository.findOne(pAttributeId);
        if (modelAtt == null) {
            throw new ModuleEntityNotFoundException(pAttributeId, ModelAttribute.class);
        }
        if (!pModelId.equals(modelAtt.getModel().getId())) {
            throw new UnexpectedModelAttributeException(pModelId, pAttributeId);
        }
        return modelAtt;
    }

    @Override
    public ModelAttribute updateModelAttribute(Long pModelId, Long pAttributeId, ModelAttribute pModelAttribute)
            throws ModuleException {
        if (!pModelAttribute.isIdentifiable()) {
            throw new ModuleEntityNotIdentifiableException(
                    String.format("Unknown identifier for model attribute \"%s\"", pModelAttribute.getId()));
        }
        if (!pModelAttribute.getId().equals(pAttributeId)) {
            throw new ModuleInconsistentEntityIdentifierException(pAttributeId, pModelAttribute.getId(),
                    ModelAttribute.class);
        }
        if (!modelAttributeRepository.exists(pAttributeId)) {
            throw new ModuleEntityNotFoundException(pAttributeId, ModelAttribute.class);
        }
        return modelAttributeRepository.save(pModelAttribute);
    }

    @Override
    public void unbindAttributeFromModel(Long pModelId, Long pAttributeId) throws ModuleException {
        final ModelAttribute modelAtt = getModelAttribute(pModelId, pAttributeId);
        // Do not bind attribute that is part of a fragment
        if (attributeModelService.isFragmentAttribute(modelAtt.getAttribute().getId())) {
            throw new FragmentAttributeException(modelAtt.getAttribute().getId());
        }
        modelAttributeRepository.delete(pAttributeId);
    }

    @Override
    @MultitenantTransactional
    public List<ModelAttribute> bindNSAttributeToModel(Long pModelId, Long pFragmentId) throws ModuleException {
        final List<ModelAttribute> modAtts = new ArrayList<>();
        final Model model = modelService.getModel(pModelId);
        final Iterable<ModelAttribute> existingModelAtts = modelAttributeRepository.findByModelId(pModelId);

        // Check if fragment not already bound
        if (!isBoundFragment(existingModelAtts, pFragmentId)) {

            // Retrieve fragment attributes
            final List<AttributeModel> attModels = attributeModelService.findByFragmentId(pFragmentId);

            if (attModels != null) {
                for (AttributeModel attModel : attModels) {
                    // Create model attributes to link base attributes
                    final ModelAttribute modelAtt = new ModelAttribute();
                    modelAtt.setAttribute(attModel);
                    modelAtt.setModel(model);
                    modelAttributeRepository.save(modelAtt);
                    modAtts.add(modelAtt);
                }
            }
        } else {
            LOG.warn("Fragment {} already bound to model {}", pFragmentId, pModelId);
        }
        return modAtts;
    }

    /**
     * Check if fragment is bounded to the model
     *
     * @param pModelAtts
     *            model attributes
     * @param pFragmentId
     *            fragment identifier
     * @return true if fragment is bound
     */
    private boolean isBoundFragment(final Iterable<ModelAttribute> pModelAtts, Long pFragmentId) {
        if (pModelAtts != null) {
            for (ModelAttribute modelAtt : pModelAtts) {
                if (pFragmentId.equals(modelAtt.getAttribute().getFragment().getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @MultitenantTransactional
    public void unbindNSAttributeToModel(Long pModelId, Long pFragmentId) throws ModuleException {
        Iterable<ModelAttribute> modelAtts = modelAttributeRepository.findByModelId(pModelId);
        if (modelAtts != null) {
            for (ModelAttribute modelAtt : modelAtts) {
                if (pFragmentId.equals(modelAtt.getAttribute().getFragment().getId())) {
                    modelAttributeRepository.delete(modelAtt);
                }
            }
        }
    }

    @Override
    public void updateNSBind(Long pFragmentId) throws ModuleException {
        // FIXME update all model bound to this fragment if fragment attribute list is updated
    }
}
