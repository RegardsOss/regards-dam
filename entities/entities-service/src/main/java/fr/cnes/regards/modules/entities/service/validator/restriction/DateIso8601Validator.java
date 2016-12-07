/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for Date ISO 8601
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

    public static final DateTimeFormatter ISO_DATE_TIME_OPTIONAL_OFFSET;
    static {
        ISO_DATE_TIME_OPTIONAL_OFFSET = new DateTimeFormatterBuilder().parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME).optionalStart().appendOffsetId().toFormatter();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> pClazz) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
    public void validate(Object pTarget, Errors pErrors) {
        // TODO Auto-generated method stub

    }

}
