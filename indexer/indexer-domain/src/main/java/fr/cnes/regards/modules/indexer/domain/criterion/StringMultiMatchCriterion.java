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

import java.util.HashSet;
import java.util.Set;

import org.elasticsearch.index.query.MultiMatchQueryBuilder;

/**
 * String specialized {@link AbstractMultiMatchCriterion}
 * @author Marc SORDI
 */
public class StringMultiMatchCriterion extends AbstractMultiMatchCriterion<String> {

    public StringMultiMatchCriterion(Set<String> names, MultiMatchQueryBuilder.Type type, String value) {
        super(names, type, value);
    }

    @Override
    public ICriterion copy() {
        return new StringMultiMatchCriterion(new HashSet<>(names), type, value);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitStringMultiMatchCriterion(this);
    }

}
