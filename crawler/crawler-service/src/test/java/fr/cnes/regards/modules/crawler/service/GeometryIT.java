package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.crawler.test.CrawlerConfiguration;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.geometry.Geometry;
import fr.cnes.regards.modules.entities.domain.geometry.GeometryType;
import fr.cnes.regards.modules.entities.gson.MultitenantFlattenedAttributeAdapterFactoryEventHandler;
import fr.cnes.regards.modules.entities.service.ICollectionService;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@ActiveProfiles("noschedule") // Disable scheduling, this will activate IngesterService during all tests
public class GeometryIT {

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactoryEventHandler gsonAttributeFactoryHandler;

    @Autowired
    private ICollectionService collService;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepos;

    @Autowired
    private IEsRepository esRepos;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private ICrawlerAndIngesterService crawlerService;

    private Model collectionModel;

    private Collection collection;

    private Collection collection2;

    private static final String TENANT = "GEOM";

    @PostConstruct
    public void initEs() {

        // Simulate spring boot ApplicationStarted event to start mapping for each tenants.
        gsonAttributeFactoryHandler.onApplicationEvent(null);

        tenantResolver.forceTenant(TENANT);

        if (esRepos.indexExists(TENANT)) {
            try {
                esRepos.deleteAll(TENANT);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else {
            esRepos.createIndex(TENANT);
        }
        esRepos.setGeometryMapping(TENANT, Arrays.stream(EntityType.values()).map(EntityType::toString)
                .toArray(length -> new String[length]));

        crawlerService.setConsumeOnlyMode(true);
    }

    @After
    public void clean() {
        // Don't use entity service to clean because events are published on RabbitMQ
        if (collection != null) {
            Utils.execute(entityRepos::delete, collection.getId());
        }
        if (collection2 != null) {
            Utils.execute(entityRepos::delete, collection2.getId());
        }
        if (collectionModel != null) {
            Utils.execute(modelService::deleteModel, collectionModel.getName());
        }
    }

    @Test
    public void testOnDbPoint() throws ModuleException, IOException {
        collectionModel = new Model();
        collectionModel.setName("model_1" + System.currentTimeMillis());
        collectionModel.setType(EntityType.COLLECTION);
        collectionModel.setVersion("1");
        collectionModel.setDescription("Test data object model");
        modelService.createModel(collectionModel);

        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "collection with geometry");
        collection.setGeometry(new Geometry.Point(new Double[] { 41.12, -71.34 }));
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getGeometry().getType() == GeometryType.POINT);
        Assert.assertTrue(collFromDB.getGeometry() instanceof Geometry.Point);
        Assert.assertArrayEquals(new Double[] { 41.12, -71.34 }, collFromDB.getGeometry().getCoordinates());

    }

