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
package fr.cnes.regards.modules.indexer.domain.criterion;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * OffsetDateTime RangeCriterion specialization
 */
public class DateRangeCriterion extends RangeCriterion<OffsetDateTime> implements ICriterion {

    protected DateRangeCriterion(String name) {
        super(name);
    }

    @Override
    public DateRangeCriterion copy() {
        DateRangeCriterion copy = new DateRangeCriterion(super.name);
        copy.valueComparisons
                .addAll(super.valueComparisons.stream().map(ValueComparison::copy).collect(Collectors.toSet()));
        return copy;

    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitDateRangeCriterion(this);
    }

}
