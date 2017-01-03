package fr.cnes.regards.modules.entities.service.crawler;

import fr.cnes.regards.modules.crawler.domain.IIndexable;

public interface IIndexerService {

    void createIndex(String pIndex);

    void saveEntity(String pIndex, IIndexable pEntity);

    void saveBulkEntities(String pIndex, IIndexable... pEntities);

    void deleteEntity(String index, IIndexable pEntity);
}
