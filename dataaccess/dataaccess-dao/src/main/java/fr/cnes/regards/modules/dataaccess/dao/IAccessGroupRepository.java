/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.dao;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;

/**
 *
 * Repository handling AccessGroup
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public interface IAccessGroupRepository extends JpaRepository<AccessGroup, Long> {

    /**
     * @param pName
     * @return
     */
    AccessGroup findOneByName(String pName);

    /**
     * @param pUserEmail
     * @param pPageable
     * @return
     */
    Page<AccessGroup> findAllByUsers(User pUser, Pageable pPageable);

    /**
     * @param pUserEmail
     * @return
     */
    Set<AccessGroup> findAllByUsers(User pUser);

    /**
     * @param pUser
     * @param pFalse
     * @param pPageable
     * @return
     */
    Page<AccessGroup> findAllByUsersOrIsPrivate(User pUser, Boolean pFalse, Pageable pPageable);

}
