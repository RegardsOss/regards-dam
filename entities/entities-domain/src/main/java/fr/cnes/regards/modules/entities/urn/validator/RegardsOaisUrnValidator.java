/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.urn.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class RegardsOaisUrnValidator implements ConstraintValidator<RegardsOaisUrn, UniformResourceName> {

    @Override
    public void initialize(RegardsOaisUrn pConstraintAnnotation) {
        // nothing to initialize for now
    }

    @Override
    public boolean isValid(UniformResourceName pValue, ConstraintValidatorContext pContext) {
        return (pValue == null) || !(pValue.getOaisIdentifier().equals(OAISIdentifier.SIP)
                && ((pValue.getOrder() != null) || (pValue.getRevision() != null)));
    }

}