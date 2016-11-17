/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * TODO: move to right place Entity interface.
 *
 * @param <T>
 *            Entity managed
 * @author msordi
 * @author Sylvain Vissiere-Guerinet
 * @since 1.0
 */
public interface IEntityPagingAndSortingRepository<T extends Entity>
        extends PagingAndSortingRepository<T, Integer>, JpaSpecificationExecutor<T> {

}