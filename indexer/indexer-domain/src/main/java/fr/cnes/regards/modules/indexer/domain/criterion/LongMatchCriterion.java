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

/**
 * Long specialized AbstractMatchCriterion.<br/>
 * <b>Only MatchType.EQUALS is allowed with Long type
 * @author oroussel
 */
public class LongMatchCriterion extends AbstractMatchCriterion<Long> {

    public LongMatchCriterion(String name, long value) {
        super(name, MatchType.EQUALS, value);

    }

    @Override
    public LongMatchCriterion copy() {
        return new LongMatchCriterion(super.name, super.value);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitLongMatchCriterion(this);
    }

}
