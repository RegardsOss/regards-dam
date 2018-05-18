/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.DoubleRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;

/**
 * Restriction validator factory
 *
 * @author Marc Sordi
 *
 */
public final class RestrictionValidatorFactory {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RestrictionValidatorFactory.class);

    private RestrictionValidatorFactory() {
    }

    public static Validator getValidator(AbstractRestriction pRestriction, String pAttributeKey) {
        if (pRestriction instanceof EnumerationRestriction) {
            return getValidator((EnumerationRestriction) pRestriction, pAttributeKey);
        }
        if (pRestriction instanceof DoubleRangeRestriction) {
            return getValidator((DoubleRangeRestriction) pRestriction, pAttributeKey);
        }
        if (pRestriction instanceof IntegerRangeRestriction) {
            return getValidator((IntegerRangeRestriction) pRestriction, pAttributeKey);
        }
        if (pRestriction instanceof PatternRestriction) {
            return getValidator((PatternRestriction) pRestriction, pAttributeKey);
        }
        String errorMessage = String.format("No validator found for restriction type %s and attribute %s.",
                                            pRestriction.getType(), pAttributeKey);
        LOGGER.debug(errorMessage);
        throw new UnsupportedOperationException(errorMessage);
    }

    public static Validator getValidator(EnumerationRestriction pRestriction, String pAttributeKey) {
        return new EnumerationValidator(pRestriction, pAttributeKey);
    }

    public static Validator getValidator(DoubleRangeRestriction pRestriction, String pAttributeKey) {
        return new DoubleRangeValidator(pRestriction, pAttributeKey);
    }

    public static Validator getValidator(IntegerRangeRestriction pRestriction, String pAttributeKey) {
        return new IntegerRangeValidator(pRestriction, pAttributeKey);
    }

    public static Validator getValidator(PatternRestriction pRestriction, String pAttributeKey) {
        return new PatternValidator(pRestriction, pAttributeKey);
    }
}
