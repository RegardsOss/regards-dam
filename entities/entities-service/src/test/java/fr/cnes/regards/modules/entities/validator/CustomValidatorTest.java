/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.validator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Marc Sordi
 *
 */
@Ignore
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CustomValidatorConfiguration.class)
public class CustomValidatorTest {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomValidatorTest.class);

    /**
     * Javax validator
     */
    private static Validator validator;

    /**
     * Set up the validator
     */
    @BeforeClass
    public static void setUpValidator() {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void validateDoc() {
        Doc doc = new Doc();
        doc.setDocName("sampledoc");

        // Run the validator
        final Set<ConstraintViolation<Doc>> constraintViolations = validator.validate(doc);

        LOGGER.debug("ok");
    }
}
