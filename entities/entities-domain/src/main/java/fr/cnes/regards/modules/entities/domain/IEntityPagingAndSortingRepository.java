/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * TODO: move to right place Entity interface.
 *
 * @param <T>
 *            Entity managed
 * @author msordi
 * @author Sylvain Vissiere-Guerinet
 * @since 1.0
 */
public interface IEntityPagingAndSortingRepository<T extends AbstractEntity>
        extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

}