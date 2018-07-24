/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.crawler.test.CrawlerConfiguration;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.BooleanAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.entities.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.entities.gson.MultitenantFlattenedAttributeAdapterFactoryEventHandler;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.service.IIndexerService;
import fr.cnes.regards.modules.indexer.service.ISearchService;
import fr.cnes.regards.modules.indexer.service.Searches;
import fr.cnes.regards.modules.models.domain.Model;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@ActiveProfiles("noschedule") // Disable scheduling, this will activate IngesterService during all tests
@TestPropertySource(locations = { "classpath:test.properties" })
public class IndexerServiceIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerServiceIT.class);

    private static final String SEARCH = "project_search";

    @Value("${regards.tenant}")
    private String tenant;

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactoryEventHandler gsonAttributeFactoryHandler;

    @Autowired
    private IIndexerService indexerService;

    @Autowired
    private ISearchService searchService;

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;

    @Autowired
    private Gson gson;

    /**
     * Resolve thread tenant at runtime
     */
    @Autowired
    protected IRuntimeTenantResolver runtimeTenantResolver;

    @Before
    public void setUp() throws Exception {

        // Simulate spring boot ApplicationStarted event to start mapping for each tenants.
        gsonAttributeFactoryHandler.onApplicationEvent(null);

        runtimeTenantResolver.forceTenant(tenant);
        indexerService.deleteIndex(tenant);
        // indexerService.deleteIndex(SEARCH);
    }

    @Test
    public void testSave() throws IOException {
        final Model model = new Model();
        model.setDescription("Description");
        model.setName("name");
        model.setType(EntityType.COLLECTION);

        // Creating a Collection with all types of attributes
        final Collection collection = new Collection(model, tenant, "coll1");
        final HashSet<AbstractAttribute<?>> attributes = new HashSet<>();

        gsonAttributeFactory.registerSubtype(tenant, BooleanAttribute.class, "booleanAtt");
        gsonAttributeFactory.registerSubtype(tenant, DateArrayAttribute.class, "dateArrayAtt");
        gsonAttributeFactory.registerSubtype(tenant, DateAttribute.class, "dateAtt");
        gsonAttributeFactory.registerSubtype(tenant, DateIntervalAttribute.class, "dateInterval");
        gsonAttributeFactory.registerSubtype(tenant, DoubleAttribute.class, "maxDoubleValue");
        gsonAttributeFactory.registerSubtype(tenant, DoubleAttribute.class, "minDoubleValue");
        gsonAttributeFactory.registerSubtype(tenant, DoubleAttribute.class, "double");
        gsonAttributeFactory.registerSubtype(tenant, DoubleArrayAttribute.class, "doubleArray");
        gsonAttributeFactory.registerSubtype(tenant, DoubleIntervalAttribute.class, "doubleInterval");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "maxInt");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "minInt");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "int");
        gsonAttributeFactory.registerSubtype(tenant, IntegerArrayAttribute.class, "intArray");
        gsonAttributeFactory.registerSubtype(tenant, IntegerIntervalAttribute.class, "intInterval");
        gsonAttributeFactory.registerSubtype(tenant, IntegerIntervalAttribute.class, "N");
        gsonAttributeFactory.registerSubtype(tenant, StringAttribute.class, "string");
        gsonAttributeFactory.registerSubtype(tenant, ObjectAttribute.class, "correspondance");
        gsonAttributeFactory.registerSubtype(tenant, StringArrayAttribute.class, "stringArrayMusset", "correspondance");
        gsonAttributeFactory.registerSubtype(tenant, StringArrayAttribute.class, "stringArraySand", "correspondance");

        attributes.add(AttributeBuilder.buildBoolean("booleanAtt", true));
        attributes.add(AttributeBuilder.buildDateArray("dateArrayAtt",
                                                       OffsetDateTime.of(2016, 1, 13, 11, 5, 0, 0, ZoneOffset.UTC),
                                                       OffsetDateTime.of(2015, 12, 31, 11, 59, 0, 0, ZoneOffset.UTC),
                                                       OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)));
        attributes.add(AttributeBuilder.buildDate("dateAtt",
                                                  OffsetDateTime.of(1974, 10, 31, 1, 50, 0, 0, ZoneOffset.UTC)));
        attributes.add(AttributeBuilder.buildDateInterval("dateInterval",
                                                          OffsetDateTime.of(1939, 9, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                                                          OffsetDateTime.of(1945, 9, 2, 0, 0, 0, 0, ZoneOffset.UTC)));
        attributes.add(AttributeBuilder.buildDouble("maxDoubleValue", Double.MAX_VALUE));
        attributes.add(AttributeBuilder.buildDouble("minDoubleValue", Double.MIN_VALUE));

        // attributes.add(AttributeBuilder.buildDouble("NaN", Double.NaN));
        // attributes.add(AttributeBuilder.buildDouble("positiveInfinity", Double.POSITIVE_INFINITY));
        // attributes.add(AttributeBuilder.buildDouble("negativeInfinity", Double.NEGATIVE_INFINITY));

        attributes.add(AttributeBuilder.buildDouble("double", 1.414213562));

        attributes.add(AttributeBuilder.buildDoubleArray("doubleArray", 0., 1., Math.PI, Math.E));

        attributes.add(AttributeBuilder.buildDoubleInterval("doubleInterval", 0., 2.));

        // attributes.add(AttributeBuilder.buildGeometry("geometry", "POLYGON(...)"));

        attributes.add(AttributeBuilder.buildInteger("maxInt", Integer.MAX_VALUE));
        attributes.add(AttributeBuilder.buildInteger("minInt", Integer.MIN_VALUE));
        attributes.add(AttributeBuilder.buildInteger("int", 42));

        attributes.add(AttributeBuilder.buildIntegerArray("intArray", -2, -1, 0, 1, 2));

        attributes.add(AttributeBuilder.buildIntegerInterval("intInterval", -10, 10));
        attributes.add(AttributeBuilder.buildIntegerInterval("N", Integer.MIN_VALUE, Integer.MAX_VALUE));

        attributes.add(AttributeBuilder.buildString("string", "Esope reste et se repose"));

        final ObjectAttribute fragment = AttributeBuilder
                .buildObject("correspondance",
                             AttributeBuilder.buildStringArray("stringArrayMusset",
                                                               "Quand je mets à vos pieds un éternel hommage",
                                                               "Voulez-vous qu'un instant je change de visage ?",
                                                               "Vous avez capturé les sentiments d'un coeur",
                                                               "Que pour vous adorer forma le créateur.",
                                                               "Je vous chéris, amour, et ma plume en délire",
                                                               "Couche sur le papier ce que je n'ose dire.",
                                                               "Avec soin de mes vers lisez les premiers mots,",
                                                               "Vous saurez quel remède apporter à mes maux."),
                             AttributeBuilder.buildStringArray("stringArraySand",
                                                               "Cette indigne faveur que votre esprit réclame",
                                                               "Nuit à mes sentiments et répugne à mon âme"));
        attributes.add(fragment);

        collection.setProperties(attributes);
        collection.setTags(new ImmutableSet.Builder<String>().add("TAG1").add("TAG2").add("TAG3").build());

        indexerService.createIndex(tenant);
        indexerService.saveEntity(tenant, collection);
        indexerService.refresh(tenant);

        // Following lines are just to test Gson serialization/deserialization of all attribute types
        final List<Collection> singleCollColl = searchService
                .search(new SimpleSearchKey<>(EntityType.COLLECTION.toString(), Collection.class), 10,
                        ICriterion.eq("feature.properties.int", 42))
                .getContent();
        Assert.assertEquals(1, singleCollColl.size());
    }

    @Test
    @Ignore
    public void testSaveBulk() {
        // Model for collection
        final Model collModel = new Model();
        collModel.setDescription("model for collections");
        collModel.setName("collModel");
        collModel.setType(EntityType.COLLECTION);

        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "altitude");
        gsonAttributeFactory.registerSubtype(tenant, DoubleAttribute.class, "latitude");
        gsonAttributeFactory.registerSubtype(tenant, DoubleAttribute.class, "longitude");

        indexerService.createIndex(SEARCH);

        // Creating a Collection for this model
        final int[] COUNTS = { 10, 100, 1000, 10000 };
        int totalCount = 0;
        for (int i = 0; i < 100; i++) {
            final int count = COUNTS[(int) (Math.random() * COUNTS.length)];
            totalCount += count;
            bulkSave(count, collModel);
        }
        LOGGER.info("Index {} should contain {} documents", SEARCH, totalCount);

    }

    private void bulkSave(final int count, final Model collModel) {
        final List<Collection> collections = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            collections.add(createCollection(collModel, i + 1));
        }
        final long start = System.currentTimeMillis();
        final int savedCollCount = indexerService.saveBulkEntities(SEARCH, collections);
        LOGGER.info("Bulk save ({} collections) : {} ms", collections.size(), System.currentTimeMillis() - start);
        Assert.assertEquals(collections.size(), savedCollCount);
    }

    private Collection createCollection(final Model collModel, final int i) {
        final Collection collection = new Collection(collModel, SEARCH, "coll" + i);
        final HashSet<AbstractAttribute<?>> attributes = new HashSet<>();
        attributes.add(AttributeBuilder.buildInteger("altitude", (int) (Math.random() * 8848)));
        attributes.add(AttributeBuilder.buildDouble("longitude", (Math.random() * 360.) - 180.));
        attributes.add(AttributeBuilder.buildDouble("latitude", (Math.random() * 180.) - 90.));
        collection.setProperties(attributes);
        return collection;
    }

    @Test
    @Ignore
    public void testSimpleSearch() {
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "altitude");
        gsonAttributeFactory.registerSubtype(tenant, DoubleAttribute.class, "latitude");
        gsonAttributeFactory.registerSubtype(tenant, DoubleAttribute.class, "longitude");

        // FIXME : this criterion cannot be available
        final ICriterion criterion = ICriterion.eq("attributes.altitude", 3700);
        // SearchKey<AbstractEntity> searchKey = new SearchKey<>(SEARCH, null, AbstractEntity.class);
        final SimpleSearchKey<AbstractEntity> searchKey = Searches.onAllEntities();
        searchKey.setSearchIndex(SEARCH);
        Page<? extends AbstractEntity> collPage = searchService.search(searchKey, 10, criterion);
        int count = 0;
        while (true) {
            for (final AbstractEntity coll : collPage.getContent()) {
                count++;
            }
            if (collPage.isLast()) {
                break;

            }
            collPage = searchService.search(searchKey, collPage.nextPageable(), criterion);
        }
        Assert.assertEquals(26, count);
    }
}