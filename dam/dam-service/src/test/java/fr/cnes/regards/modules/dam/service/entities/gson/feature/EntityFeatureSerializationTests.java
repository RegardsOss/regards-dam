/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.service.entities.gson.feature;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.google.gson.Gson;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.domain.entities.feature.CollectionFeature;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.model.service.IAttributeModelService;
import fr.cnes.regards.modules.model.service.IModelService;

/**
 *
 * Test feature serialization
 *
 * @author Marc Sordi
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature" },
        locations = "classpath:es.properties")
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

    @Before
    public void before() throws ModuleException {

        // - Import models
        // COLLECTION : Galaxy
        galaxyModel = modelService.importModel(this.getClass().getResourceAsStream("collection_galaxy.xml"));

        // - Refresh attribute factory
        List<AttributeModel> atts = attributeModelService.getAttributes(null, null, null, null);
        gsonAttributeFactory.refresh(getDefaultTenant(), atts);
    }

    @Test
    public void serializeCollectionFeature() {

        CollectionFeature feature = new CollectionFeature(getDefaultTenant(), "FIRST", "My first collection");

        // Set dynamic properties
        feature.getProperties().add(IProperty.buildString(GALAXY, MILKY_WAY));
        feature.getProperties()
                .add(IProperty.buildString(ABSTRACT, "The Milky Way is the galaxy that contains our Solar System."));
        feature.getProperties().add(IProperty.buildObject(DATA, IProperty.buildInteger(STAR_NB, 300),
                                                          IProperty.buildInteger(PLANET_NB, 100)));

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

        Collection collection = new Collection(galaxyModel, getDefaultTenant(), "SECOND", "My second collection");

        // Set dynamic properties
        collection.addProperty(IProperty.buildString(GALAXY, MILKY_WAY));
        collection.addProperty(IProperty.buildString(ABSTRACT,
                                                     "The Milky Way is the galaxy that contains our Solar System."));
        collection.addProperty(IProperty.buildObject(DATA, IProperty.buildInteger(STAR_NB, 300),
                                                     IProperty.buildInteger(PLANET_NB, 100)));

        // Set tags
        collection.addTags("second tag");

        // Serialize
        String result = gson.toJson(collection);
        LOGGER.debug(result);
        // Deserialize
        Collection parsed = gson.fromJson(result, Collection.class);
        Assert.assertNotNull(parsed);
    }
}
