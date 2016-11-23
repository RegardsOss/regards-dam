/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.entities.domain.IEntityPagingAndSortingRepository;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Repository
public interface ICollectionRepository extends IEntityPagingAndSortingRepository<Collection> {

    /**
     * @param pModelId
     *            id of the model the collections should respect
     * @return list of Collection respecting a model
     */
    List<Collection> findByModelId(Long pModelId);

}
