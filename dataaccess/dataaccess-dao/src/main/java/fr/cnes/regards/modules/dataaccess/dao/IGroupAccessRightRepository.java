/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessright.GroupAccessRight;
import fr.cnes.regards.modules.entities.domain.DataSet;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public interface IGroupAccessRightRepository extends IAccessRightRepository<GroupAccessRight> {

    /**
     * @param pAccessGroupName
     * @param pPageable
     * @return
     */
    Page<GroupAccessRight> findAllByAccessGroup(AccessGroup pAccessGroup, Pageable pPageable);

    /**
     * @param pAccessGroupName
     * @param pDs
     * @param pPageable
     * @return
     */
    Page<GroupAccessRight> findAllByAccessGroupAndDataSet(AccessGroup pAccessGroup, DataSet pDs, Pageable pPageable);

}
