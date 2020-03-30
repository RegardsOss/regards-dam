package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.geojson.geometry.Point;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.crawler.test.CrawlerConfiguration;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.service.Searches;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.gson.MultitenantFlattenedAttributeAdapterFactoryEventHandler;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@ActiveProfiles("noschedule") // Disable scheduling, this will activate IngesterService during all tests
@TestPropertySource(locations = { "classpath:test.properties" })
@DirtiesContext(hierarchyMode = HierarchyMode.EXHAUSTIVE, classMode = ClassMode.BEFORE_CLASS)
public class GeometrySearchIT {

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactoryEventHandler gsonAttributeFactoryHandler;

    @Autowired
    private IEsRepository esRepos;

    private static final String TENANT = "GEOM";

    private static final SimpleSearchKey<Collection> SEARCH_KEY = Searches.onSingleEntity(EntityType.COLLECTION);

    static {
        SEARCH_KEY.setSearchIndex(TENANT);
    }

    private Model collectionModel;

    @PostConstruct
    public void setUp() {

        // Simulate spring boot ApplicationStarted event to start mapping for each tenants.
        gsonAttributeFactoryHandler.onApplicationEvent(null);

        // Index creation with geometry mapping
        if (esRepos.indexExists(TENANT)) {
            esRepos.deleteAll(TENANT);
        } else {
            esRepos.createIndex(TENANT);
        }

    }

    @Before
    public void init() throws ModuleException {

        collectionModel = new Model();
        collectionModel.setName("model_1" + System.currentTimeMillis());
        collectionModel.setType(EntityType.COLLECTION);
        collectionModel.setVersion("1");
        collectionModel.setDescription("Test data object model");
    }

    @Test
    public void testCircleSearch() throws ModuleException, IOException {

        double[] b202 = new double[] { 1.4948514103889465, 43.577530672197476 };
        Point p202 = IGeometry.point(IGeometry.position(1.4948514103889465, 43.577530672197476));

        Point p259 = IGeometry.point(IGeometry.position(1.4948514103889465, 43.577614225677394));

        // Setting a geometry onto collection
        final Collection collectionOnB202 = new Collection(collectionModel, TENANT, "COLB202",
                "collection on b202 office room");
        collectionOnB202.setNormalizedGeometry(p202);
        collectionOnB202.setWgs84(p202);

        final Collection collectionOnB259 = new Collection(collectionModel, TENANT, "COLB100",
                "collection on b100 office room");
        collectionOnB259.setNormalizedGeometry(p259);
        collectionOnB259.setWgs84(p259);

        this.save(collectionOnB202, collectionOnB259);

        // 1m (default unit) circle on B202
        List<Collection> results = this.search(ICriterion.intersectsCircle(b202, "1"));
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(collectionOnB202, results.get(0));

        // 1m (unit specified) circle on B202
        results = this.search(ICriterion.intersectsCircle(b202, "1m"));
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(collectionOnB202, results.get(0));

        // 1km (unit specified) circle on B202
        results = this.search(ICriterion.intersectsCircle(b202, "1km"));
        Assert.assertEquals(2, results.size());
    }

