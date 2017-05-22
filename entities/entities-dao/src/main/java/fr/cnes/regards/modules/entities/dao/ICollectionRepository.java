/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Repository
public interface ICollectionRepository extends IAbstractEntityRepository<Collection> {

    List<Collection> findByGroups(String group);


    @Query("from Collection col left join fetch col.descriptionFile where col.id=:id")
    Collection findOneWithDescriptionFile(@Param("id") Long collectionId);


    /**
     * Find all collection of which ipId belongs to given set (eagerly loading all relations)
     *
     * @param pIpIds set of ipId
     * @return found collections
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model", "descriptionFile" })
    List<Collection> findByIpIdIn(Set<UniformResourceName> pIpIds);


    /**
     * Find collection of given IpId eagerly loading all common relations
     *
     * @param pIpId ipId of which entity
     * @return found entity
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model", "descriptionFile" })
    Collection findByIpId(UniformResourceName pIpId);
}
