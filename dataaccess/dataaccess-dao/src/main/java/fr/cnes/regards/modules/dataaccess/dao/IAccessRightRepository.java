/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;
import fr.cnes.regards.modules.dataset.domain.DataSet;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public interface IAccessRightRepository<T extends AbstractAccessRight>
        extends JpaRepository<AbstractAccessRight, Long> {

    /**
     * @param pDs
     * @param pPageable
     * @return
     */
    Page<T> findAllByDataSet(DataSet pDs, Pageable pPageable);

}
