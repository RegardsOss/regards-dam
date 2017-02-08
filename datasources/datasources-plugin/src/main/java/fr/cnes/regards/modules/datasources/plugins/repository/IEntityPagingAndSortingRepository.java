/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.repository;

import org.hibernate.criterion.Restrictions;
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
        extends PagingAndSortingRepository<T, Long>{

}