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
package fr.cnes.regards.modules.dam.dao.entities;

import java.util.Set;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.collect.Sets;

import fr.cnes.regards.modules.dam.domain.entities.StaticProperties;

/**
 * JPA {@link Specification} to define {@link Predicate}s for criteria search from repository.
 * @author Sébastien Binda
 * @param <E> Entity type to search for
 */
public class EntitySpecifications<E> {

    private static final String LIKE_CHAR = "%";

    /**
     * Filter on the given attributes (sessionId, owner, ingestDate and state) and return result ordered by descending
     * ingestDate
     * @param label Search label
     * @return {@link Specification}
     */
    public Specification<E> search(String label) {
        return (root, query, cb) -> {
            Set<Predicate> predicates = Sets.newHashSet();
            if (label != null) {
                predicates.add(cb
                        .like(cb.function("jsonb_extract_path_text", String.class, root.get(StaticProperties.FEATURE),
                                          cb.literal(StaticProperties.FEATURE_LABEL)),
                              LIKE_CHAR + label + LIKE_CHAR));
            }
            query.orderBy(cb
                    .asc(cb.function("jsonb_extract_path_text", String.class, root.get(StaticProperties.FEATURE),
                                     cb.literal(StaticProperties.FEATURE_LABEL))));
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

}
