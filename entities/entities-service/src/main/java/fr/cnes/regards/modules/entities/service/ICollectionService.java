/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.List;

import fr.cnes.regards.modules.entities.domain.Collection;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 */
public interface ICollectionService extends IEntityService {

    /**
     * @return all {@link Collection}s
     */
    public List<Collection> retrieveCollectionList();

    /**
     * @param pCollectionId
     *            collection Id wanted
     * @return one {@link Collection}
     */
    public Collection retrieveCollectionById(Long pCollectionId);

    /**
     *
     * @param pCollectionIpId
     *            Ip id of the {@link Collection} requested
     * @return requested {@link Collection}
     */
    public Collection retrieveCollectionByIpId(String pCollectionIpId);

}
