/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.validator.CheckModel;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelAttributeService;

/**
 * Validate an entity according to its model definition
 *
 * @author Marc Sordi
 *
 */
public class ModelValidator implements ConstraintValidator<CheckModel, AbstractEntity> {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelValidator.class);

    @Autowired
    private IModelAttributeService modelAttributeService;

    @Override
    public void initialize(CheckModel pConstraintAnnotation) {
        // Nothing to do at the moment
    }

    @Override
    public boolean isValid(AbstractEntity pValue, ConstraintValidatorContext pContext) {

        Model model = pValue.getModel();
        if (model == null) {
            return false;
        }

        if (modelAttributeService == null) {
            LOGGER.debug("null bean");
        }
        // TODO : implement model validation
        return true;
    }

}
