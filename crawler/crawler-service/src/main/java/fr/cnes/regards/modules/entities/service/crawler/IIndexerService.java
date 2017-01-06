package fr.cnes.regards.modules.entities.service.crawler;

import java.util.Map;

import fr.cnes.regards.modules.crawler.domain.IIndexable;

public interface IIndexerService {

    boolean createIndex(String pIndex);

    boolean saveEntity(String pIndex, IIndexable pEntity);

    Map<String, Throwable> saveBulkEntities(String pIndex, IIndexable... pEntities);

    boolean deleteEntity(String index, IIndexable pEntity);
}
