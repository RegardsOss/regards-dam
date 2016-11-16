/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.ModelAttribute;

/**
 *
 * Model attribute service
 *
 * @author Marc Sordi
 *
 */
@Service
public class ModelAttributeService implements IModelAttributeService {

    @Override
    public List<ModelAttribute> getModelAttributes(Long pModelId) throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ModelAttribute bindAttributeToModel(Long pModelId, ModelAttribute pModelAttribute) throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ModelAttribute getModelAttribute(Long pModelId, Long pAttributeId) throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ModelAttribute updateModelAttribute(Long pModelId, Long pAttributeId, ModelAttribute pModelAttribute)
            throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unbindAttributeFromModel(Long pModelId, Long pAttributeId) throws ModuleException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<ModelAttribute> bindNSAttributeToModel(Long pModelId, Long pFragmentId) throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unbindNSAttributeToModel(Long pModelId, Long pFragmentId) throws ModuleException {
        // TODO Auto-generated method stub

    }

}
