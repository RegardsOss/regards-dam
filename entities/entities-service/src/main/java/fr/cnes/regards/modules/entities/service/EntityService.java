/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.service.IModelAttributeService;

/**
 * Entity service implementation
 *
 * @author Marc Sordi
 *
 */
@Service
public class EntityService implements IEntityService {

    /**
     * Attribute model service
     */
    @Autowired
    private IModelAttributeService modelAttributeService;

    @Override
    public boolean validate(AbstractEntity pAbstractEntity) throws ModuleException {
        Assert.notNull(pAbstractEntity, "Entity must not be null.");

        Model model = pAbstractEntity.getModel();
        Assert.notNull(model, "Model must be set on entity in order to be validated.");
        Assert.notNull(model.getId(), "Model identifier must be specified.");

        List<ModelAttribute> modAtts = modelAttributeService.getModelAttributes(model.getId());

        // TODO
        return false;
    }

}
