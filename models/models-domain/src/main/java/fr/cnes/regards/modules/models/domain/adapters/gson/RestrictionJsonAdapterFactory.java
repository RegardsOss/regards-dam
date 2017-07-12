/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.models.domain.adapters.gson;

import fr.cnes.regards.framework.gson.adapters.PolymorphicTypeAdapterFactory;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.DoubleRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.LongRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionType;

/**
 *
 * Restriction adapter
 *
 * @author Marc Sordi
 *
 */
public class RestrictionJsonAdapterFactory extends PolymorphicTypeAdapterFactory<AbstractRestriction> {

    protected RestrictionJsonAdapterFactory() {
        super(AbstractRestriction.class, "type");
        registerSubtype(EnumerationRestriction.class, RestrictionType.ENUMERATION);
        registerSubtype(PatternRestriction.class, RestrictionType.PATTERN);
        registerSubtype(DoubleRangeRestriction.class, RestrictionType.DOUBLE_RANGE);
        registerSubtype(IntegerRangeRestriction.class, RestrictionType.INTEGER_RANGE);
        registerSubtype(LongRangeRestriction.class, RestrictionType.LONG_RANGE);
    }
}
