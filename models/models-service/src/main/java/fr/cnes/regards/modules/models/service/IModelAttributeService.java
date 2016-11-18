/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;

/**
 *
 * Model attribute service
 *
 * @author Marc Sordi
 *
 */
public interface IModelAttributeService {

    List<ModelAttribute> getModelAttributes(Long pModelId) throws ModuleException;

    ModelAttribute bindAttributeToModel(Long pModelId, ModelAttribute pModelAttribute) throws ModuleException;

    ModelAttribute getModelAttribute(Long pModelId, Long pAttributeId) throws ModuleException;

    ModelAttribute updateModelAttribute(Long pModelId, Long pAttributeId, ModelAttribute pModelAttribute)
            throws ModuleException;

    void unbindAttributeFromModel(Long pModelId, Long pAttributeId) throws ModuleException;

    List<ModelAttribute> bindNSAttributeToModel(Long pModelId, Long pFragmentId) throws ModuleException;

    /**
     * Propagate a fragment update
     *
     * @param pFragmentId
     *            fragment updated
     * @throws ModuleException
     *             if error occurs!
     */
    void updateNSBind(Long pFragmentId) throws ModuleException;

    void unbindNSAttributeToModel(Long pModelId, Long pFragmentId) throws ModuleException;

    Model duplicateModelAttributes(Long pSourceModelId, Model pTargetModel) throws ModuleException;

}
