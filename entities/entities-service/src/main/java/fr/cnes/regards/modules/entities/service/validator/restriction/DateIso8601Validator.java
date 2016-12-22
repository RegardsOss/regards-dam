/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.entities.domain.attribute.DateArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateIntervalAttribute;

/**
 * Validate {@link DateAttribute}, {@link DateArrayAttribute} and {@link DateIntervalAttribute} that must manage ISO
 * 8601 date format.
 *
 * <br/>
 * ISO 8601 date examples :
 * <ul>
 * <li>1977-04-22T01:00:00-05:00</li>
 * <li>equivalent to 1977-04-22T06:00:00Z</li>
 * </ul>
 *
 * @author Marc Sordi
 *
 *
 */
public class DateIso8601Validator implements Validator {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(DateIso8601Validator.class);

    public static final DateTimeFormatter ISO_DATE_TIME_OPTIONAL_OFFSET;
    static {
        ISO_DATE_TIME_OPTIONAL_OFFSET = new DateTimeFormatterBuilder().parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME).optionalStart().appendOffsetId().toFormatter();
    }

    @Override
    public boolean supports(Class<?> pClazz) {
        return (pClazz == DateAttribute.class) || (pClazz == DateArrayAttribute.class)
                || (pClazz == DateIntervalAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        // Nothing to do : format validation done with GSON parser
    }

}
