/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.dao;

import org.springframework.data.repository.CrudRepository;

import fr.cnes.regards.modules.collections.domain.Collection;

/**
 * @author lmieulet
 *
 */
public interface ICollectionRepository extends CrudRepository<Collection, Long> {

    /**
     * @param pModelId
     * @return
     */
    Iterable<Collection> findAllByModelId(Long pModelId);

}
