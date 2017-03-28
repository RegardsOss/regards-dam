/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;
import fr.cnes.regards.modules.entities.domain.Dataset;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public interface IAccessRightRepository extends JpaRepository<AccessRight, Long> {

    /**
     * @param pDs
     * @param pPageable
     * @return
     */
    Page<AccessRight> findAllByDataset(Dataset pDs, Pageable pPageable);

    /**
     * @param pAg1
     * @param pPageable
     * @return
     */
    Page<AccessRight> findAllByAccessGroup(AccessGroup pAg1, Pageable pPageable);

    /**
     * @param pAg1
     * @param pDs1
     * @param pPageable
     * @return
     */
    Page<AccessRight> findAllByAccessGroupAndDataset(AccessGroup pAg1, Dataset pDs1, Pageable pPageable);

}
