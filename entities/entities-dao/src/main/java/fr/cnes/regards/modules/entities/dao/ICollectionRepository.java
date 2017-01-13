/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.entities.domain.Collection;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Repository
public interface ICollectionRepository extends IAbstractEntityRepository<Collection> {

    /**
     * @param pCollectionIpId
     *            ip id of the {@link Collection} to delete
     */
    void deleteByIpId(String pCollectionIpId);

    /**
     * @param pCollectionIpId
     *            Ip id of the requested {@link Collection}
     * @return requested {@link Collection}
     */
    Collection findOneByIpId(String pCollectionIpId);

}