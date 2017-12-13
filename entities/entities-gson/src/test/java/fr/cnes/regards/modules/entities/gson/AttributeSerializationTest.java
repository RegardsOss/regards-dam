/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
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
public class AttributeSerializationTest {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeSerializationTest.class);

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
    private AttributeAdapterFactory factory;

    /**
     * Gson instance
     */
    private Gson gson;

    /**
     * Init GSON context
     */
    @Before
    public void initGson() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        factory = new AttributeAdapterFactory();

        // Register sub type(s)
        factory.registerSubtype(StringAttribute.class, DISCRIMINATOR_DESCRIPTION);
        factory.registerSubtype(ObjectAttribute.class, DISCRIMINATOR_GEO); // geo namespace
        factory.registerSubtype(StringAttribute.class, DISCRIMINATOR_CRS, DISCRIMINATOR_GEO);
        factory.registerSubtype(ObjectAttribute.class, DISCRIMINATOR_ORG); // org namespace
        factory.registerSubtype(StringArrayAttribute.class, DISCRIMINATOR_DESCRIPTION, DISCRIMINATOR_ORG);

        gsonBuilder.registerTypeAdapterFactory(factory);
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new OffsetDateTimeAdapter().nullSafe());
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

        Assert.assertEquals(1, parsedCar.getProperties().size());
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

        Assert.assertEquals(1, parsedCar.getProperties().size());

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

        Assert.assertEquals(2, parsedCar.getProperties().size());
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

        Set<AbstractAttribute<?>> attributes = parsedCar.getProperties();
        Assert.assertEquals(2, attributes.size());

        List<String> expectedRootAttributes = new ArrayList<>();
        expectedRootAttributes.add(DISCRIMINATOR_DESCRIPTION);
        expectedRootAttributes.add(DISCRIMINATOR_GEO);

        for (AbstractAttribute<?> att : attributes) {
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
        Car car = getCarWithRootAttribute();
        addNestedAttributes(car);
        addConflictAttributes(car);

        final String jsonCar = gson.toJson(car);
        LOGGER.info(jsonCar);
        final Car parsedCar = gson.fromJson(jsonCar, Car.class);

        Set<AbstractAttribute<?>> attributes = parsedCar.getProperties();

        final int expectedSize = 3;
        Assert.assertEquals(expectedSize, attributes.size());

        List<String> expectedRootAttributes = new ArrayList<>();
        expectedRootAttributes.add(DISCRIMINATOR_DESCRIPTION);
        expectedRootAttributes.add(DISCRIMINATOR_GEO);
        expectedRootAttributes.add(DISCRIMINATOR_ORG);

        for (AbstractAttribute<?> att : attributes) {
            Assert.assertTrue(expectedRootAttributes.contains(att.getName()));

            if (DISCRIMINATOR_DESCRIPTION.equals(att.getName())) {
                Assert.assertTrue(att instanceof StringAttribute);
            }

            if (DISCRIMINATOR_ORG.equals(att.getName())) {
                Assert.assertTrue(att instanceof ObjectAttribute);
                ObjectAttribute geo = (ObjectAttribute) att;

                for (AbstractAttribute<?> nested : geo.getValue()) {
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
        Car car = new Car();

        Set<AbstractAttribute<?>> attributes = new HashSet<>();

        StringAttribute description = new StringAttribute();
        description.setName(DISCRIMINATOR_DESCRIPTION);
        description.setValue("test description");
        attributes.add(description);

        car.setProperties(attributes);
        return car;
    }

    /**
     * @param pCar {@link Car}
     */
    private void addRuntimeRootAttribute(Car pCar) {

        BooleanAttribute runnable = new BooleanAttribute();
        runnable.setName(DISCRIMINATOR_RUNNABLE);
        runnable.setValue(true);
        pCar.addProperty(runnable);
    }

    /**
     * @param pCar {@link Car} with nested attributes
     */
    private void addNestedAttributes(Car pCar) {

        // Namespace or fragment name
        ObjectAttribute geo = new ObjectAttribute();
        geo.setName(DISCRIMINATOR_GEO);

        StringAttribute crs = new StringAttribute();
        crs.setName(DISCRIMINATOR_CRS);
        crs.setValue("WGS84");

        Set<AbstractAttribute<?>> atts = new HashSet<>();
        atts.add(crs);
        geo.setValue(atts);

        pCar.addProperty(geo);
    }

    /**
     * @param pCar {@link Car} with conflicting attributes
     */
    private void addConflictAttributes(Car pCar) {
        // Namespace or fragment name
        ObjectAttribute org = new ObjectAttribute();
        org.setName(DISCRIMINATOR_ORG);

        StringArrayAttribute description = new StringArrayAttribute();
        description.setName(DISCRIMINATOR_DESCRIPTION);
        description.setValue(Arrays.array("desc1", "desc2"));

        Set<AbstractAttribute<?>> atts = new HashSet<>();
        atts.add(description);
        org.setValue(atts);

        pCar.addProperty(org);
    }
}
