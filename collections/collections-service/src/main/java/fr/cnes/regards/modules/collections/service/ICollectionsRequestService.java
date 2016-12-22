/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service;

import java.util.List;

import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * @author lmieulet
 *
 */
public interface ICollectionsRequestService {

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

    /**
     * @param pCollection
     *            collection containning changes
     * @param pCollectionId
     *            id of collection to change
     * @return changed collection
     * @throws EntityInconsistentIdentifierException
     *             thrown if pCollection's id and pCollectionId do not match
     * @throws EntityNotFoundException
     */
    public Collection updateCollection(Collection pCollection, Long pCollectionId)
            throws EntityInconsistentIdentifierException, EntityNotFoundException;

    /**
     * @param pCollectionId
     *            id of the {@link Collection} to delete
     */
    public void deleteCollection(Long pCollectionId);

    /**
     * @param pCollectionIpId
     *            Ip id of the {@link Collection} to delete
     */
    public void deleteCollection(String pCollectionIpId);

    /**
     * @param pCollection
     *            {@link Collection} to create
     * @return created {@link Collection}
     */
    public Collection createCollection(Collection pCollection);

    /**
     * @param pCollectionId
     *            id of the {@link Collection} we want to dissociate the list from
     * @param pToBeDissociated
     *            list of {@link AbstractEntity}s to be dissociate from the {@link Collection} with id pCollectionId
     *
     * @return {@link Collection} dissociated
     */
    public Collection dissociateCollection(Long pCollectionId, List<AbstractEntity> pToBeDissociated);

    /**
     * @param pCollectionId
     * @param pToBeAssociatedWith
     * @return
     */
    Collection associateCollection(Long pCollectionId, List<AbstractEntity> pToBeAssociatedWith);

}
