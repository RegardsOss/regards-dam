package fr.cnes.regards.modules.entities.service.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.crawler.dao.IEsRepository;
import fr.cnes.regards.modules.crawler.domain.IIndexable;

@Service
public class IndexerService implements IIndexerService {

    @Autowired
    private IEsRepository repository;

    @Override
    public void createIndex(String pIndex) {
    }

    @Override
    public void saveEntity(String pIndex, IIndexable pEntity) {
    }

}
