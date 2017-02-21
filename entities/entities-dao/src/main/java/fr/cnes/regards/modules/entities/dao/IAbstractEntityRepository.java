/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public interface IAbstractEntityRepository<T extends AbstractEntity> extends JpaRepository<T, Long> {

    /**
     * Load entity with tags (eager) and groups
     * @param pId id of entity
     * @return entity
     */
    @EntityGraph(attributePaths = { "tags", "groups" })
    T findById(Long pId);

    /**
     * Find all entities of which ipId belongs to given set
     * @param pIpIds set of ipId
     * @return found entities
     */
    List<AbstractEntity> findByIpIdIn(Set<UniformResourceName> pIpIds);

    /**
     * Find entity of which given ipId
     * @param pIpId ipId of which entity
     * @return found entity
     */
    AbstractEntity findOneByIpId(UniformResourceName pIpId);

    /**
     * Find all entities containing given tag
     * @param pTagToSearch tag to search entities for
     * @return entities which contain given tag
     */
    List<AbstractEntity> findByTags(String pTagToSearch);

}
