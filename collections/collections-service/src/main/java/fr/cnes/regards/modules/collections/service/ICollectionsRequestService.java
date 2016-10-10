/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import fr.cnes.regards.modules.collections.domain.Collection;

/**
 * @author lmieulet
 *
 */
public interface ICollectionsRequestService {

    /**
     *
     * @return
     */
    public List<Collection> retrieveCollectionList();

    /**
     *
     * @param pModelId
     * @return
     */
    public List<Collection> retrieveCollectionListByModelId(Long pModelId);

    /**
     * @param pCollectionId
     */
    public Collection retrieveCollectionById(String pCollectionId);

    /**
     * @param pCollection
     * @param pCollectionId
     * @return
     * @throws OperationNotSupportedException
     */
    public Collection updateCollection(Collection pCollection, String pCollectionId)
            throws OperationNotSupportedException;

    /**
     * @param pCollectionId
     * @return
     */
    public void deleteCollection(String pCollectionId);

    /**
     * @param pCollection
     * @return
     */
    public Collection createCollection(Collection pCollection);

}
