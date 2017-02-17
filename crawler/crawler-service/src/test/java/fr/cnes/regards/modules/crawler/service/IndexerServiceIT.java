/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
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
import fr.cnes.regards.modules.entities.service.adapters.gson.FlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
public class IndexerServiceIT {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerServiceIT.class);

    private static final String TENANT = "project";

    private static final String SEARCH = "project_search";

    @Autowired
    private IIndexerService indexerService;

    @Autowired
    private FlattenedAttributeAdapterFactory gsonAttributeFactory;

    @Autowired
    private Gson gson;

    @Before
    public void setUp() throws Exception {
        indexerService.deleteIndex(TENANT);
        // indexerService.deleteIndex(SEARCH);
    }

    @Test
    public void testSave() throws IOException {
        Model model = new Model();
        model.setDescription("Description");
        model.setName("name");
        model.setType(EntityType.COLLECTION);

        // Creating a Collection with all types of attributes
        Collection collection = new Collection(model, TENANT, "coll1");
        List<AbstractAttribute<?>> attributes = new ArrayList<>();

        gsonAttributeFactory.registerSubtype(BooleanAttribute.class, "booleanAtt");
        gsonAttributeFactory.registerSubtype(DateArrayAttribute.class, "dateArrayAtt");
        gsonAttributeFactory.registerSubtype(DateAttribute.class, "dateAtt");
        gsonAttributeFactory.registerSubtype(DateIntervalAttribute.class, "dateInterval");
        gsonAttributeFactory.registerSubtype(DoubleAttribute.class, "maxDoubleValue");
        gsonAttributeFactory.registerSubtype(DoubleAttribute.class, "minDoubleValue");
        gsonAttributeFactory.registerSubtype(DoubleAttribute.class, "double");
        gsonAttributeFactory.registerSubtype(DoubleArrayAttribute.class, "doubleArray");
        gsonAttributeFactory.registerSubtype(DoubleIntervalAttribute.class, "doubleInterval");
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "maxInt");
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "minInt");
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "int");
        gsonAttributeFactory.registerSubtype(IntegerArrayAttribute.class, "intArray");
        gsonAttributeFactory.registerSubtype(IntegerIntervalAttribute.class, "intInterval");
        gsonAttributeFactory.registerSubtype(IntegerIntervalAttribute.class, "N");
        gsonAttributeFactory.registerSubtype(StringAttribute.class, "string");
        gsonAttributeFactory.registerSubtype(ObjectAttribute.class, "correspondance");
        gsonAttributeFactory.registerSubtype(StringArrayAttribute.class, "stringArrayMusset", "correspondance");
        gsonAttributeFactory.registerSubtype(StringArrayAttribute.class, "stringArraySand", "correspondance");

        attributes.add(AttributeBuilder.buildBoolean("booleanAtt", true));
        attributes.add(AttributeBuilder.buildDateArray("dateArrayAtt", LocalDateTime.of(2016, 1, 13, 11, 5),
                                                       LocalDateTime.of(2015, 12, 31, 11, 59),
                                                       LocalDateTime.of(2000, 1, 1, 0, 0)));
        attributes.add(AttributeBuilder.buildDate("dateAtt", LocalDateTime.of(1974, 10, 31, 1, 50)));
        attributes.add(AttributeBuilder.buildDateInterval("dateInterval", LocalDateTime.of(1939, 9, 1, 0, 0),
                                                          LocalDateTime.of(1945, 9, 2, 0, 0)));
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

        ObjectAttribute fragment = AttributeBuilder
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

        collection.setAttributes(attributes);
        collection.setTags(new ImmutableSet.Builder<String>().add("TAG1").add("TAG2").add("TAG3").build());

        String collectionString = gson.toJson(collection);

        indexerService.createIndex(TENANT);
        indexerService.saveEntity(TENANT, collection);
        indexerService.refresh(TENANT);
        // Following lines are just to test Gson serialization/deserialization of all attribute types
        List<Collection> singleCollColl = indexerService
                .search(TENANT, Collection.class, 10, ICriterion.eq("attributes.int", 42)).getContent();
        Assert.assertEquals(1, singleCollColl.size());
    }

    @Test
    @Ignore
    public void testSaveBulk() {
        // Model for collection
        Model collModel = new Model();
        collModel.setDescription("model for collections");
        collModel.setName("collModel");
        collModel.setType(EntityType.COLLECTION);

        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "altitude");
        gsonAttributeFactory.registerSubtype(DoubleAttribute.class, "latitude");
        gsonAttributeFactory.registerSubtype(DoubleAttribute.class, "longitude");

        indexerService.createIndex(SEARCH);

        // Creating a Collection for this model
        int[] COUNTS = { 10, 100, 1000, 10000 };
        int totalCount = 0;
        for (int i = 0; i < 100; i++) {
            int count = COUNTS[(int) (Math.random() * COUNTS.length)];
            totalCount += count;
            bulkSave(count, collModel);
        }
        System.out.println(String.format("Index %s should contain %d documents", SEARCH, totalCount));

    }

    private void bulkSave(int count, Model collModel) {
        List<Collection> collections = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            collections.add(createCollection(collModel, i + 1));
        }
        long start = System.currentTimeMillis();
        Map<String, Throwable> errors = indexerService.saveBulkEntities(SEARCH, collections);
        System.out.println(String.format("Bulk save (%d collections) : %d ms", collections.size(),
                                         System.currentTimeMillis() - start));
        Assert.assertNull(errors);
    }

    private Collection createCollection(Model collModel, int i) {
        Collection collection = new Collection(collModel, SEARCH, "coll" + i);
        List<AbstractAttribute<?>> attributes = new ArrayList<>();
        attributes.add(AttributeBuilder.buildInteger("altitude", (int) (Math.random() * 8848)));
        attributes.add(AttributeBuilder.buildDouble("longitude", (Math.random() * 360.) - 180.));
        attributes.add(AttributeBuilder.buildDouble("latitude", (Math.random() * 180.) - 90.));
        collection.setAttributes(attributes);
        return collection;
    }

    @Test
    @Ignore
    public void testSimpleSearch() {
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "altitude");
        gsonAttributeFactory.registerSubtype(DoubleAttribute.class, "latitude");
        gsonAttributeFactory.registerSubtype(DoubleAttribute.class, "longitude");

        ICriterion criterion = ICriterion.eq("attributes.altitude", 3700);
        Page<? extends AbstractEntity> collPage = indexerService.search(SEARCH, AbstractEntity.class, 10, criterion);
        int count = 0;
        while (true) {
            for (AbstractEntity coll : collPage.getContent()) {
                System.out.println(coll.toString());
                count++;
            }
            if (collPage.isLast()) {
                break;

            }
            collPage = indexerService.search(SEARCH, AbstractEntity.class, collPage.nextPageable(), criterion);
        }
        Assert.assertEquals(26, count);
    }
}