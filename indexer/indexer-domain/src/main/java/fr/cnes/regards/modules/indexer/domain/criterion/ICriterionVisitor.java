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
package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * ICriterion visitor
 * @param <T> return type of all methods
 * @author oroussel
 */
public interface ICriterionVisitor<T> {

    T visitEmptyCriterion(EmptyCriterion criterion);

    T visitAndCriterion(AbstractMultiCriterion criterion);

    T visitOrCriterion(AbstractMultiCriterion criterion);

    T visitNotCriterion(NotCriterion criterion);

    T visitStringMatchCriterion(StringMatchCriterion criterion);

    T visitStringMultiMatchCriterion(StringMultiMatchCriterion criterion);

    T visitStringMatchAnyCriterion(StringMatchAnyCriterion criterion);

    T visitIntMatchCriterion(IntMatchCriterion criterion);

    T visitLongMatchCriterion(LongMatchCriterion criterion);

    T visitDateMatchCriterion(DateMatchCriterion criterion);

    <U extends Comparable<? super U>> T visitRangeCriterion(RangeCriterion<U> criterion);

    T visitDateRangeCriterion(DateRangeCriterion criterion);

    T visitBooleanMatchCriterion(BooleanMatchCriterion criterion);

    T visitPolygonCriterion(PolygonCriterion criterion);

    T visitBoundaryBoxCriterion(BoundaryBoxCriterion criterion);

    T visitCircleCriterion(CircleCriterion criterion);

    T visitFieldExistsCriterion(FieldExistsCriterion criterion);
}
