/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.repository;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * Extension of {@link PagingAndSortingRepository} and provide additional methods to retrieve entities using
 * {@link Restrictions}.
 *
 * @param <T>
 *            Entity managed
 * 
 * @author Christophe Mertz
 */
public interface IEntityPagingAndSortingRepository<T extends AbstractEntity>
        extends PagingAndSortingRepository<T, Long>, JpaSpecificationExecutor<T> {

    /**
     * Returns all instances of the type.
     *
     * @param pCondition
     *            SQL expression (i.e. where clause)
     * @return all entities
     */
    List<T> findBy(Restrictions pCondition);

    /**
     * Returns all entities sorted by the given options.
     *
     * @param pCondition
     *            SQL expression (i.e. where clause)
     * @param pSort
     *            sort clause
     * @return all entities sorted by the given options
     */
    List<T> findBy(Restrictions pCondition, Sort pSort);

    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
     *
     * @param pCondition
     *            SQL expression (i.e. where clause)
     * @param pPageable
     *            pagination clause
     * @return a page of entities
     */
    Page<T> findBy(Restrictions pCondition, Pageable pPageable);

}