    @Test
    public void testOnEsPoint() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "collection with geometry");
        collection.setGeometry(new Geometry.Point(new Double[] { 41.12, -71.34 }));

        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getGeometry().getType() == GeometryType.POINT);
        Assert.assertArrayEquals(new Double[] { 41.12, -71.34 }, collFromEs.getGeometry().getCoordinates());
    }

    @Test
    public void testOnDbMultiPointLineString() throws ModuleException, IOException {
        collectionModel = new Model();
        collectionModel.setName("model_1" + System.currentTimeMillis());
        collectionModel.setType(EntityType.COLLECTION);
        collectionModel.setVersion("1");
        collectionModel.setDescription("Test data object model");
        modelService.createModel(collectionModel);

        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "collection with geometry");
        final Geometry<?> geometry = new Geometry.MultiPoint(new Double[][] { { 41.12, -71.34 }, { 42., -72. } });
        collection.setGeometry(geometry);
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getGeometry().getType() == GeometryType.MULTI_POINT);
        Assert.assertTrue(collFromDB.getGeometry() instanceof Geometry.MultiPoint);
        Assert.assertArrayEquals(new Double[][] { { 41.12, -71.34 }, { 42., -72. } },
                                 collFromDB.getGeometry().getCoordinates());

        collection2 = new Collection(collectionModel, TENANT, "another collection with geometry");
        collection2.setGeometry(new Geometry.LineString(new Double[][] { { 41.12, -71.34 }, { 42., -72. } }));
        collService.create(collection2);

        final Collection coll2FromDB = collService.load(collection2.getId());
        Assert.assertTrue(coll2FromDB.getGeometry().getType() == GeometryType.LINE_STRING);
        Assert.assertTrue(coll2FromDB.getGeometry() instanceof Geometry.LineString);
        Assert.assertArrayEquals(new Double[][] { { 41.12, -71.34 }, { 42., -72. } },
                                 coll2FromDB.getGeometry().getCoordinates());

    }

    @Test
    public void testOnEsMultiPointLineString() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "collection with geometry");
        collection.setGeometry(new Geometry.MultiPoint(new Double[][] { { 41.12, -71.34 }, { 42., -72. } }));

        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getGeometry().getType() == GeometryType.MULTI_POINT);
        Assert.assertArrayEquals(new Double[][] { { 41.12, -71.34 }, { 42., -72. } },
                                 collFromEs.getGeometry().getCoordinates());

        collection2 = new Collection(collectionModel, TENANT, "another collection with geometry");
        collection2.setGeometry(new Geometry.LineString(new Double[][] { { 41.12, -71.34 }, { 42., -72. } }));

        esRepos.save(TENANT, collection2);
        esRepos.refresh(TENANT);

        final Collection coll2FromEs = esRepos.get(TENANT, collection2);
        Assert.assertTrue(coll2FromEs.getGeometry().getType() == GeometryType.LINE_STRING);
        Assert.assertArrayEquals(new Double[][] { { 41.12, -71.34 }, { 42., -72. } },
                                 coll2FromEs.getGeometry().getCoordinates());

    }

    @Test
    public void testOnDbMultiLineStringPolygon() throws ModuleException, IOException {
        collectionModel = new Model();
        collectionModel.setName("model_1" + System.currentTimeMillis());
        collectionModel.setType(EntityType.COLLECTION);
        collectionModel.setVersion("1");
        collectionModel.setDescription("Test data object model");
        modelService.createModel(collectionModel);

        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "collection with geometry");
        final Geometry<?> geometry = new Geometry.MultiLineString(
                new Double[][][] { { { 41.12, -71.34 }, { 42., -72. } }, { { 39.12, -70.34 }, { 38., -70. } } });
        collection.setGeometry(geometry);
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getGeometry().getType() == GeometryType.MULTI_LINE_STRING);
        Assert.assertTrue(collFromDB.getGeometry() instanceof Geometry.MultiLineString);
        Assert.assertArrayEquals(new Double[][][] { { { 41.12, -71.34 }, { 42., -72. } },
                { { 39.12, -70.34 }, { 38., -70. } } }, collFromDB.getGeometry().getCoordinates());

        collection2 = new Collection(collectionModel, TENANT, "another collection with geometry");
        collection2.setGeometry(new Geometry.Polygon(
                new Double[][][] { { { 41.12, -71.34 }, { 42., -72. } }, { { 39.12, -70.34 }, { 38., -70. } } }));
        collService.create(collection2);

        final Collection coll2FromDB = collService.load(collection2.getId());
        Assert.assertTrue(coll2FromDB.getGeometry().getType() == GeometryType.POLYGON);
        Assert.assertTrue(coll2FromDB.getGeometry() instanceof Geometry.Polygon);
        Assert.assertArrayEquals(new Double[][][] { { { 41.12, -71.34 }, { 42., -72. } },
                { { 39.12, -70.34 }, { 38., -70. } } }, coll2FromDB.getGeometry().getCoordinates());

    }

    @Test
    public void testOnEsMultiLineStringPolygon() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "collection with geometry");
        collection.setGeometry(new Geometry.MultiLineString(
                new Double[][][] { { { 41.12, -71.34 }, { 42., -72. } }, { { 39.12, -70.34 }, { 38., -70. } } }));

        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getGeometry().getType() == GeometryType.MULTI_LINE_STRING);
        Assert.assertArrayEquals(new Double[][][] { { { 41.12, -71.34 }, { 42., -72. } },
                { { 39.12, -70.34 }, { 38., -70. } } }, collFromEs.getGeometry().getCoordinates());

        collection2 = new Collection(collectionModel, TENANT, "another collection with geometry");
        // Polygon with hole defined using http://geojson.io
        final Double[][][] polygon = new Double[][][] {
                { { 1.4522552490234373, 43.62365009386727 }, { 1.4556884765625, 43.57641143300888 },
                        { 1.5250396728515625, 43.57641143300888 }, { 1.531219482421875, 43.62215891380659 },
                        { 1.4522552490234373, 43.62365009386727 } },
                { { 1.47216796875, 43.608736628843445 }, { 1.4728546142578125, 43.58735421230633 },
                        { 1.50787353515625, 43.58735421230633 }, { 1.5085601806640625, 43.60823944964323 },
                        { 1.47216796875, 43.608736628843445 } } };
        collection2.setGeometry(new Geometry.Polygon(polygon));

        esRepos.save(TENANT, collection2);
        esRepos.refresh(TENANT);

        final Collection coll2FromEs = esRepos.get(TENANT, collection2);
        Assert.assertTrue(coll2FromEs.getGeometry().getType() == GeometryType.POLYGON);
        Assert.assertArrayEquals(polygon, coll2FromEs.getGeometry().getCoordinates());

    }

    @Test
    public void testOnDbMultiPolygon() throws ModuleException, IOException {
        collectionModel = new Model();
        collectionModel.setName("model_1" + System.currentTimeMillis());
        collectionModel.setType(EntityType.COLLECTION);
        collectionModel.setVersion("1");
        collectionModel.setDescription("Test data object model");
        modelService.createModel(collectionModel);

        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "collection with geometry");
        final Double[][][][] multiPolygon = new Double[][][][] {
                { { { 102.0, 2.0 }, { 103.0, 2.0 }, { 103.0, 3.0 }, { 102.0, 3.0 }, { 102.0, 2.0 } } },
                { { { 100.0, 0.0 }, { 101.0, 0.0 }, { 101.0, 1.0 }, { 100.0, 1.0 }, { 100.0, 0.0 } },
                        { { 100.2, 0.2 }, { 100.2, 0.8 }, { 100.8, 0.8 }, { 100.8, 0.2 }, { 100.2, 0.2 } } } };
        final Geometry<?> geometry = new Geometry.MultiPolygon(multiPolygon);
        collection.setGeometry(geometry);
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getGeometry().getType() == GeometryType.MULTI_POLYGON);
        Assert.assertTrue(collFromDB.getGeometry() instanceof Geometry.MultiPolygon);
        Assert.assertArrayEquals(multiPolygon, collFromDB.getGeometry().getCoordinates());
    }

    @Test
    public void testOnEsMultiPolygon() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "collection with geometry");
        final Double[][][][] multiPolygon = new Double[][][][] {
                { { { 102.0, 2.0 }, { 103.0, 2.0 }, { 103.0, 3.0 }, { 102.0, 3.0 }, { 102.0, 2.0 } } },
                { { { 100.0, 0.0 }, { 101.0, 0.0 }, { 101.0, 1.0 }, { 100.0, 1.0 }, { 100.0, 0.0 } },
                        { { 100.2, 0.2 }, { 100.2, 0.8 }, { 100.8, 0.8 }, { 100.8, 0.2 }, { 100.2, 0.2 } } } };
        collection.setGeometry(new Geometry.MultiPolygon(multiPolygon));

        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getGeometry().getType() == GeometryType.MULTI_POLYGON);
        Assert.assertArrayEquals(multiPolygon, collFromEs.getGeometry().getCoordinates());
    }

    @Test
    public void testNoGeometry() throws ModuleException, IOException {
        collectionModel = new Model();
        collectionModel.setName("model_1" + System.currentTimeMillis());
        collectionModel.setType(EntityType.COLLECTION);
        collectionModel.setVersion("1");
        collectionModel.setDescription("Test data object model");
        modelService.createModel(collectionModel);
        collection = new Collection(collectionModel, TENANT, "collection without geometry");

        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertNull(collFromDB.getGeometry());

        // Index creation with geometry mapping
        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertNull(collFromEs.getGeometry());
    }
}
