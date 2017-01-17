package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableSet;

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
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
public class IndexerServiceIT {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerServiceIT.class);

    private static final String TENANT = "project";

    @Autowired
    private IIndexerService indexerService;

    @Autowired
    private FlattenedAttributeAdapterFactory gsonAttributeFactory;

    @Before
    public void setUp() throws Exception {
        indexerService.deleteIndex(TENANT);
    }

    @Test
    public void testSave() throws IOException {
        Model model = new Model();
        model.setDescription("Description");
        model.setName("name");
        model.setType(EntityType.COLLECTION);

        // Creating a Collection with all types of attributes
        Collection collection = new Collection(model,
                new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, TENANT, UUID.randomUUID(), 1),
                "coll1");
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

        attributes.add(AttributeBuilder.buildDoubleInterval("doubleInterval", 0., 1.));

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

        indexerService.createIndex(TENANT);
        indexerService.saveEntity(TENANT, collection);
    }

    public void testSaveBulk() {

    }

}