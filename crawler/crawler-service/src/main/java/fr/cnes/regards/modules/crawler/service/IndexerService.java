/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.service;

import java.util.Collection;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.crawler.dao.IEsRepository;
import fr.cnes.regards.modules.crawler.domain.IIndexable;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

@Service
public class IndexerService implements IIndexerService {

    private final IEsRepository repository;

    public IndexerService(IEsRepository repository) {
        this.repository = repository;
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IIndexable> T get(UniformResourceName urn) {
        Class<? extends IIndexable> clazz;
        switch (urn.getEntityType()) {
            case COLLECTION:
                clazz = fr.cnes.regards.modules.entities.domain.Collection.class;
                break;
            case DATA:
                clazz = DataObject.class;
                break;
            case DATASET:
                clazz = Dataset.class;
                break;
            case DOCUMENT:
                clazz = Document.class;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return (T) repository.get(urn.getTenant(), urn.getEntityType().toString(), urn.toString(), clazz);
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
    public Map<String, Throwable> saveBulkEntities(String pIndex, IIndexable... pEntities) {
        return repository.saveBulk(pIndex, pEntities);
    }

    @Override
    public Map<String, Throwable> saveBulkEntities(String pIndex, Collection<? extends IIndexable> pEntities) {
        return repository.saveBulk(pIndex, pEntities);
    }

    @Override
    public <T> Page<T> search(String pIndex, Class<T> pClass, int pPageSize, ICriterion criterion) {
        return repository.search(pIndex, pClass, pPageSize, criterion);
    }

    @Override
    public <T> Page<T> search(String pIndex, Class<T> pClass, Pageable pPageRequest, ICriterion criterion) {
        return repository.search(pIndex, pClass, pPageRequest, criterion);
    }

    @Override
    public boolean deleteEntity(String pIndex, IIndexable pEntity) {
        return repository.delete(pIndex, pEntity);
    }
}
