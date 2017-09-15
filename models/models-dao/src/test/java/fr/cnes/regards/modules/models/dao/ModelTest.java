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
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * @author Marc Sordi
 *
 */
public class ModelTest extends AbstractModelTest {

    /**
     * Logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ModelTest.class);

    /**
     * Create a model, attach an attribute model and try to retrieve
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Create a model of type COLLECTION - DOCUMENT, DATA or DATASET are also available -")
    public void createModel() {
        final Model model = createModel("MISSION", "Scientist mission collection", EntityType.COLLECTION);

        // Create attribute
        AttributeModel attModel = AttributeModelBuilder.build("NAME", AttributeType.STRING, "ForTests").get();
        attModel = saveAttribute(attModel);

        // Create model attribute
        final ModelAttrAssoc modelAtt = new ModelAttrAssoc(attModel, model);
        modelAttributeRepository.save(modelAtt);

        // Retrieve all model attributes
        final Iterable<ModelAttrAssoc> directAtts = modelAttributeRepository.findByModelId(model.getId());
        Assert.assertEquals(1, Iterables.size(directAtts));
    }

    // TODO try to delete a non empty model
}
