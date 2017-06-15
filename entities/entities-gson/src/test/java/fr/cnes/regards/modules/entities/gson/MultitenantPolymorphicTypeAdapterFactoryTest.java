/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.gson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.BooleanAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;

/**
 * Test attribute serialization
 *
 * @author Marc Sordi
 */
public class MultitenantPolymorphicTypeAdapterFactoryTest {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultitenantPolymorphicTypeAdapterFactoryTest.class);

    /**
     * Tenant
     */
    private static final String TENANT = "tenant";

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
    private MultitenantFlattenedAttributeAdapterFactory factory;

    /**
     * Gson instance
     */
    private Gson gson;

    /**
     * {@link ISubscriber} service
     */
    private ISubscriber mockSubscriber;

    /**
     * {@link ITenantResolver}
     */
    private ITenantResolver mockTenantResolver;

    /**
     * {@link IRuntimeTenantResolver}
     */
    private IRuntimeTenantResolver mockRuntimeTenantResolver;

    /**
     * Init GSON context
     */
    @Before
    public void initGson() {

        mockSubscriber = Mockito.mock(ISubscriber.class);
        mockTenantResolver = Mockito.mock(ITenantResolver.class);
        mockRuntimeTenantResolver = Mockito.mock(IRuntimeTenantResolver.class);
        Mockito.when(mockRuntimeTenantResolver.getTenant()).thenReturn(TENANT);

        final GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapterFactory(new TestEntityAdapterFactory());

        factory = new MultitenantFlattenedAttributeAdapterFactory(mockRuntimeTenantResolver);
        // Register sub type(s)
        factory.registerSubtype(TENANT, StringAttribute.class, DISCRIMINATOR_DESCRIPTION);
        factory.registerSubtype(TENANT, ObjectAttribute.class, DISCRIMINATOR_GEO); // geo namespace
        factory.registerSubtype(TENANT, StringAttribute.class, DISCRIMINATOR_CRS, DISCRIMINATOR_GEO);
        factory.registerSubtype(TENANT, ObjectAttribute.class, DISCRIMINATOR_ORG); // org namespace
        factory.registerSubtype(TENANT, StringArrayAttribute.class, DISCRIMINATOR_DESCRIPTION, DISCRIMINATOR_ORG);

        gsonBuilder.registerTypeAdapterFactory(factory);
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new OffsetDateTimeAdapter().nullSafe());
        gson = gsonBuilder.create();
    }

    /**
     * Test with root attributes
     */
    @Test
    public void onlyRootAttribute() {
        final Car car = getCarWithRootAttribute();

        final String jsonCar = gson.toJson(car);
        LOGGER.info(jsonCar);
        final Car parsedCar = gson.fromJson(jsonCar, Car.class);

        Assert.assertEquals(1, parsedCar.getProperties().size());
    }

    /**
     * Test adding new attribute at runtime (after factory initialized)
     */
    @Test
    public void addAttributeAtRuntime() {
        final Car car = getCarWithRootAttribute();

        String jsonCar = gson.toJson(car);
        LOGGER.info(jsonCar);
        Car parsedCar = gson.fromJson(jsonCar, Car.class);

        Assert.assertEquals(1, parsedCar.getProperties().size());

        // Add new attribute
        addRuntimeRootAttribute(car);

        try {
            gson.toJson(car);
        } catch (final JsonParseException e) {
            LOGGER.error("New attribute not registered");
        }

        // Registering new attribute
        factory.registerSubtype(TENANT, BooleanAttribute.class, DISCRIMINATOR_RUNNABLE);

        jsonCar = gson.toJson(car);
        LOGGER.info(jsonCar);
        parsedCar = gson.fromJson(jsonCar, Car.class);

        Assert.assertEquals(2, parsedCar.getProperties().size());
    }

    /**
     * Test with root and nested attributes
     */
    @Test
    public void nestedAttributes() {
        final Car car = getCarWithRootAttribute();
        addNestedAttributes(car);

        final String jsonCar = gson.toJson(car);
        LOGGER.info(jsonCar);
        final Car parsedCar = gson.fromJson(jsonCar, Car.class);

        final Set<AbstractAttribute<?>> attributes = parsedCar.getProperties();
        Assert.assertEquals(2, attributes.size());

        final List<String> expectedRootAttributes = new ArrayList<>();
        expectedRootAttributes.add(DISCRIMINATOR_DESCRIPTION);
        expectedRootAttributes.add(DISCRIMINATOR_GEO);

        for (final AbstractAttribute<?> att : attributes) {
            Assert.assertTrue(expectedRootAttributes.contains(att.getName()));

            if (DISCRIMINATOR_DESCRIPTION.equals(att.getName())) {
                Assert.assertTrue(att instanceof StringAttribute);
            }

            if (DISCRIMINATOR_GEO.equals(att.getName())) {
                Assert.assertTrue(att instanceof ObjectAttribute);
            }
        }
    }

    /**
     * Test with root and nested conflictual attributes
     */
    @Test
    public void conflictAttributes() {
        final Car car = getCarWithRootAttribute();
        addNestedAttributes(car);
        addConflictAttributes(car);

        final String jsonCar = gson.toJson(car);
        LOGGER.info(jsonCar);
        final Car parsedCar = gson.fromJson(jsonCar, Car.class);

        final Set<AbstractAttribute<?>> attributes = parsedCar.getProperties();

        final int expectedSize = 3;
        Assert.assertEquals(expectedSize, attributes.size());

        final List<String> expectedRootAttributes = new ArrayList<>();
        expectedRootAttributes.add(DISCRIMINATOR_DESCRIPTION);
        expectedRootAttributes.add(DISCRIMINATOR_GEO);
        expectedRootAttributes.add(DISCRIMINATOR_ORG);

        for (final AbstractAttribute<?> att : attributes) {
            Assert.assertTrue(expectedRootAttributes.contains(att.getName()));

            if (DISCRIMINATOR_DESCRIPTION.equals(att.getName())) {
                Assert.assertTrue(att instanceof StringAttribute);
            }

            if (DISCRIMINATOR_ORG.equals(att.getName())) {
                Assert.assertTrue(att instanceof ObjectAttribute);
                final ObjectAttribute geo = (ObjectAttribute) att;

                for (final AbstractAttribute<?> nested : geo.getValue()) {
                    if (DISCRIMINATOR_DESCRIPTION.equals(nested.getName())) {
                        Assert.assertTrue(nested instanceof StringArrayAttribute);
                    }
                }
            }

        }
    }

    /**
     * @return {@link Car}
     */
    private Car getCarWithRootAttribute() {
        final Car car = new Car();

        final Set<AbstractAttribute<?>> attributes = new HashSet<>();

        final StringAttribute description = new StringAttribute();
        description.setName(DISCRIMINATOR_DESCRIPTION);
        description.setValue("test description");
        attributes.add(description);

        car.setProperties(attributes);
        return car;
    }

    /**
     * @param pCar
     *            {@link Car}
     */
    private void addRuntimeRootAttribute(final Car pCar) {

        final BooleanAttribute runnable = new BooleanAttribute();
        runnable.setName(DISCRIMINATOR_RUNNABLE);
        runnable.setValue(true);
        pCar.getProperties().add(runnable);
    }

    /**
     * @param pCar
     *            {@link Car} with nested attributes
     */
    private void addNestedAttributes(final Car pCar) {

        // Namespace or fragment name
        final ObjectAttribute geo = new ObjectAttribute();
        geo.setName(DISCRIMINATOR_GEO);

        final StringAttribute crs = new StringAttribute();
        crs.setName(DISCRIMINATOR_CRS);
        crs.setValue("WGS84");

        final Set<AbstractAttribute<?>> atts = new HashSet<>();
        atts.add(crs);
        geo.setValue(atts);

        pCar.getProperties().add(geo);
    }

    /**
     * @param pCar
     *            {@link Car} with conflicting attributes
     */
    private void addConflictAttributes(final Car pCar) {
        // Namespace or fragment name
        final ObjectAttribute org = new ObjectAttribute();
        org.setName(DISCRIMINATOR_ORG);

        final StringArrayAttribute description = new StringArrayAttribute();
        description.setName(DISCRIMINATOR_DESCRIPTION);
        description.setValue(Arrays.array("desc1", "desc2"));

        final Set<AbstractAttribute<?>> atts = new HashSet<>();
        atts.add(description);
        org.setValue(atts);

        pCar.getProperties().add(org);
    }

}