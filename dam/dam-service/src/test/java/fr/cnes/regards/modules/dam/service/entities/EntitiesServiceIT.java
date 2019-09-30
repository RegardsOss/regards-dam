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
package fr.cnes.regards.modules.dam.service.entities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.plugin.entities.MinDateComputePlugin;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.model.dao.IModelRepository;
import fr.cnes.regards.modules.model.domain.IComputedAttribute;
import fr.cnes.regards.modules.model.domain.Model;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=entities",
        "regards.dam.post.aip.entities.to.storage=false" }, locations = "classpath:es.properties")
@MultitenantTransactional
public class EntitiesServiceIT extends AbstractMultitenantServiceTest {

    private static final String datasetModelFileName = "datasetModel.xml";

    @Autowired
    private IEntitiesService entitiesService;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepos;

    @Autowired
    private IDatasetRepository datasetRepository;

    @Autowired
    private IModelRepository modelRepository;

    private Dataset dataset;

    @PersistenceContext
    private EntityManager em;

    @Before
    public void init() throws ModuleException {

        datasetRepository.deleteAll();
        modelRepository.deleteAll();
        pluginConfRepos.deleteAll();

        // then import the model of dataset
        importModel(datasetModelFileName);
        // instantiate the dataset
        Model datasetModel = modelService.getModelByName("datasetModel");
        dataset = new Dataset(datasetModel, "PROJECT", "DSFUN", "Test pour le fun");
        dataset.setLicence("pLicence");
    }

    @Test
    public void testGetComputationPlugins() throws ModuleException {
        Set<IComputedAttribute<Dataset, ?>> results = entitiesService.getComputationPlugins(dataset);
        for (IComputedAttribute<Dataset, ?> plugin : results) {
            Assert.assertEquals(MinDateComputePlugin.class, plugin.getClass());
        }
    }

    @Test
    public void testImportModel() throws ModuleException {
        importModel("model-LaPollioDesDatasets.xml");
    }

    /**
     * Import model definition file from resources directory
     *
     * @param pFilename filename
     * @throws ModuleException if error occurs
     */
    private void importModel(String pFilename) throws ModuleException {
        try {
            final InputStream input = Files.newInputStream(Paths.get("src", "test", "resources", pFilename));
            modelService.importModel(input);
        } catch (IOException e) {
            e.printStackTrace();
            String errorMessage = "Cannot import " + pFilename;
            throw new AssertionError(errorMessage);
        }
    }
}
