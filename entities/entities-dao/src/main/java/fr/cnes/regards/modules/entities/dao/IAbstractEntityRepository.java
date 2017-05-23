/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Common requests on entities
 *
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
public interface IAbstractEntityRepository<T extends AbstractEntity> extends JpaRepository<T, Long> {

    /**
     * Find entity giving its id eagerly loading its common relations (ie relations defined into AbstractEntity
     *
     * @param pId
     *            id of entity
     * @return entity
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    T findById(Long pId);

    /**
     * Find all entities of which ipId belongs to given set (eagerly loading all relations)
     *
     * @param pIpIds
     *            set of ipId
     * @return found entities
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    List<T> findByIpIdIn(Set<UniformResourceName> pIpIds);

    /**
     * Find entity of given ipId
     *
     * @param pIpId
     *            ipId of which entity
     * @return found entity
     */
    T findOneByIpId(UniformResourceName pIpId);

    /**
     * Find entity of given IpId eagerly loading all common relations
     *
     * @param pIpId
     *            ipId of which entity
     * @return found entity
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    T findByIpId(UniformResourceName pIpId);

    /**
     * Find all entities complient with the given modelName
     *
     * @param pModelName
     *            name of the model we want to be complient with
     * @return datasets complient with the given model
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    Set<T> findAllByModelName(String pModelName);

    /**
     * Find all entities complient with the given modelName
     *
     * @param pModelName
     *            name of the model we want to be complient with
     * @return datasets complient with the given model
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    Set<Dataset> findAllByModelId(Set<Long> pModelIds);

    /**
     * Find all entities containing given tag
     *
     * @param pTagToSearch
     *            tag to search entities for
     * @return entities which contain given tag
     */
    List<T> findByTags(String pTagToSearch);

}
