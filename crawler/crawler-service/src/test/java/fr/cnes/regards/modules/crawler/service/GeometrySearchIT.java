package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.crawler.test.CrawlerConfiguration;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.geometry.Geometry;
import fr.cnes.regards.modules.entities.service.adapters.gson.MultitenantFlattenedAttributeAdapterFactoryEventHandler;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.service.Searches;
import fr.cnes.regards.modules.models.domain.EntityType;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@ActiveProfiles("noschedule") // Disable scheduling, this will activate IngesterService during all tests
public class GeometrySearchIT {

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactoryEventHandler gsonAttributeFactoryHandler;

    @Autowired
    private IEsRepository esRepos;

    private static final String TENANT = "GEOM";

    private static final SimpleSearchKey<Collection> SEARCH_KEY = Searches.onSingleEntity(TENANT,
                                                                                          EntityType.COLLECTION);

    @PostConstruct
    public void setUp() {

        // Simulate spring boot ApplicationStarted event to start mapping for each tenants.
        gsonAttributeFactoryHandler.onApplicationEvent(null);

        // Index creation with geometry mapping
        if (esRepos.indexExists(TENANT)) {
            esRepos.deleteIndex(TENANT);
        }
        esRepos.createIndex(TENANT);
        esRepos.setGeometryMapping(TENANT, Arrays.stream(EntityType.values()).map(EntityType::toString)
                .toArray(length -> new String[length]));

    }

    @Test
    public void testCircleSearch() throws ModuleException, IOException {
        final Double[] b202 = new Double[] { 1.4948514103889465, 43.577530672197476 };
        final Double[] b259 = new Double[] { 1.4948514103889465, 43.577614225677394 };
        // Setting a geometry onto collection
        final Collection collectionOnB202 = new Collection(null, TENANT, "collection on b202 office room");
        collectionOnB202.setGeometry(new Geometry.Point(b202));

        final Collection collectionOnB259 = new Collection(null, TENANT, "collection on b100 office room");
        collectionOnB259.setGeometry(new Geometry.Point(b259));

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
        final Double[] northPole = new Double[] { 0., 90. };
        final Double[] nearWestNorthPole = new Double[] { -5., 85. };
        final Double[] nearEastNorthPole = new Double[] { 175., 85. };

        final Collection collNorthPole = new Collection(null, TENANT, "North Pole");
        collNorthPole.setGeometry(new Geometry.Point(northPole));

        final Collection collNearWestNorthPole = new Collection(null, TENANT, "West near North Pole");
        collNearWestNorthPole.setGeometry(new Geometry.Point(nearWestNorthPole));

        final Collection collNearEastNorthPole = new Collection(null, TENANT, "East near North Pole");
        collNearEastNorthPole.setGeometry(new Geometry.Point(nearEastNorthPole));

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

        final Double[] eastPole = new Double[] { 180., 0. };
        final Collection collEastPole = new Collection(null, TENANT, "East Pole");
        collEastPole.setGeometry(new Geometry.Point(eastPole));
        final Double[] honolulu = new Double[] { 201.005859375 - 360., 21.53484700204879 };
        final Collection collHonolulu = new Collection(null, TENANT, "Honolulu");
        collHonolulu.setGeometry(new Geometry.Point(honolulu));

        this.save(collEastPole, collHonolulu);

        results = this.search(ICriterion.intersectsCircle(eastPole, "4000km"));
        Assert.assertEquals(2, results.size());
    }

    @Test
    public void testPolygonSearch() throws ModuleException, IOException {
        final Double[] b202 = new Double[] { 1.4948514103889465, 43.577530672197476 };
        final Double[][][] cs = new Double[][][] { { { 1.4946448802947996, 43.57797369862905 },
                { 1.4946502447128296, 43.57727223860706 }, { 1.4948782324790955, 43.57727418172091 },
                { 1.4948728680610657, 43.57797952790247 }, { 1.4946448802947996, 43.57797369862905 } } };
        // Setting a geometry onto collection
        final Collection collectionOnB202 = new Collection(null, TENANT, "collection on b202 office room");
        collectionOnB202.setGeometry(new Geometry.Point(b202));

        this.save(collectionOnB202);

        // on B202
        List<Collection> results = this.search(ICriterion.intersectsPolygon(cs));
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(collectionOnB202, results.get(0));

        // Concave with B202 office room on it
        final Double[][][] concaveCs = new Double[][][] { { { 1.4946475625038147, 43.57797369862905 },
                { 1.4947816729545593, 43.577894031835676 }, { 1.4947521686553955, 43.577721096238555 },
                { 1.4946582913398743, 43.57727418172091 }, { 1.4948809146881101, 43.57727223860706 },
                { 1.4948675036430359, 43.57797758481139 }, { 1.4946475625038147, 43.57797369862905 } } };
        // on B202
        results = this.search(ICriterion.intersectsPolygon(concaveCs));
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(collectionOnB202, results.get(0));

        final Double[][][] batA = new Double[][][] {
                { { 1.4952269196510315, 43.577484037646634 }, { 1.495237648487091, 43.57706821130483 },
                        { 1.495336890220642, 43.57703323512646 }, { 1.4953315258026123, 43.57688944395752 },
                        { 1.4952349662780762, 43.5767747994011 }, { 1.4954683184623718, 43.5767806287906 },
                        { 1.495462954044342, 43.57748598075364 }, { 1.4952269196510315, 43.577484037646634 } } };
        Assert.assertTrue(this.search(ICriterion.intersectsPolygon(batA)).isEmpty());
    }

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
