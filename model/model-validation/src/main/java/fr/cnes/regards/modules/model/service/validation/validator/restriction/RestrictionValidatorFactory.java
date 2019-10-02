/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.model.service.validation.validator.restriction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.model.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.model.domain.attributes.restriction.DoubleRangeRestriction;
import fr.cnes.regards.modules.model.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.model.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.model.domain.attributes.restriction.PatternRestriction;

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

    public static Validator getValidator(AbstractRestriction restriction, String attributeKey) {
        if (restriction instanceof EnumerationRestriction) {
            return getValidator((EnumerationRestriction) restriction, attributeKey);
        }
        if (restriction instanceof DoubleRangeRestriction) {
            return getValidator((DoubleRangeRestriction) restriction, attributeKey);
        }
        if (restriction instanceof IntegerRangeRestriction) {
            return getValidator((IntegerRangeRestriction) restriction, attributeKey);
        }
        if (restriction instanceof PatternRestriction) {
            return getValidator((PatternRestriction) restriction, attributeKey);
        }
        String errorMessage = String.format("No validator found for restriction type %s and attribute %s.",
                                            restriction.getType(), attributeKey);
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
