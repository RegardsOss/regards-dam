/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.dataaccess.domain.accessright.UserAccessRight;
import fr.cnes.regards.modules.dataset.domain.DataSet;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public interface IUserAccessRightRepository extends IAccessRightRepository<UserAccessRight> {

    /**
     * @param pAccessGroupName
     * @param pPageable
     * @return
     */
    Page<UserAccessRight> findAllByUser(User pUser, Pageable pPageable);

    /**
     * @param pAccessGroupName
     * @param pDs
     * @param pPageable
     * @return
     */
    Page<UserAccessRight> findAllByUserAndDataSet(User pUser, DataSet pDs, Pageable pPageable);

}
