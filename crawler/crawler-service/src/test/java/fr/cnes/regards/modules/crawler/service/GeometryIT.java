package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.geojson.GeoJsonType;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.geojson.geometry.LineString;
import fr.cnes.regards.framework.geojson.geometry.MultiLineString;
import fr.cnes.regards.framework.geojson.geometry.MultiPoint;
import fr.cnes.regards.framework.geojson.geometry.MultiPolygon;
import fr.cnes.regards.framework.geojson.geometry.Point;
import fr.cnes.regards.framework.geojson.geometry.Polygon;
import fr.cnes.regards.framework.geojson.geometry.Unlocated;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.crawler.test.CrawlerConfiguration;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.gson.MultitenantFlattenedAttributeAdapterFactoryEventHandler;
import fr.cnes.regards.modules.entities.service.ICollectionService;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@ActiveProfiles("noschedule") // Disable scheduling, this will activate IngesterService during all tests
@TestPropertySource(locations = { "classpath:test.properties" })
public class GeometryIT {

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactoryEventHandler gsonAttributeFactoryHandler;

    @Autowired
    private ICollectionService collService;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity<?>> entityRepos;

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

    @Before
    public void init() throws ModuleException {
        collectionModel = new Model();
        collectionModel.setName("model_1" + System.currentTimeMillis());
        collectionModel.setType(EntityType.COLLECTION);
        collectionModel.setVersion("1");
        collectionModel.setDescription("Test data object model");
        modelService.createModel(collectionModel);
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

        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL", "collection with geometry");
        collection.setGeometry(IGeometry.point(IGeometry.position(41.12, -71.34)));
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getGeometry().getType() == GeoJsonType.POINT);
        Assert.assertTrue(collFromDB.getGeometry() instanceof Point);

