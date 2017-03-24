/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.indexer.service;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.models.domain.EntityType;

@Service
public class IndexerService implements IIndexerService {

    @Autowired
    private IEsRepository repository;

    @Override
    public boolean createIndex(String pIndex) {
        if (!repository.indexExists(pIndex)) {
            boolean created = repository.createIndex(pIndex);
            if (created) {
                repository.setGeometryMapping(pIndex, Arrays.stream(EntityType.values()).map(EntityType::toString)
                        .toArray(length -> new String[length]));
            }
            return created;
        }
        return true;
    }

    @Override
    public boolean deleteIndex(String pIndex) {
        if (repository.indexExists(pIndex)) {
            return repository.deleteIndex(pIndex);
        }
        return true;
    }

    @Override
    public boolean indexExists(String pIndex) {
        return repository.indexExists(pIndex);
    }

    @Override
    public boolean saveEntity(String pIndex, IIndexable pEntity) {
        return repository.save(pIndex, pEntity);
    }

    @Override
    public void refresh(String pIndex) {
        repository.refresh(pIndex);
    }

    @Override
    public int saveBulkEntities(String pIndex, IIndexable... pEntities) {
        return repository.saveBulk(pIndex, pEntities);
    }

    @Override
    public int saveBulkEntities(String pIndex, Collection<? extends IIndexable> pEntities) {
        return repository.saveBulk(pIndex, pEntities);
    }

    @Override
    public boolean deleteEntity(String pIndex, IIndexable pEntity) {
        return repository.delete(pIndex, pEntity);
    }
}