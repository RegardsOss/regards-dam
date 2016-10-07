/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.collections.dao.ICollectionRepository;
import fr.cnes.regards.modules.collections.domain.Collection;

/**
 * @author lmieulet
 *
 */
@Service
public class CollectionsRequestService implements ICollectionsRequestService {

    private final ICollectionRepository collectionRepository_;

    /**
     * @param pCollectionRepository
     */
    public CollectionsRequestService(ICollectionRepository pCollectionRepository) {
        super();
        collectionRepository_ = pCollectionRepository;
    }

    @Override
    public List<Collection> retrieveCollectionList() {
        Iterable<Collection> collections = collectionRepository_.findAll();
        return StreamSupport.stream(collections.spliterator(), true).collect(Collectors.toList());
    }

    @Override
    public List<Collection> retrieveCollectionListByModelId(Long pModelId) {
        Iterable<Collection> collections = collectionRepository_.findAllByModelId(pModelId);
        return StreamSupport.stream(collections.spliterator(), true).collect(Collectors.toList());
    }

}
