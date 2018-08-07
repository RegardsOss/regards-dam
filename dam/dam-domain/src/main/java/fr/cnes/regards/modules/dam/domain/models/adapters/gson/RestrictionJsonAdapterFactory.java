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
package fr.cnes.regards.modules.dam.domain.models.adapters.gson;

import fr.cnes.regards.framework.gson.adapters.PolymorphicTypeAdapterFactory;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.DoubleRangeRestriction;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.LongRangeRestriction;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.PatternRestriction;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.RestrictionType;

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
