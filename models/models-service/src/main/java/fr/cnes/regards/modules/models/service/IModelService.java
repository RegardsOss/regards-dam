/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.io.OutputStream;
import java.util.List;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelType;

/**
 *
 * Model service
 *
 * @author Marc Sordi
 *
 */
public interface IModelService {

    List<Model> getModels(ModelType pType);

    Model createModel(Model pModel) throws ModuleException;

    Model getModel(Long pModelId) throws ModuleException;

    Model updateModel(Long pModelId, Model pModel) throws ModuleException;

    void deleteModel(Long pModelId) throws ModuleException;

    Model duplicateModel(Long pModelId, Model pModel) throws ModuleException;

    void exportModel(Long pModelId, OutputStream pOutputStream) throws ModuleException;
}
