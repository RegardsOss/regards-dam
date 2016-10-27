/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.naming.OperationNotSupportedException;

import org.springframework.stereotype.Service;

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
     */
    public CollectionsRequestService(ICollectionRepository pCollectionRepository) {
        super();
        collectionRepository = pCollectionRepository;
    }

    @Override
    public List<Collection> retrieveCollectionList() {
        Iterable<Collection> collections = collectionRepository.findAll();
        return StreamSupport.stream(collections.spliterator(), true).collect(Collectors.toList());
    }

    @Override
    public List<Collection> retrieveCollectionListByModelId(Long pModelId) {
        Iterable<Collection> collections = collectionRepository.findAllByModelId(pModelId);
        return StreamSupport.stream(collections.spliterator(), true).collect(Collectors.toList());
    }

    @Override
    public Collection retrieveCollectionById(String pCollectionId) {
        return collectionRepository.findOne(pCollectionId);
    }

    @Override
    public Collection updateCollection(Collection pCollection, String pCollectionId)
            throws OperationNotSupportedException {
        // Check if exist
        Collection collectionBeforeUpdate = collectionRepository.findOne(pCollectionId);
        if (!collectionBeforeUpdate.getId().equals(pCollection.getId())) {
            throw new OperationNotSupportedException(
                    "collectionId inside the route is different from the enity inside the request body");
        }
        Collection collectionAfterUpdate = collectionRepository.save(pCollection);
        return collectionAfterUpdate;
    }

    @Override
    public void deleteCollection(String pCollectionId) {
        collectionRepository.delete(pCollectionId);
    }

    @Override
    public Collection createCollection(Collection pCollection) {
        return collectionRepository.save(pCollection);
    }

}
