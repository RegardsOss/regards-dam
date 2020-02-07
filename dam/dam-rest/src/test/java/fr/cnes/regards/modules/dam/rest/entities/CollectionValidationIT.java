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
package fr.cnes.regards.modules.dam.rest.entities;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.domain.entities.attribute.AbstractAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.gson.entities.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.dam.rest.DamRestConfiguration;
import fr.cnes.regards.modules.dam.rest.models.ModelController;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;
import fr.cnes.regards.modules.dam.service.models.IModelService;

/**
 *
 * Test collection validation
 *
 * @author Marc Sordi
 *
 */
@DirtiesContext
@MultitenantTransactional
@ContextConfiguration(classes = { DamRestConfiguration.class })
public class CollectionValidationIT extends AbstractRegardsTransactionalIT {

    /**
     * {@link Model} service
     */
    @Autowired
    private IModelService modelService;

    /**
     * {@link IAttributeModelService} service
     */
    @Autowired
    private IAttributeModelService attributeModelService;

    /**
     * Attribute Adapter Factory
     */
    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory attributeAdapterFactory;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    /**
     * Import a model
     *
     * @param pFilename
     *            model to import from resources folder
     */
    private void importModel(final String pFilename) {

        Path filePath = Paths.get("src", "test", "resources", pFilename);

        RequestBuilderCustomizer customizer = customizer();
        customizer.expect(MockMvcResultMatchers.status().isCreated());

        performDefaultFileUpload(ModelController.TYPE_MAPPING + "/import", filePath, customizer,
                                 "Should be able to import a fragment");

        final List<AttributeModel> atts = attributeModelService.getAttributes(null, null, null);
        attributeAdapterFactory.refresh(getDefaultTenant(), atts);
    }

    /**
     * Instance with a simple single root attribute
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test(expected = AssertionError.class)
    public void testSimpleModel() throws ModuleException {
        importModel("simple-model.xml");

        final Model mission = modelService.getModelByName("MISSION");

        final Collection mission1 = new Collection(mission, null, "COL1", "SPOT");

        RequestBuilderCustomizer customizer = customizer();
        customizer.expect(MockMvcResultMatchers.status().isOk());
        performDefaultPost(CollectionController.TYPE_MAPPING, mission1, customizer, "...");
    }

    @Test
    public void test1CollectionWith() {
        importModel("modelTest1.xml");

    }

    @Test
    public void testPropertyCase() throws ModuleException {
        importModel("simple-model-label.xml");

        Model model = modelService.getModelByName("MISSION_WITH_LABEL");

        Collection collection = new Collection(model, getDefaultTenant(), "COL1", "mission");
        Set<AbstractAttribute<?>> atts = new HashSet<>();
        atts.add(AttributeBuilder.buildString("LABEL", "uppercaselabel"));
        collection.setProperties(atts);

        // Set multitenant factory tenant
        tenantResolver.forceTenant(getDefaultTenant());

        RequestBuilderCustomizer customizer = customizer();
        customizer.expect(MockMvcResultMatchers.status().isCreated());
        performDefaultPost(CollectionController.TYPE_MAPPING, collection, customizer,
                           "Failed to create a new collection");
    }
}
