/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.modules.collections.dao.ICollectionRepository;
import fr.cnes.regards.modules.collections.domain.Collection;

/**
 * @author lmieulet
 *
 */
@Service
public class CollectionsRequestService implements ICollectionsRequestService {

    /**
     * DAO autowired by Spring
     */
    private final ICollectionRepository collectionRepository;

    /**
     *
     * @param pCollectionRepository
     *            repository used by service to provide data
     */
    public CollectionsRequestService(ICollectionRepository pCollectionRepository) {
        super();
        collectionRepository = pCollectionRepository;
    }

    @Override
    public List<Collection> retrieveCollectionList(Long pModelId) {
        final Iterable<Collection> collections = collectionRepository.findAll();
        if (pModelId == null) {
            return StreamSupport.stream(collections.spliterator(), true).collect(Collectors.toList());
        } else {
            return retrieveCollectionListByModelId(pModelId);
        }
    }

    // TODO: reintegrate
    private List<Collection> retrieveCollectionListByModelId(Long pModelId) {
        // final Iterable<Collection> collections = collectionRepository.findAllByModelId(pModelId);
        // return StreamSupport.stream(collections.spliterator(), true).collect(Collectors.toList());
        return null;
    }

    @Override
    public Collection retrieveCollectionById(Long pCollectionId) {
        return collectionRepository.findOne(pCollectionId);
    }

    @Override
    public Collection updateCollection(Collection pCollection, Long pCollectionId)
            throws EntityInconsistentIdentifierException {
        // Check if exist
        final Collection collectionBeforeUpdate = collectionRepository.findOne(pCollectionId);
        if (!collectionBeforeUpdate.getId().equals(pCollection.getId())) {
            // "collectionId inside the route is different from the enity inside the request body"
            throw new EntityInconsistentIdentifierException(pCollectionId, pCollection.getId(), Collection.class);
        }
        final Collection collectionAfterUpdate = collectionRepository.save(pCollection);
        return collectionAfterUpdate;
    }

    @Override
    public void deleteCollection(Long pCollectionId) {
        collectionRepository.delete(pCollectionId);
    }

    @Override
    public Collection createCollection(Collection pCollection) {
        return collectionRepository.save(pCollection);
    }

}