        Point point = (Point) collFromDB.getGeometry();
        Assert.assertEquals(IGeometry.position(41.12, -71.34), point.getCoordinates());
    }

    @Test
    public void testOnEsPoint() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL", "collection with geometry");
        collection.setGeometry(IGeometry.point(IGeometry.position(41.12, -71.34)));

        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getGeometry().getType() == GeoJsonType.POINT);
        Point point = (Point) collFromEs.getGeometry();
        Assert.assertEquals(IGeometry.position(41.12, -71.34), point.getCoordinates());
    }

    @Test
    public void testOnDbMultiPointLineString() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        MultiPoint multipoint = IGeometry.multiPoint(IGeometry.position(41.12, -71.34), IGeometry.position(42., -72.));
        collection.setGeometry(multipoint);
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getGeometry().getType() == GeoJsonType.MULTIPOINT);
        Assert.assertTrue(collFromDB.getGeometry() instanceof MultiPoint);
        multipoint = (MultiPoint) collFromDB.getGeometry();

        double[][] ref1 = { { 41.12, -71.34 }, { 42., -72. } };
        Assert.assertArrayEquals(ref1, multipoint.getCoordinates().toArray());

        collection2 = new Collection(collectionModel, TENANT, "COL2", "another collection with geometry");
        LineString lineString = IGeometry.lineString(IGeometry
                .toLineStringCoordinates(IGeometry.position(41.12, -71.34), IGeometry.position(42., -72.)));
        collection2.setGeometry(lineString);
        collService.create(collection2);

        final Collection coll2FromDB = collService.load(collection2.getId());
        Assert.assertTrue(coll2FromDB.getGeometry().getType() == GeoJsonType.LINESTRING);
        Assert.assertTrue(coll2FromDB.getGeometry() instanceof LineString);
        lineString = (LineString) coll2FromDB.getGeometry();
        Assert.assertArrayEquals(ref1, lineString.getCoordinates().toArray());

    }

    @Test
    public void testOnEsMultiPointLineString() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        MultiPoint multipoint = IGeometry.multiPoint(IGeometry.position(41.12, -71.34), IGeometry.position(42., -72.));
        collection.setGeometry(multipoint);

        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getGeometry().getType() == GeoJsonType.MULTIPOINT);
        multipoint = collFromEs.getGeometry();

        double[][] ref1 = { { 41.12, -71.34 }, { 42., -72. } };
        Assert.assertArrayEquals(ref1, multipoint.getCoordinates().toArray());

        collection2 = new Collection(collectionModel, TENANT, "COL2", "another collection with geometry");
        LineString lineString = IGeometry.lineString(IGeometry
                .toLineStringCoordinates(IGeometry.position(41.12, -71.34), IGeometry.position(42., -72.)));
        collection2.setGeometry(lineString);

        esRepos.save(TENANT, collection2);
        esRepos.refresh(TENANT);

        final Collection coll2FromEs = esRepos.get(TENANT, collection2);
        Assert.assertTrue(coll2FromEs.getGeometry().getType() == GeoJsonType.LINESTRING);
        lineString = (LineString) coll2FromEs.getGeometry();
        Assert.assertArrayEquals(ref1, lineString.getCoordinates().toArray());

    }

    @Test
    public void testOnDbMultiLineStringPolygon() throws ModuleException, IOException {

        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        MultiLineString geometry = IGeometry
                .multiLineString(IGeometry.toLineStringCoordinates(IGeometry.position(41.12, -71.34),
                                                                   IGeometry.position(42., -72.)),
                                 IGeometry.toLineStringCoordinates(IGeometry.position(39.12, -70.34),
                                                                   IGeometry.position(38., -70.)));
        collection.setGeometry(geometry);
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getGeometry().getType() == GeoJsonType.MULTILINESTRING);
        Assert.assertTrue(collFromDB.getGeometry() instanceof MultiLineString);
        geometry = collFromDB.getGeometry();

        double[][] ref1 = { { 41.12, -71.34 }, { 42., -72. } };
        Assert.assertArrayEquals(ref1, geometry.getCoordinates().get(0).toArray());
        double[][] ref2 = { { 39.12, -70.34 }, { 38., -70. } };
        Assert.assertArrayEquals(ref2, geometry.getCoordinates().get(1).toArray());
    }

    @Test
    public void testOnEsMultiLineStringPolygon() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        collection.setGeometry(IGeometry
                .multiLineString(IGeometry.toLineStringCoordinates(IGeometry.position(41.12, -71.34),
                                                                   IGeometry.position(42., -72.)),
                                 IGeometry.toLineStringCoordinates(IGeometry.position(39.12, -70.34),
                                                                   IGeometry.position(38., -70.))));
        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getGeometry().getType() == GeoJsonType.MULTILINESTRING);

        collection2 = new Collection(collectionModel, TENANT, "COL2", "another collection with geometry");
        // Polygon with hole defined using http://geojson.io
        Polygon polygon = IGeometry
                .polygon(IGeometry.toPolygonCoordinates(IGeometry
                        .toLinearRingCoordinates(IGeometry.position(1.4522552490234373, 43.62365009386727),
                                                 IGeometry.position(1.4556884765625, 43.57641143300888),
                                                 IGeometry.position(1.5250396728515625, 43.57641143300888),
                                                 IGeometry.position(1.531219482421875, 43.62215891380659),
                                                 IGeometry.position(1.4522552490234373, 43.62365009386727)),
                                                        IGeometry
                                                                .toLinearRingCoordinates(IGeometry
                                                                        .position(1.47216796875, 43.608736628843445),
                                                                                         IGeometry
                                                                                                 .position(1.4728546142578125,
                                                                                                           43.58735421230633),
                                                                                         IGeometry
                                                                                                 .position(1.50787353515625,
                                                                                                           43.58735421230633),
                                                                                         IGeometry
                                                                                                 .position(1.5085601806640625,
                                                                                                           43.60823944964323),
                                                                                         IGeometry
                                                                                                 .position(1.47216796875,
                                                                                                           43.608736628843445))));
        collection2.setGeometry(polygon);

        esRepos.save(TENANT, collection2);
        esRepos.refresh(TENANT);

        final Collection coll2FromEs = esRepos.get(TENANT, collection2);
        Assert.assertTrue(coll2FromEs.getGeometry().getType() == GeoJsonType.POLYGON);
        Assert.assertTrue(coll2FromEs.getGeometry() instanceof Polygon);
    }

    @Test
    public void testOnDbMultiPolygon() throws ModuleException, IOException {

        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        MultiPolygon mp = IGeometry.multiPolygon(IGeometry.toPolygonCoordinates(IGeometry
                .toLinearRingCoordinates(IGeometry.position(102.0, 2.0), IGeometry.position(103.0, 2.0),
                                         IGeometry.position(103.0, 3.0), IGeometry.position(102.0, 3.0),
                                         IGeometry.position(102.0, 2.0))),
                                                 IGeometry.toPolygonCoordinates(IGeometry
                                                         .toLinearRingCoordinates(IGeometry.position(100.0, 0.0),
                                                                                  IGeometry.position(101.0, 0.0),
                                                                                  IGeometry.position(101.0, 1.0),
                                                                                  IGeometry.position(100.0, 1.0),
                                                                                  IGeometry.position(100.0, 0.0))));
        collection.setGeometry(mp);
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getGeometry().getType() == GeoJsonType.MULTIPOLYGON);
        Assert.assertTrue(collFromDB.getGeometry() instanceof MultiPolygon);
    }

    @Test
    public void testOnEsMultiPolygon() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        MultiPolygon mp = IGeometry.multiPolygon(IGeometry.toPolygonCoordinates(IGeometry
                .toLinearRingCoordinates(IGeometry.position(102.0, 2.0), IGeometry.position(103.0, 2.0),
                                         IGeometry.position(103.0, 3.0), IGeometry.position(102.0, 3.0),
                                         IGeometry.position(102.0, 2.0))),
                                                 IGeometry.toPolygonCoordinates(IGeometry
                                                         .toLinearRingCoordinates(IGeometry.position(100.0, 0.0),
                                                                                  IGeometry.position(101.0, 0.0),
                                                                                  IGeometry.position(101.0, 1.0),
                                                                                  IGeometry.position(100.0, 1.0),
                                                                                  IGeometry.position(100.0, 0.0))));
        collection.setGeometry(mp);

        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getGeometry().getType() == GeoJsonType.MULTIPOLYGON);
    }

    @Test
    public void testNoGeometry() throws ModuleException, IOException {

        collection = new Collection(collectionModel, TENANT, "COL1", "collection without geometry");

        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getGeometry() instanceof Unlocated);

        // Index creation with geometry mapping
        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getGeometry() instanceof Unlocated);
    }
}
