package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.service.ICollectionService;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.service.IIndexerService;
import fr.cnes.regards.modules.indexer.service.ISearchService;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
public class GeometryIT {

    @Autowired
    private ICollectionService collService;

    @Autowired
    private IIndexerService indexerService;

    @Autowired
    private ISearchService searchService;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepos;

    @Autowired
    private IEsRepository esRepos;

    @Autowired
    private IModelService modelService;

    private Model collectionModel;

    private Collection collection;

    private static final String TENANT = "GEOM";

    @After
    public void clean() {
        // Don't use entity service to clean because events are published on RabbitMQ
        if (collection != null) {
            Utils.execute(entityRepos::delete, collection.getId());
        }
        if (collectionModel != null) {
            Utils.execute(modelService::deleteModel, collectionModel.getId());
        }
    }

    @Test
    public void test1() throws ModuleException, IOException {
        collectionModel = new Model();
        collectionModel.setName("model_1");
        collectionModel.setType(EntityType.COLLECTION);
        collectionModel.setVersion("1");
        collectionModel.setDescription("Test data object model");
        modelService.createModel(collectionModel);

        collection = new Collection(collectionModel, TENANT, "collection with geometry");
        collService.create(collection);

        if (esRepos.indexExists(TENANT)) {
            esRepos.deleteIndex(TENANT);
        }
        esRepos.createIndex(TENANT);
        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);
    }
}
