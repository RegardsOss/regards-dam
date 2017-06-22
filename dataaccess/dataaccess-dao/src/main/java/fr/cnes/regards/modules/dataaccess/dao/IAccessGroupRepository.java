/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.dao;

import javax.persistence.NamedEntityGraph;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    AccessGroup findOneByName(String pName);

    Page<AccessGroup> findAllByUsers(User pUser, Pageable pPageable);

    @EntityGraph(value = "graph.accessgroup.users")
    Set<AccessGroup> findAllByUsers(User pUser);

    Page<AccessGroup> findAllByUsersOrIsPublic(User pUser, Boolean pTrue, Pageable pPageable);

}
