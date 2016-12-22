/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.entities.service.validator.restriction.EnumerationValidator;
import fr.cnes.regards.modules.entities.service.validator.restriction.FloatRangeValidator;
import fr.cnes.regards.modules.entities.service.validator.restriction.IntegerRangeValidator;
import fr.cnes.regards.modules.entities.service.validator.restriction.PatternValidator;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.FloatRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;

/**
 * Restriction validator factory
 *
 * @author Marc Sordi
 *
 */
@Service
public final class ValidatorFactory {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorFactory.class);

    /**
     * Static validators
     */
    private static Map<Class<?>, Validator> staticValidators = new HashMap<>();

    //
    // @PostConstruct
    // private void initFactory() throws UnknownRestrictionValidatorException {
    // initValidators();
    // }
    //
    // public Validator getValidator(IRestriction pRestriction) throws UnknownRestrictionValidatorException {
    // return validators.get(pRestriction.getType());
    // }
    //
    // private void initValidators() throws UnknownRestrictionValidatorException {
    // if (validators == null) {
    // validators = new EnumMap<>(RestrictionType.class);
    // for (RestrictionType rType : RestrictionType.values()) {
    // validators.put(rType, getValidatorInstance(rType));
    // }
    // }
    // }
    //
    // private Validator getValidatorInstance(RestrictionType pRestrictionType)
    // throws UnknownRestrictionValidatorException {
    // Validator validator;
    // switch (pRestrictionType) {
    // case NO_RESTRICTION:
    // validator = new NoRestrictionValidator();
    // break;
    // case DATE_ISO8601:
    // validator = new DateIso8601Validator();
    // break;
    // case ENUMERATION:
    // validator = new EnumerationValidator(null);
    // break;
    // case FLOAT_RANGE:
    // validator = new FloatRangeValidator();
    // break;
    // case GEOMETRY:
    // validator = new GeometryValidator();
    // break;
    // case INTEGER_RANGE:
    // validator = new IntegerRangeValidator();
    // break;
    // case PATTERN:
    // validator = new PatternValidator();
    // break;
    // case URL:
    // validator = new UrlValidator();
    // break;
    // default:
    // String errorMessage = String.format("No validator found for restriction %s.", pRestrictionType);
    // LOGGER.debug(errorMessage);
    // throw new UnknownRestrictionValidatorException(errorMessage);
    // }
    // return validator;
    // }

    // TODO
    // public static Validator getValidator(AbstractAttribute<?> pAttribute) {
    // return new EnumerationValidator(pRestriction);
    // }

    public static Validator getValidator(AbstractRestriction pRestriction) {
        String errorMessage = String.format("No validator found for restriction type %s.", pRestriction.getType());
        LOGGER.debug(errorMessage);
        throw new UnsupportedOperationException(errorMessage);
    }

    public static Validator getValidator(EnumerationRestriction pRestriction) {
        return new EnumerationValidator(pRestriction);
    }

    public static Validator getValidator(FloatRangeRestriction pRestriction) {
        return new FloatRangeValidator(pRestriction);
    }

    public static Validator getValidator(IntegerRangeRestriction pRestriction) {
        return new IntegerRangeValidator(pRestriction);
    }

    public static Validator getValidator(PatternRestriction pRestriction) {
        return new PatternValidator(pRestriction);
    }
}
