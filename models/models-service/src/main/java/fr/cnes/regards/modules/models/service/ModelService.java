/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
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

    @Override
    public List<Model> getModels(ModelType pType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Model createModel(Model pModel) throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Model getModel(Long pModelId) throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Model updateModel(Long pModelId, Model pModel) throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteModel(Long pModelId) throws ModuleException {
        // TODO Auto-generated method stub

    }

    @Override
    public Model duplicateModel(Long pModelId, Model pModel) throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }
}
