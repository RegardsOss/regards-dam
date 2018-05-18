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
package fr.cnes.regards.modules.entities.dao;

import javax.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.collect.Sets;

/**
 * JPA {@link Specification} to define {@link Predicate}s for criteria search for {@link SIPSession} from repository.
 * @author Sébastien Binda
 */
public class EntitySpecifications<E> {

    private static final String LIKE_CHAR = "%";

    /**
     * Filter on the given attributes (sessionId, owner, ingestDate and state) and return result ordered by descending ingestDate
     * @param id {@link String}
     * @param from {@link OffsetDateTime}
     * @param to {@link OffsetDateTime}
     * @return
     */
    public Specification<E> search(String label) {
        return (root, query, cb) -> {
            Set<Predicate> predicates = Sets.newHashSet();
            if (label != null) {
                predicates.add(cb.like(root.get("label"), LIKE_CHAR + label + LIKE_CHAR));
            }
            query.orderBy(cb.desc(root.get("label")));
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

}
