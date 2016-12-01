/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.jpa.utils.IterableUtils;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotIdentifiableException;
import fr.cnes.regards.framework.module.rest.exception.EntityUnexpectedIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.dao.IModelAttributeRepository;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.ModelType;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.service.exception.FragmentAttributeException;
import fr.cnes.regards.modules.models.service.exception.UnexpectedModelAttributeException;
import fr.cnes.regards.modules.models.service.xml.XmlExportHelper;
import fr.cnes.regards.modules.models.service.xml.XmlImportHelper;

/**
 * Manage model lifecycle
 *
 * @author Marc Sordi
 *
 */
@Service
public class ModelService implements IModelService, IModelAttributeService {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelService.class);

    /**
     * Model repository
     */
    private final IModelRepository modelRepository;

    /**
     * Model attribute repository
     */
    private final IModelAttributeRepository modelAttributeRepository;

    /**
     * Attribute model service
     */
    private final IAttributeModelService attributeModelService;

    // CHECKSTYLE:OFF
    public ModelService(IModelRepository pModelRepository, IModelAttributeRepository pModelAttributeRepository,
            IAttributeModelService pAttributeModelService) {
        this.modelRepository = pModelRepository;
        this.modelAttributeRepository = pModelAttributeRepository;
        this.attributeModelService = pAttributeModelService;
    }
    // CHECKSTYLE:ON

    @Override
    public List<Model> getModels(ModelType pType) {
        return IterableUtils.toList(modelRepository.findByType(pType));
    }

    @Override
    public Model createModel(Model pModel) throws ModuleException {
        if (pModel.isIdentifiable()) {
            throw new EntityUnexpectedIdentifierException(pModel.getId(), Model.class);
        }
        final Model model = modelRepository.findByName(pModel.getName());
        if (model != null) {
            throw new EntityAlreadyExistsException(
                    String.format("Model with name \"%s\" already exists!", pModel.getName()));
        }
        return modelRepository.save(pModel);
    }

    @Override
    public Model getModel(Long pModelId) throws ModuleException {
        final Model model = modelRepository.findOne(pModelId);
        if (model == null) {
            throw new EntityNotFoundException(pModelId, Model.class);
        }
        return model;
    }

    @Override
    public Model updateModel(Long pModelId, Model pModel) throws ModuleException {
        if (!pModel.isIdentifiable()) {
            throw new EntityNotIdentifiableException(
                    String.format("Unknown identifier for model \"%s\"", pModel.getName()));
        }
        if (!pModelId.equals(pModel.getId())) {
            throw new EntityInconsistentIdentifierException(pModelId, pModel.getId(), pModel.getClass());
        }
        if (!modelRepository.exists(pModelId)) {
            throw new EntityNotFoundException(pModel.getId(), Model.class);
        }
        return modelRepository.save(pModel);
    }

    @Override
    public void deleteModel(Long pModelId) throws ModuleException {
        if (modelRepository.exists(pModelId)) {
            modelRepository.delete(pModelId);
        }
    }

    @Override
    @MultitenantTransactional
    public Model duplicateModel(Long pModelId, Model pModel) throws ModuleException {
        if (!modelRepository.exists(pModelId)) {
            throw new EntityNotFoundException(pModel.getId(), Model.class);
        }
        return duplicateModelAttributes(pModelId, createModel(pModel));
    }

    @Override
    public List<ModelAttribute> getModelAttributes(Long pModelId) throws ModuleException {
        getModel(pModelId);
        return IterableUtils.toList(modelAttributeRepository.findByModelId(pModelId));
    }

    @Override
    public ModelAttribute bindAttributeToModel(Long pModelId, ModelAttribute pModelAttribute) throws ModuleException {
        final Model model = getModel(pModelId);
        if (pModelAttribute.isIdentifiable()) {
            throw new EntityUnexpectedIdentifierException(pModelAttribute.getId(), ModelAttribute.class);
        }
        // Do not bind attribute that is part of a fragment
        if (attributeModelService.isFragmentAttribute(pModelAttribute.getAttribute().getId())) {
            throw new FragmentAttributeException(pModelAttribute.getAttribute().getId());
        }
        // Do not rebind an attribute
        final Iterable<ModelAttribute> existingModelAtts = modelAttributeRepository.findByModelId(pModelId);
        if (existingModelAtts != null) {
            for (ModelAttribute modAtt : existingModelAtts) {
                if (modAtt.equals(pModelAttribute)) {
                    throw new EntityAlreadyExistsException(
                            String.format("Attribute %s already exists in model %s!", modAtt.getAttribute().getName(),
                                          model.getName()));
                }
            }
        }

        pModelAttribute.setModel(model);
        return modelAttributeRepository.save(pModelAttribute);
    }

    @Override
    public ModelAttribute getModelAttribute(Long pModelId, Long pAttributeId) throws ModuleException {
        final ModelAttribute modelAtt = modelAttributeRepository.findOne(pAttributeId);
        if (modelAtt == null) {
            throw new EntityNotFoundException(pAttributeId, ModelAttribute.class);
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
            throw new EntityNotIdentifiableException(
                    String.format("Unknown identifier for model attribute \"%s\"", pModelAttribute.getId()));
        }
        if (!pModelAttribute.getId().equals(pAttributeId)) {
            throw new EntityInconsistentIdentifierException(pAttributeId, pModelAttribute.getId(),
                    ModelAttribute.class);
        }
        if (!modelAttributeRepository.exists(pAttributeId)) {
            throw new EntityNotFoundException(pAttributeId, ModelAttribute.class);
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
        final Model model = getModel(pModelId);
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
            LOGGER.warn("Fragment {} already bound to model {}", pFragmentId, pModelId);
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
        final Iterable<ModelAttribute> modelAtts = modelAttributeRepository.findByModelId(pModelId);
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

    @Override
    public Model duplicateModelAttributes(Long pSourceModelId, Model pTargetModel) throws ModuleException {
        // Retrieve all reference model attributes
        final List<ModelAttribute> modelAtts = getModelAttributes(pSourceModelId);
        if (modelAtts != null) {
            for (ModelAttribute modelAtt : modelAtts) {
                // Create model attributes to link base attributes
                final ModelAttribute duplicatedModelAtt = new ModelAttribute();
                duplicatedModelAtt.setMode(modelAtt.getMode());
                duplicatedModelAtt.setAttribute(modelAtt.getAttribute());
                duplicatedModelAtt.setModel(pTargetModel);
                modelAttributeRepository.save(duplicatedModelAtt);
            }
        }
        return pTargetModel;
    }

    @Override
    public void exportModel(Long pModelId, OutputStream pOutputStream) throws ModuleException {
        // Get model
        final Model model = getModel(pModelId);
        // Get all related attributes
        final Iterable<ModelAttribute> modelAtts = getModelAttributes(pModelId);
        // Export fragment to output stream
        XmlExportHelper.exportModel(pOutputStream, model, modelAtts);
    }

    @MultitenantTransactional
    @Override
    public Iterable<ModelAttribute> importModel(InputStream pInputStream) throws ModuleException {
        // Import model from input stream
        final Iterable<ModelAttribute> modelAtts = XmlImportHelper.importModel(pInputStream);
        // Insert attributes
        for (ModelAttribute modelAtt : modelAtts) {
            // Create model
            createModel(modelAtt.getModel());
            // Create attribute
            attributeModelService.createAttribute(modelAtt.getAttribute());
            // Bind attribute to model
            modelAttributeRepository.save(modelAtt);
        }
        return modelAtts;
    }
}
