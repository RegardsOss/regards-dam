/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.models.domain.attributes.restriction.IRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionType;

/**
 * Restriction validator factory
 *
 * @author Marc Sordi
 *
 */
@Service
public final class RestrictionValidatorFactory {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RestrictionValidatorFactory.class);

    /**
     * Validators
     */
    private Map<RestrictionType, Validator> validators;

    @PostConstruct
    private void initFactory() throws UnknownRestrictionValidatorException {
        initValidators();
    }

    public Validator getValidator(IRestriction pRestriction) throws UnknownRestrictionValidatorException {
        return validators.get(pRestriction.getType());
    }

    private void initValidators() throws UnknownRestrictionValidatorException {
        if (validators == null) {
            validators = new EnumMap<>(RestrictionType.class);
            for (RestrictionType rType : RestrictionType.values()) {
                validators.put(rType, getValidatorInstance(rType));
            }
        }
    }

    private Validator getValidatorInstance(RestrictionType pRestrictionType)
            throws UnknownRestrictionValidatorException {
        Validator validator;
        switch (pRestrictionType) {
            case NO_RESTRICTION:
                validator = new NoRestrictionValidator();
                break;
            case DATE_ISO8601:
                validator = new DateIso8601Validator();
                break;
            case ENUMERATION:
                validator = new EnumerationValidator();
                break;
            case FLOAT_RANGE:
                validator = new FloatRangeValidator();
                break;
            case GEOMETRY:
                validator = new GeometryValidator();
                break;
            case INTEGER_RANGE:
                validator = new IntegerRangeValidator();
                break;
            case PATTERN:
                validator = new PatternValidator();
                break;
            case URL:
                validator = new UrlValidator();
                break;
            default:
                String errorMessage = String.format("No validator found for restriction %s.", pRestrictionType);
                LOGGER.debug(errorMessage);
                throw new UnknownRestrictionValidatorException(errorMessage);
        }
        return validator;
    }
}
