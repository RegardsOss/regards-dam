/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.models.domain.EntityType;

@Service
public class IndexerService implements IIndexerService {

    @Autowired
    private IEsRepository repository;

    private static final BiMap<EntityType, Class<? extends AbstractEntity>> TYPE_MAP = EnumHashBiMap
            .create(EntityType.class);
    static {
        TYPE_MAP.put(EntityType.COLLECTION, fr.cnes.regards.modules.entities.domain.Collection.class);
        TYPE_MAP.put(EntityType.DATASET, Dataset.class);
        TYPE_MAP.put(EntityType.DATA, DataObject.class);
        TYPE_MAP.put(EntityType.DOCUMENT, Document.class);
    }

    @Override
    public boolean createIndex(String pIndex) {
        if (!repository.indexExists(pIndex)) {
            return repository.createIndex(pIndex);
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