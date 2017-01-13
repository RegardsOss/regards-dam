package fr.cnes.regards.modules.crawler.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.crawler.dao.IEsRepository;
import fr.cnes.regards.modules.crawler.domain.IIndexable;

@Service
public class IndexerService implements IIndexerService {

    private IEsRepository repository;

    public IndexerService(IEsRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean createIndex(String pIndex) {
        return repository.createIndex(pIndex);
    }

    @Override
    public boolean deleteIndex(String pIndex) {
        return repository.deleteIndex(pIndex);
    }

    @Override
    public boolean saveEntity(String pIndex, IIndexable pEntity) {
        return repository.save(pIndex, pEntity);
    }

    @Override
    public Map<String, Throwable> saveBulkEntities(String pIndex, IIndexable... pEntities) {
        return repository.saveBulk(pIndex, pEntities);
    }

    @Override
    public boolean deleteEntity(String pIndex, IIndexable pEntity) {
        return repository.delete(pIndex, pEntity);
    }

}
