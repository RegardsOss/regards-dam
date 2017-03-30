package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.geometry.Geometry;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.service.Searches;
import fr.cnes.regards.modules.models.domain.EntityType;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
public class GeometrySearchIT {

    @Autowired
    private IEsRepository esRepos;

    private static final String TENANT = "GEOM";

    private static final SimpleSearchKey<Collection> SEARCH_KEY = Searches.onSingleEntity(TENANT,
                                                                                          EntityType.COLLECTION);

    @PostConstruct
    public void setUp() {
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
        Double[] b202 = new Double[] { 1.4948514103889465, 43.577530672197476 };
        Double[] b259 = new Double[] { 1.4948514103889465, 43.577614225677394 };
        // Setting a geometry onto collection
        Collection collectionOnB202 = new Collection(null, TENANT, "collection on b202 office room");
        collectionOnB202.setGeometry(new Geometry.Point(b202));

        Collection collectionOnB259 = new Collection(null, TENANT, "collection on b100 office room");
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
    @Ignore
    public void testCircleSearchOnLimits() throws ModuleException, IOException {
        Double[] northPole = new Double[] { 0., 90. };
        Double[] nearWestNorthPole = new Double[] { -5., 85. };
        Double[] nearEastNorthPole = new Double[] { 5., 85. };

        Collection data1 = new Collection(null, TENANT, "North Pole");
        data1.setGeometry(new Geometry.Point(northPole));

        Collection data2 = new Collection(null, TENANT, "West near North Pole");
        data2.setGeometry(new Geometry.Point(nearWestNorthPole));

        Collection data3 = new Collection(null, TENANT, "East near North Pole");
        data3.setGeometry(new Geometry.Point(nearEastNorthPole));

        this.save(data1, data2, data3);

        List<Collection> results = this.search(ICriterion.intersectsCircle(northPole, "100km"));
        Assert.assertEquals(3, results.size());
        results = this.search(ICriterion.intersectsCircle(nearEastNorthPole, "100km"));
        Assert.assertEquals(2, results.size());
        Assert.assertTrue(results.contains(nearEastNorthPole));
        Assert.assertTrue(results.contains(northPole));
        Assert.assertFalse(results.contains(nearWestNorthPole));
        results = this.search(ICriterion.intersectsCircle(nearWestNorthPole, "100km"));
        Assert.assertEquals(2, results.size());
        Assert.assertFalse(results.contains(nearEastNorthPole));
        Assert.assertTrue(results.contains(northPole));
        Assert.assertTrue(results.contains(nearWestNorthPole));

    }

    private void save(AbstractEntity... entities) {
        for (AbstractEntity entity : entities) {
            esRepos.save(TENANT, entity);
        }
        esRepos.refresh(TENANT);
    }

    private List<Collection> search(ICriterion criterion) {
        return esRepos.search(SEARCH_KEY, 10_000, criterion).getContent();
    }
}
