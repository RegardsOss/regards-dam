/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.service.models;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.dao.models.IModelAttrAssocRepository;
import fr.cnes.regards.modules.dam.dao.models.IModelRepository;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.domain.models.attributes.Fragment;
import fr.cnes.regards.modules.dam.service.models.exception.FragmentAttributeException;

/**
 * @author Marc Sordi
 */
public class ModelServiceTest {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelServiceTest.class);

    /**
     * Sample model name
     */
    private static final String MODEL_NAME = "model";

    /**
     * Sample attribute model name
     */
    private static final String ATT_MOD_NAME = "attmod";

    /**
     * Model repository
     */
    private IModelRepository mockModelR;

    /**
     * Model attribute repository
     */
    private IModelAttrAssocRepository mockModelAttR;

    /**
     * Attribute model service
     */
    private IAttributeModelService mockAttModelS;

    /**
     * Model and model attribute services
     */
    private ModelService modelService;

    private IPluginService mockPluginService;

    @Before
    public void beforeTest() {
        mockModelR = Mockito.mock(IModelRepository.class);
        mockModelAttR = Mockito.mock(IModelAttrAssocRepository.class);
        mockAttModelS = Mockito.mock(IAttributeModelService.class);
        mockPluginService = Mockito.mock(IPluginService.class);
        modelService = new ModelService(mockModelR, mockModelAttR, mockAttModelS, mockPluginService);
    }

    @Test(expected = EntityInvalidException.class)
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Test unexpected model creation")
    public void createUnexpectedModelTest() throws ModuleException {
        final Model model = new Model();
        model.setId(1L);
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);
        modelService.createModel(model);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Test model creation with conflict")
    public void createAlreadyExistsModelTest() throws ModuleException {
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);

        Mockito.when(mockModelR.findByName(MODEL_NAME)).thenReturn(model);

        modelService.createModel(model);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Test model creation")
    public void createModelTest() throws ModuleException {
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);

        Mockito.when(mockModelR.findByName(MODEL_NAME)).thenReturn(null);
        Mockito.when(mockModelR.save(model)).thenReturn(model);

        Assert.assertNotNull(modelService.createModel(model));
    }

    @Test(expected = EntityNotFoundException.class)
    public void getUnknownModelTest() throws ModuleException {
        final Long modelId = 1L;

        Mockito.when(mockModelR.exists(modelId)).thenReturn(false);

        modelService.getModel(modelId);
    }

    @Test
    public void getModelTest() throws ModuleException {
        final Long modelId = 1L;
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);

        Mockito.when(mockModelR.exists(modelId)).thenReturn(true);
        Mockito.when(mockModelR.findOne(modelId)).thenReturn(model);

        Assert.assertNotNull(modelService.getModel(modelId));
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateUnexpectedModelTest() throws ModuleException {
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);
        modelService.updateModel(MODEL_NAME, model);
    }

    @Test(expected = EntityInconsistentIdentifierException.class)
    public void updateInconsistentModelTest() throws ModuleException {
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);
        model.setId(2L);

        modelService.updateModel("toto", model);
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateUnknownModelTest() throws ModuleException {
        final Long modelId = 1L;
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);
        model.setId(modelId);

        Mockito.when(mockModelR.exists(modelId)).thenReturn(false);

        modelService.updateModel(MODEL_NAME, model);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Test model update")
    public void updateModelTest() throws ModuleException {
        final Long modelId = 1L;
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);
        model.setId(modelId);

        Mockito.when(mockModelR.exists(modelId)).thenReturn(true);
        Mockito.when(mockModelR.save(model)).thenReturn(model);

        Assert.assertNotNull(modelService.updateModel(MODEL_NAME, model));
    }

    /**
     * Do not bind an attribute that is part of fragment
     * @throws ModuleException if error occurs!
     */
    @Test(expected = FragmentAttributeException.class)
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Test error occurs binding attribute that is part of a fragment")
    public void bindAttributeToModelTest() throws ModuleException {

        final Long modelId = 1L;
        final Model model = new Model();
        model.setId(modelId);
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);

        final Fragment frag = Fragment.buildFragment("FRAG", null);
        final Long attId = 10L;
        final AttributeModel attModel = AttributeModelBuilder.build(ATT_MOD_NAME, AttributeType.STRING, "ForTests")
                .fragment(frag).withId(attId).get();

        final ModelAttrAssoc modAtt = new ModelAttrAssoc(attModel, model);

        Mockito.when(mockModelR.findByName(MODEL_NAME)).thenReturn(model);
        Mockito.when(mockAttModelS.isFragmentAttribute(attId)).thenReturn(true);

        modelService.bindAttributeToModel(MODEL_NAME, modAtt);
    }

    // TODO do not rebind an attribute

    /**
     * Do not unbind an attribute that is part of fragment
     * @throws ModuleException if error occurs!
     */
    @Test(expected = FragmentAttributeException.class)
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Test error occurs unbinding attribute that is part of a fragment")
    public void unbindAttributeFromModelTest() throws ModuleException {

        final Long modelId = 1L;
        final Model model = new Model();
        model.setId(modelId);
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);

        final Fragment frag = Fragment.buildFragment("FR2AG", null);
        final Long attId = 10L;
        final AttributeModel attModel = AttributeModelBuilder.build(ATT_MOD_NAME, AttributeType.STRING, "ForTests")
                .fragment(frag).withId(attId).withPatternRestriction(".*");

        final Long modAttId = 10L;
        final ModelAttrAssoc modAtt = new ModelAttrAssoc(attModel, model);
        modAtt.setId(modAttId);

        Mockito.when(mockModelR.exists(modelId)).thenReturn(true);
        Mockito.when(mockModelR.findOne(modelId)).thenReturn(model);
        Mockito.when(mockModelAttR.findOne(modAttId)).thenReturn(modAtt);
        Mockito.when(mockAttModelS.isFragmentAttribute(attId)).thenReturn(true);

        modelService.unbindAttributeFromModel(MODEL_NAME, modAttId);
    }

    /**
     * Test model export
     * @throws ModuleException if error occurs!
     */
    @Test
    public void exportModelTest() throws ModuleException {

        final Long modelId = 1L;
        String modelName = "sample";
        final Model model = new Model();
        model.setId(modelId);
        model.setName(modelName);
        model.setDescription("Model description");
        model.setType(EntityType.COLLECTION);

        final List<ModelAttrAssoc> modelAttrAssocs = new ArrayList<>();

        // Attribute #1 in default fragment
        AttributeModel attMod = AttributeModelBuilder.build("att_string", AttributeType.STRING, "ForTests")
                .fragment(Fragment.buildDefault()).withoutRestriction();
        ModelAttrAssoc modAtt = new ModelAttrAssoc(attMod, model);
        modelAttrAssocs.add(modAtt);

        // Attribute #2 in default fragment
        attMod = AttributeModelBuilder.build("att_boolean", AttributeType.BOOLEAN, "ForTests")
                .fragment(Fragment.buildDefault()).withoutRestriction();
        modAtt = new ModelAttrAssoc(attMod, model);
        modelAttrAssocs.add(modAtt);

        // Geo fragment
        final Fragment geo = Fragment.buildFragment("GEO", "Geographic information");

        // Attribute #3 in geo fragment
        attMod = AttributeModelBuilder.build("CRS", AttributeType.STRING, "ForTests").fragment(geo)
                .withEnumerationRestriction("Earth", "Mars", "Venus");
        modAtt = new ModelAttrAssoc(attMod, model);
        modelAttrAssocs.add(modAtt);

        // Geo fragment
        final Fragment contact = Fragment.buildFragment("Contact", "Contact information");

        // Attribute #5 in contact fragment
        attMod = AttributeModelBuilder.build("Phone", AttributeType.STRING, "ForTests").fragment(contact)
                .withPatternRestriction("[0-9 ]{10}");
        modAtt = new ModelAttrAssoc(attMod, model);
        modelAttrAssocs.add(modAtt);

        // Attribute #6 in contact fragment
        attMod = AttributeModelBuilder.build("date", AttributeType.DATE_ISO8601, "ForTests").fragment(contact)
                .withoutRestriction();
        modAtt = new ModelAttrAssoc(attMod, model);
        modelAttrAssocs.add(modAtt);

        // Attribute #7 (computed) in default fragment
        attMod = AttributeModelBuilder.build("value_sum", AttributeType.LONG, "ForTests").defaultFragment()
                .withoutRestriction();
        modAtt = new ModelAttrAssoc(attMod, model);
        PluginConfiguration sumComputeConf = new PluginConfiguration();
        sumComputeConf.setLabel("SumComputeValue");
        sumComputeConf.setPluginClassName("fr.cnes.regards.modules.dam.plugin.entities.LongSumComputePlugin");
        sumComputeConf.setParameters(Lists.newArrayList(new PluginParameter("parameterAttributeName", "\"paramName\""),
                                                        // No parameter fragment => default value => "" => stripped as
                                                        // """"
                                                        new PluginParameter("parameterAttributeFragmentName", "\"\"")));

        modAtt.setComputationConf(sumComputeConf);
        modelAttrAssocs.add(modAtt);

        Mockito.when(mockModelR.findByName(modelName)).thenReturn(model);
        Mockito.when(mockModelR.findOne(modelId)).thenReturn(model);
        Mockito.when(mockModelAttR.findByModelName(modelName)).thenReturn(modelAttrAssocs);

        try {
            final OutputStream output = Files.newOutputStream(Paths.get("target", model.getName() + ".xml"));
            modelService.exportModel(modelName, output);
        } catch (IOException e) {
            LOGGER.debug("Cannot export fragment");
        }
    }

}