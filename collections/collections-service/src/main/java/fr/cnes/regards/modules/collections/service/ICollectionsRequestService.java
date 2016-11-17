/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.modules.collections.domain.Collection;

/**
 * @author lmieulet
 *
 */
public interface ICollectionsRequestService {

    /**
     *
     * @param pModelId
     *            model id to get a restricted list of collection respecting the expected model, null if you want all
     *
     * @return list of collection respecting, or not, a model
     */
    public List<Collection> retrieveCollectionList(Long pModelId);

    /**
     * @param pCollectionId
     *            collection Id wanted
     * @return one collection
     */
    public Collection retrieveCollectionById(Long pCollectionId);

    /**
     * @param pCollection
     *            collection containning changes
     * @param pCollectionId
     *            id of collection to change
     * @return changed collection
     * @throws OperationNotSupportedException
     */
    public Collection updateCollection(Collection pCollection, Long pCollectionId)
            throws EntityInconsistentIdentifierException;

    /**
     * @param pCollectionId
     * @return
     */
    public void deleteCollection(Long pCollectionId);

    /**
     * @param pCollection
     * @return
     */
    public Collection createCollection(Collection pCollection);

}
