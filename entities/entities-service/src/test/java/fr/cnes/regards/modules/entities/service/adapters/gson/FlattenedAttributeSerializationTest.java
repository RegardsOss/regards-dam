/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.adapters.gson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import fr.cnes.regards.framework.gson.adapters.LocalDateTimeAdapter;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.BooleanAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.GeometryAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

/**
 *
 * Test attribute serialization
 *
 * @author Marc Sordi
 *
 */
public class FlattenedAttributeSerializationTest {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FlattenedAttributeSerializationTest.class);

    /**
     * "description" attribute
     */
    private static final String DISCRIMINATOR_DESCRIPTION = "description";

    /**
     * "runnable" attribute
     */
    private static final String DISCRIMINATOR_RUNNABLE = "runnable";

    /**
     * "geo" attribute
     */
    private static final String DISCRIMINATOR_GEO = "geo";

    /**
     * "crs" attribute
     */
    private static final String DISCRIMINATOR_CRS = "crs";

    /**
     * "coordinate" attribute
     */
    private static final String DISCRIMINATOR_COORDINATE = "coordinate";

    /**
     * "Org" attribute
     */
    private static final String DISCRIMINATOR_ORG = "Org";

    /**
     * Polymorphic factory
     */
    private FlattenedAttributeAdapterFactory factory;

    /**
     * Gson instance
     */
    private Gson gson;

    /**
     * {@link AttributeModel} service
     */
    private IAttributeModelService mockAttModelService;

    /**
     * Init GSON context
     */
    @Before
    public void initGson() {

        mockAttModelService = Mockito.mock(IAttributeModelService.class);
        final GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapterFactory(new CarEntityTypeAdapterFactory());

        factory = new FlattenedAttributeAdapterFactory(mockAttModelService);
        // Register sub type(s)
        factory.registerSubtype(StringAttribute.class, DISCRIMINATOR_DESCRIPTION);
        factory.registerSubtype(ObjectAttribute.class, DISCRIMINATOR_GEO); // geo namespace
        factory.registerSubtype(StringAttribute.class, DISCRIMINATOR_CRS, DISCRIMINATOR_GEO);
        factory.registerSubtype(GeometryAttribute.class, DISCRIMINATOR_COORDINATE, DISCRIMINATOR_GEO);
        factory.registerSubtype(ObjectAttribute.class, DISCRIMINATOR_ORG); // org namespace
        factory.registerSubtype(StringArrayAttribute.class, DISCRIMINATOR_DESCRIPTION, DISCRIMINATOR_ORG);

        gsonBuilder.registerTypeAdapterFactory(factory);
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe());
        gson = gsonBuilder.create();
    }

    /**
     * Test with root attributes
     */
    @Test
    public void onlyRootAttribute() {
        Car car = getCarWithRootAttribute();

        final String jsonCar = gson.toJson(car);
        LOGGER.info(jsonCar);
        final Car parsedCar = gson.fromJson(jsonCar, Car.class);

        Assert.assertEquals(1, parsedCar.getAttributes().size());
    }

    /**
     * Test adding new attribute at runtime (after factory initialized)
     */
    @Test
    public void addAttributeAtRuntime() {
        Car car = getCarWithRootAttribute();

        String jsonCar = gson.toJson(car);
        LOGGER.info(jsonCar);
        Car parsedCar = gson.fromJson(jsonCar, Car.class);

        Assert.assertEquals(1, parsedCar.getAttributes().size());

        // Add new attribute
        addRuntimeRootAttribute(car);

        try {
            gson.toJson(car);
        } catch (JsonParseException e) {
            LOGGER.error("New attribute not registered");
        }

        // Registering new attribute
        factory.registerSubtype(BooleanAttribute.class, DISCRIMINATOR_RUNNABLE);

        jsonCar = gson.toJson(car);
        LOGGER.info(jsonCar);
        parsedCar = gson.fromJson(jsonCar, Car.class);

        Assert.assertEquals(2, parsedCar.getAttributes().size());
    }

    /**
     * Test with root and nested attributes
     */
    @Test
    public void nestedAttributes() {
        Car car = getCarWithRootAttribute();
        addNestedAttributes(car);

        final String jsonCar = gson.toJson(car);
        LOGGER.info(jsonCar);
        final Car parsedCar = gson.fromJson(jsonCar, Car.class);

        List<AbstractAttribute<?>> attributes = parsedCar.getAttributes();
        Assert.assertEquals(2, attributes.size());

        List<String> expectedRootAttributes = new ArrayList<>();
        expectedRootAttributes.add(DISCRIMINATOR_DESCRIPTION);
        expectedRootAttributes.add(DISCRIMINATOR_GEO);

        for (AbstractAttribute<?> att : attributes) {
            Assert.assertThat(att.getName(), Matchers.isIn(expectedRootAttributes));

            if (DISCRIMINATOR_DESCRIPTION.equals(att.getName())) {
                Assert.assertThat(att, Matchers.instanceOf(StringAttribute.class));
            }

            if (DISCRIMINATOR_GEO.equals(att.getName())) {
                Assert.assertThat(att, Matchers.instanceOf(ObjectAttribute.class));
                ObjectAttribute geo = (ObjectAttribute) att;

                for (AbstractAttribute<?> nested : geo.getValue()) {
                    if (DISCRIMINATOR_COORDINATE.equals(nested.getName())) {
                        Assert.assertThat(nested, Matchers.instanceOf(GeometryAttribute.class));
                    }
                }
            }
        }
    }

    /**
     * Test with root and nested conflictual attributes
     */
    @Test
    public void conflictAttributes() {
        Car car = getCarWithRootAttribute();
        addNestedAttributes(car);
        addConflictAttributes(car);

        final String jsonCar = gson.toJson(car);
        LOGGER.info(jsonCar);
        final Car parsedCar = gson.fromJson(jsonCar, Car.class);

        List<AbstractAttribute<?>> attributes = parsedCar.getAttributes();

        final int expectedSize = 3;
        Assert.assertThat(attributes, Matchers.hasSize(expectedSize));

        List<String> expectedRootAttributes = new ArrayList<>();
        expectedRootAttributes.add(DISCRIMINATOR_DESCRIPTION);
        expectedRootAttributes.add(DISCRIMINATOR_GEO);
        expectedRootAttributes.add(DISCRIMINATOR_ORG);

        for (AbstractAttribute<?> att : attributes) {
            Assert.assertThat(att.getName(), Matchers.isIn(expectedRootAttributes));

            if (DISCRIMINATOR_DESCRIPTION.equals(att.getName())) {
                Assert.assertThat(att, Matchers.instanceOf(StringAttribute.class));
            }

            if (DISCRIMINATOR_ORG.equals(att.getName())) {
                Assert.assertThat(att, Matchers.instanceOf(ObjectAttribute.class));
                ObjectAttribute geo = (ObjectAttribute) att;

                for (AbstractAttribute<?> nested : geo.getValue()) {
                    if (DISCRIMINATOR_DESCRIPTION.equals(nested.getName())) {
                        Assert.assertThat(nested, Matchers.instanceOf(StringArrayAttribute.class));
                    }
                }
            }

        }
    }

    /**
     *
     * @return {@link Car}
     */
    private Car getCarWithRootAttribute() {
        Car car = new Car();

        List<AbstractAttribute<?>> attributes = new ArrayList<>();

        StringAttribute description = new StringAttribute();
        description.setName(DISCRIMINATOR_DESCRIPTION);
        description.setValue("test description");
        attributes.add(description);

        car.setAttributes(attributes);
        return car;
    }

    /**
     *
     * @param pCar
     *            {@link Car}
     */
    private void addRuntimeRootAttribute(Car pCar) {

        BooleanAttribute runnable = new BooleanAttribute();
        runnable.setName(DISCRIMINATOR_RUNNABLE);
        runnable.setValue(true);
        pCar.getAttributes().add(runnable);
    }

    /**
     *
     * @param pCar
     *            {@link Car} with nested attributes
     */
    private void addNestedAttributes(Car pCar) {

        // Namespace or fragment name
        ObjectAttribute geo = new ObjectAttribute();
        geo.setName(DISCRIMINATOR_GEO);

        StringAttribute crs = new StringAttribute();
        crs.setName(DISCRIMINATOR_CRS);
        crs.setValue("WGS84");

        GeometryAttribute coordinate = new GeometryAttribute();
        coordinate.setName(DISCRIMINATOR_COORDINATE);
        coordinate.setValue("POLYGON(TITI,TOTO)");

        List<AbstractAttribute<?>> atts = new ArrayList<>();
        atts.add(crs);
        atts.add(coordinate);
        geo.setValue(atts);

        pCar.getAttributes().add(geo);
    }

    /**
     *
     * @param pCar
     *            {@link Car} with conflicting attributes
     */
    private void addConflictAttributes(Car pCar) {
        // Namespace or fragment name
        ObjectAttribute org = new ObjectAttribute();
        org.setName(DISCRIMINATOR_ORG);

        StringArrayAttribute description = new StringArrayAttribute();
        description.setName(DISCRIMINATOR_DESCRIPTION);
        description.setValue(Arrays.array("desc1", "desc2"));

        List<AbstractAttribute<?>> atts = new ArrayList<>();
        atts.add(description);
        org.setValue(atts);

        pCar.getAttributes().add(org);
    }
}
