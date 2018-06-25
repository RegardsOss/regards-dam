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
package fr.cnes.regards.modules.entities.gson.feature;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import com.google.gson.Gson;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.entities.domain.feature.CollectionFeature;
import fr.cnes.regards.modules.entities.gson.IAttributeHelper;
import fr.cnes.regards.modules.entities.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 *
 * Test feature serialization
 *
 * @author Marc Sordi
 *
 */
@TestPropertySource(locations = { "classpath:test.properties" },
        properties = { "spring.jpa.properties.hibernate.default_schema=feature" })
@MultitenantTransactional
public class EntityFeatureSerializationTests extends AbstractMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityFeatureSerializationTests.class);

    // Common properties
    protected static final String ABSTRACT = "abstract";

    // Galaxy properties
    protected static final String GALAXY = "galaxy";

    protected static final String MILKY_WAY = "Milky way";

    // Galaxy data
    protected static final String DATA = "data";

    protected static final String STAR_NB = "starnb";

    protected static final String PLANET_NB = "planetnb";

    @Autowired
    protected IModelService modelService;

    @Autowired
    protected IAttributeModelService attributeModelService;

    @Autowired
    protected MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;

    @Autowired
    protected IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    protected Gson gson;

    protected Model galaxyModel;

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = { "fr.cnes.regards.modules" })
    static class ScanningConfiguration {

        @Bean
        public IAttributeHelper attributeHelper() {
            return Mockito.mock(IAttributeHelper.class);
        }
    }

    @Before
    public void before() throws ModuleException {

        // - Import models
        // COLLECTION : Galaxy
        galaxyModel = modelService.importModel(this.getClass().getResourceAsStream("collection_galaxy.xml"));

        // - Refresh attribute factory
        List<AttributeModel> atts = attributeModelService.getAttributes(null, null, null);
        gsonAttributeFactory.refresh(DEFAULT_TENANT, atts);
    }

    @Test
    public void serializeCollectionFeature() {

        CollectionFeature feature = new CollectionFeature(DEFAULT_TENANT, "My first collection");

        // Set dynamic properties
        feature.getProperties().add(AttributeBuilder.buildString(GALAXY, MILKY_WAY));
        feature.getProperties().add(AttributeBuilder
                .buildString(ABSTRACT, "The Milky Way is the galaxy that contains our Solar System."));
        feature.getProperties().add(AttributeBuilder.buildObject(DATA, AttributeBuilder.buildInteger(STAR_NB, 300),
                                                                 AttributeBuilder.buildInteger(PLANET_NB, 100)));

        // Set tags
        feature.addTag("first tag");

        // Serialize
        String result = gson.toJson(feature);
        LOGGER.debug(result);
        // Deserialize
        CollectionFeature parsed = gson.fromJson(result, CollectionFeature.class);
        Assert.assertNotNull(parsed);
    }

    @Test
    public void serializeCollection() {

        Collection collection = new Collection(galaxyModel, DEFAULT_TENANT, "My second collection");

        // Set dynamic properties
        collection.addProperty(AttributeBuilder.buildString(GALAXY, MILKY_WAY));
        collection.addProperty(AttributeBuilder
                .buildString(ABSTRACT, "The Milky Way is the galaxy that contains our Solar System."));
        collection.addProperty(AttributeBuilder.buildObject(DATA, AttributeBuilder.buildInteger(STAR_NB, 300),
                                                            AttributeBuilder.buildInteger(PLANET_NB, 100)));

        // Set tags
        collection.getTags().add("second tag");

        // Serialize
        String result = gson.toJson(collection);
        LOGGER.debug(result);
        // Deserialize
        Collection parsed = gson.fromJson(result, Collection.class);
        Assert.assertNotNull(parsed);
    }
}