    @Test
    public void testCircleSearchOnLimits() throws ModuleException, IOException {
        double[] northPole = new double[] { 0., 90. };
        Point northPolePoint = IGeometry.point(IGeometry.position(0., 90.));
        double[] nearWestNorthPole = new double[] { -5., 85. };
        Point nearWestNorthPolePoint = IGeometry.point(IGeometry.position(-5., 85.));
        double[] nearEastNorthPole = new double[] { 175., 85. };
        Point nearEastNorthPolePoint = IGeometry.point(IGeometry.position(175., 85.));

        final Collection collNorthPole = new Collection(collectionModel, TENANT, "COLNORTH", "North Pole");
        collNorthPole.setNormalizedGeometry(northPolePoint);
        collNorthPole.setWgs84(northPolePoint);

        final Collection collNearWestNorthPole = new Collection(collectionModel, TENANT, "COLWEST",
                "West near North Pole");
        collNearWestNorthPole.setNormalizedGeometry(nearWestNorthPolePoint);
        collNearWestNorthPole.setWgs84(nearWestNorthPolePoint);

        final Collection collNearEastNorthPole = new Collection(collectionModel, TENANT, "COLEAST",
                "East near North Pole");
        collNearEastNorthPole.setNormalizedGeometry(nearEastNorthPolePoint);
        collNearEastNorthPole.setWgs84(nearEastNorthPolePoint);

        this.save(collNorthPole, collNearWestNorthPole, collNearEastNorthPole);

        List<Collection> results = this.search(ICriterion.intersectsCircle(northPole, "600km"));
        Assert.assertEquals(3, results.size());
        results = this.search(ICriterion.intersectsCircle(nearEastNorthPole, "600km"));
        Assert.assertEquals(2, results.size());
        Assert.assertTrue(results.contains(collNearEastNorthPole));
        Assert.assertTrue(results.contains(collNorthPole));
        Assert.assertFalse(results.contains(collNearWestNorthPole));
        results = this.search(ICriterion.intersectsCircle(nearWestNorthPole, "600km"));
        Assert.assertEquals(2, results.size());
        Assert.assertFalse(results.contains(collNearEastNorthPole));
        Assert.assertTrue(results.contains(collNorthPole));
        Assert.assertTrue(results.contains(collNearWestNorthPole));

        double[] eastPole = new double[] { 180., 0. };
        Point eastPolePoint = IGeometry.point(IGeometry.position(180., 0.));
        Collection collEastPole = new Collection(collectionModel, TENANT, "COLEAST", "East Pole");
        collEastPole.setNormalizedGeometry(eastPolePoint);
        collEastPole.setWgs84(eastPolePoint);

        Point honoluluPoint = IGeometry.point(IGeometry.position(201.005859375 - 360., 21.53484700204879));
        Collection collHonolulu = new Collection(collectionModel, TENANT, "HONO", "Honolulu");
        collHonolulu.setNormalizedGeometry(honoluluPoint);
        collHonolulu.setWgs84(honoluluPoint);

        this.save(collEastPole, collHonolulu);

        results = this.search(ICriterion.intersectsCircle(eastPole, "4000km"));
        Assert.assertEquals(2, results.size());
    }

    @Test
    public void testPolygonSearch() throws ModuleException, IOException {
        Point p202 = IGeometry.point(IGeometry.position(1.4948514103889465, 43.577530672197476));
        final double[][][] cs = new double[][][] { { { 1.4946448802947996, 43.57797369862905 },
                { 1.4946502447128296, 43.57727223860706 }, { 1.4948782324790955, 43.57727418172091 },
                { 1.4948728680610657, 43.57797952790247 }, { 1.4946448802947996, 43.57797369862905 } } };
        // Setting a geometry onto collection
        final Collection collectionOnB202 = new Collection(collectionModel, TENANT, "COLB202",
                "collection on b202 office room");
        collectionOnB202.setNormalizedGeometry(p202);
        collectionOnB202.setWgs84(p202);

        this.save(collectionOnB202);

        // on B202
        List<Collection> results = this.search(ICriterion.intersectsPolygon(cs));
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(collectionOnB202, results.get(0));

        // Concave with B202 office room on it
        final double[][][] concaveCs = new double[][][] { { { 1.4946475625038147, 43.57797369862905 },
                { 1.4947816729545593, 43.577894031835676 }, { 1.4947521686553955, 43.577721096238555 },
                { 1.4946582913398743, 43.57727418172091 }, { 1.4948809146881101, 43.57727223860706 },
                { 1.4948675036430359, 43.57797758481139 }, { 1.4946475625038147, 43.57797369862905 } } };
        // on B202
        results = this.search(ICriterion.intersectsPolygon(concaveCs));
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(collectionOnB202, results.get(0));

        final double[][][] batA = new double[][][] {
                { { 1.4952269196510315, 43.577484037646634 }, { 1.495237648487091, 43.57706821130483 },
                        { 1.495336890220642, 43.57703323512646 }, { 1.4953315258026123, 43.57688944395752 },
                        { 1.4952349662780762, 43.5767747994011 }, { 1.4954683184623718, 43.5767806287906 },
                        { 1.495462954044342, 43.57748598075364 }, { 1.4952269196510315, 43.577484037646634 } } };
        Assert.assertTrue(this.search(ICriterion.intersectsPolygon(batA)).isEmpty());
    }

    @SuppressWarnings("rawtypes")
    private void save(final AbstractEntity... entities) {
        for (final AbstractEntity entity : entities) {
            esRepos.save(TENANT, entity);
        }
        esRepos.refresh(TENANT);
    }

    private List<Collection> search(final ICriterion criterion) {
        return esRepos.search(SEARCH_KEY, 10_000, criterion).getContent();
    }
}
