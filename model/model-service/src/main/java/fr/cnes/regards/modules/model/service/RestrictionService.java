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
package fr.cnes.regards.modules.model.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.model.domain.attributes.restriction.IRestriction;
import fr.cnes.regards.modules.model.domain.attributes.restriction.RestrictionFactory;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * Restriction service
 *
 * @author Marc Sordi
 *
 */
@Service
public class RestrictionService {

    /**
     * List of all available restriction<br/>
     * The list contains an empty instance of each restriction type.
     */
    private List<IRestriction> restrictions;

    @PostConstruct
    public void init() {
        restrictions = new ArrayList<>();
        restrictions.add(RestrictionFactory.buildEnumerationRestriction());
        restrictions.add(RestrictionFactory.buildFloatRangeRestriction(null, null, false, false));
        restrictions.add(RestrictionFactory.buildLongRangeRestriction(null, null, false, false));
        restrictions.add(RestrictionFactory.buildIntegerRangeRestriction(null, null, false, false));
        restrictions.add(RestrictionFactory.buildPatternRestriction(null));
    }

    /**
     * Regarding the list of available restriction, this method computes the list of applicable ones for a particular
     * type of attribute.
     *
     * @param pType
     *            attribute type
     * @return list of restriction supported by the attribute type
     */
    public List<String> getRestrictions(PropertyType pType) {
        final List<String> restrictionList = new ArrayList<>();
        for (IRestriction restriction : restrictions) {
            if (restriction.supports(pType)) {
                restrictionList.add(restriction.getType().name());
            }
        }
        return restrictionList;
    }
}
