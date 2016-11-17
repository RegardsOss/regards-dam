/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.utils.IterableUtils;
import fr.cnes.regards.framework.module.rest.exception.ModuleAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.ModuleEntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleEntityNotIdentifiableException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.rest.exception.ModuleInconsistentEntityIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.ModuleUnexpectedEntityIdentifierException;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelType;

/**
 * Manage model lifecycle
 *
 * @author Marc Sordi
 *
 */
@Service
public class ModelService implements IModelService {

    /**
     * Model repository
     */
    private final IModelRepository modelRepository;

    public ModelService(IModelRepository pModelRepository) {
        this.modelRepository = pModelRepository;
    }

    @Override
    public List<Model> getModels(ModelType pType) {
        return IterableUtils.toList(modelRepository.findByType(pType));
    }

    @Override
    public Model createModel(Model pModel) throws ModuleException {
        if (pModel.isIdentifiable()) {
            throw new ModuleUnexpectedEntityIdentifierException(pModel.getId(), Model.class);
        }
        final Model model = modelRepository.findByName(pModel.getName());
        if (model != null) {
            throw new ModuleAlreadyExistsException(
                    String.format("Model with name \"%s\" already exists!", pModel.getName()));
        }
        return modelRepository.save(pModel);
    }

    @Override
    public Model getModel(Long pModelId) throws ModuleException {
        if (!modelRepository.exists(pModelId)) {
            throw new ModuleEntityNotFoundException(pModelId, Model.class);
        }
        return modelRepository.findOne(pModelId);
    }

    @Override
    public Model updateModel(Long pModelId, Model pModel) throws ModuleException {
        if (!pModel.isIdentifiable()) {
            throw new ModuleEntityNotIdentifiableException(
                    String.format("Unknown identifier for model \"%s\"", pModel.getName()));
        }
        if (!pModelId.equals(pModel.getId())) {
            throw new ModuleInconsistentEntityIdentifierException(pModelId, pModel.getId(), pModel.getClass());
        }
        if (!modelRepository.exists(pModelId)) {
            throw new ModuleEntityNotFoundException(pModel.getId(), Model.class);
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
    public Model duplicateModel(Long pModelId, Model pModel) throws ModuleException {
        if (!modelRepository.exists(pModelId)) {
            throw new ModuleEntityNotFoundException(pModel.getId(), Model.class);
        }
        final Model duplicatedModel = createModel(pModel);
        // TODO assign all reference model attributes to duplicated one
        return duplicatedModel;
    }
}
