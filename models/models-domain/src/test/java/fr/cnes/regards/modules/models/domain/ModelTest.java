/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Before;

/**
 * Test {@link Model}
 *
 * @author Marc Sordi
 *
 */
public class ModelTest {

    /**
     * Validator
     */
    private Validator validator;

    @Before
    public void setUp() {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // TODO
}
