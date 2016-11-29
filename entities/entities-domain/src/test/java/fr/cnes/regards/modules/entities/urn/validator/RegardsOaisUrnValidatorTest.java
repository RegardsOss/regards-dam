/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.urn.validator;

import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class RegardsOaisUrnValidatorTest {

    /**
     * Validator
     */
    private static Validator validator;

    @BeforeClass
    public static void init() {
        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void testFullAip() {
        final UniformResourceName urn = new UniformResourceName(OAISIdentifier.AIP,
                AbstractEntity.class.getSimpleName(), "Tenant123", UUID.randomUUID(), 1, 3L, "4");
        final Set<ConstraintViolation<UniformResourceName>> urnConstraintViolation = validator.validate(urn);
        Assert.assertEquals(0, urnConstraintViolation.size());
    }

    @Test
    public void testMinimalAip() {
        final UniformResourceName urn = new UniformResourceName(OAISIdentifier.AIP,
                AbstractEntity.class.getSimpleName(), "Tenant123", UUID.randomUUID(), 1);
        final Set<ConstraintViolation<UniformResourceName>> urnConstraintViolation = validator.validate(urn);
        Assert.assertEquals(0, urnConstraintViolation.size());
    }

    @Test
    public void testOrderAip() {
        final UniformResourceName urn = new UniformResourceName(OAISIdentifier.AIP,
                AbstractEntity.class.getSimpleName(), "Tenant123", UUID.randomUUID(), 1, 3L);
        final Set<ConstraintViolation<UniformResourceName>> urnConstraintViolation = validator.validate(urn);
        Assert.assertEquals(0, urnConstraintViolation.size());
    }

    @Test
    public void testRevisionAip() {
        final UniformResourceName urn = new UniformResourceName(OAISIdentifier.AIP,
                AbstractEntity.class.getSimpleName(), "Tenant123", UUID.randomUUID(), 1, "3");
        final Set<ConstraintViolation<UniformResourceName>> urnConstraintViolation = validator.validate(urn);
        Assert.assertEquals(0, urnConstraintViolation.size());
    }

    @Test
    public void testValidSip() {
        final UniformResourceName urn = new UniformResourceName(OAISIdentifier.SIP,
                AbstractEntity.class.getSimpleName(), "Tenant123", UUID.randomUUID(), 1);
        final Set<ConstraintViolation<UniformResourceName>> urnConstraintViolation = validator.validate(urn);
        Assert.assertEquals(0, urnConstraintViolation.size());
    }

    @Test
    public void testInvalidSipOrderAndRev() {
        final UniformResourceName urn = new UniformResourceName(OAISIdentifier.SIP,
                AbstractEntity.class.getSimpleName(), "Tenant123", UUID.randomUUID(), 1, 3L, "1");
        final Set<ConstraintViolation<UniformResourceName>> urnConstraintViolation = validator.validate(urn);
        Assert.assertEquals(1, urnConstraintViolation.size());
        Assert.assertEquals("{" + RegardsOaisUrn.CLASS_NAME + "message}",
                            urnConstraintViolation.iterator().next().getMessage());
    }

    @Test
    public void testInvalidOrderSip() {
        final UniformResourceName urn = new UniformResourceName(OAISIdentifier.SIP,
                AbstractEntity.class.getSimpleName(), "Tenant123", UUID.randomUUID(), 1, 3L);
        final Set<ConstraintViolation<UniformResourceName>> urnConstraintViolation = validator.validate(urn);
        Assert.assertEquals(1, urnConstraintViolation.size());
        Assert.assertEquals("{" + RegardsOaisUrn.CLASS_NAME + "message}",
                            urnConstraintViolation.iterator().next().getMessage());
    }

    @Test
    public void testInvalidRevisionSip() {
        final UniformResourceName urn = new UniformResourceName(OAISIdentifier.SIP,
                AbstractEntity.class.getSimpleName(), "Tenant123", UUID.randomUUID(), 1, "2");
        final Set<ConstraintViolation<UniformResourceName>> urnConstraintViolation = validator.validate(urn);
        Assert.assertEquals(1, urnConstraintViolation.size());
        Assert.assertEquals("{" + RegardsOaisUrn.CLASS_NAME + "message}",
                            urnConstraintViolation.iterator().next().getMessage());
    }

}
