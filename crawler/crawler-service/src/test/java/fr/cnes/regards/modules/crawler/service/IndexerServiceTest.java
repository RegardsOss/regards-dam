package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableSet;

import fr.cnes.regards.framework.gson.autoconfigure.GsonAutoConfiguration;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Tag;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

@RunWith(SpringRunner.class)

@TestPropertySource({ "classpath:application-test.properties"/*, "classpath:application-rabbit.properties"*/ })
@ContextConfiguration(classes = { CrawlerConfiguration.class, GsonAutoConfiguration.class })
public class IndexerServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerServiceTest.class);

    private String tenant;

    @Autowired
    private IIndexerService indexerService;

    @Before
    public void setUp() throws Exception {
        indexerService.deleteIndex(tenant);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Value("${regards.tenants}")
    public void setTenant(String tenant) {
        this.tenant = tenant.toLowerCase();
    }

    @Test
    public void test() throws IOException {
        Model model = new Model();
        model.setDescription("Description");
        model.setName("name");
        model.setType(EntityType.COLLECTION);

        // Creating a Collection with all types of attributes
        Collection collection = new Collection(model,
                new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, tenant, UUID.randomUUID(), 1),
                "coll1");
        List<AbstractAttribute<?>> attributes = new ArrayList<>();

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

        attributes.add(AttributeBuilder.buildGeometry("geometry", "POLYGON(...)"));

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
        collection.setTags(new ImmutableSet.Builder<Tag>().add(new Tag("TAG1")).add(new Tag("TAG2"))
                .add(new Tag("TAG3")).build());

        indexerService.createIndex(tenant);
        indexerService.saveEntity(tenant, collection);
    }
}