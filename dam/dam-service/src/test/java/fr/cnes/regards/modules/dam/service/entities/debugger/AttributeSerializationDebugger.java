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
package fr.cnes.regards.modules.dam.service.entities.debugger;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTest;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.gson.entities.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.dam.service.models.exception.ImportException;
import fr.cnes.regards.modules.dam.service.models.xml.XmlImportHelper;

/**
 * Tools for debugging (de)serialization problems
 *
 * @author Marc Sordi
 *
 */
@Ignore
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = { AttributeSerializationDebugger.StaticConfiguration.class })
@MultitenantTransactional
public class AttributeSerializationDebugger extends AbstractDaoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeSerializationDebugger.class);

    private static final Path USE_CASE_PATH = Paths.get("src", "test", "resources", "debugger");

    @Autowired
    private Gson gson;

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory gsonFactory;

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "fr.cnes.regards.modules")
    static class StaticConfiguration {
    }

    /**
     * Import a model
     * @param xmlFilePath model as XML file
     * @return list of {@link ModelAttrAssoc}
     */
    private List<ModelAttrAssoc> importModel(Path xmlFilePath) {
        try {
            return XmlImportHelper.importModel(Files.newInputStream(xmlFilePath), new ArrayList<>());
        } catch (IOException | ImportException e) {
            LOGGER.debug("Import of model failed", e);
            Assert.fail();
        }
        // never reached because test will fail first
        return null;
    }

    /**
     * Read a JSON file
     * @param jsonFilePath JSON file path
     * @return String representation of the JSON
     */
    private String readJson(Path jsonFilePath) {

        if (Files.exists(jsonFilePath)) {
            try (JsonReader reader = new JsonReader(new FileReader(jsonFilePath.toFile()))) {
                JsonElement el = Streams.parse(reader);
                return el.toString();
            } catch (IOException e) {
                String message = "Cannot read JSON contract";
                LOGGER.error(message, e);
                throw new AssertionError(message, e);
            }
        } else {
            String message = String.format("File does not exist : %s", jsonFilePath.toAbsolutePath().toString());
            LOGGER.error(message);
            throw new AssertionError(message);
        }
    }

    @Test
    public void useCase001() {

        // Define XML file
        Path xmlFilePath = USE_CASE_PATH.resolve(Paths.get("useCase001", "model-spire_fts_l2_light_chris.xml"));

        // Import model
        List<ModelAttrAssoc> assocs = importModel(xmlFilePath);
        Assert.assertNotNull(assocs);

        // Update gson factory
        for (ModelAttrAssoc assoc : assocs) {
            gsonFactory.registerAttribute(DEFAULT_TENANT, assoc.getAttribute());
        }

        // Define JSON file
        Path jsonFilePath = USE_CASE_PATH.resolve(Paths.get("useCase001", "elasticsearch response ok.json"));

        // Read JSON
        String jsonString = readJson(jsonFilePath);
        LOGGER.debug(jsonString);

        // Parse JSON
        DataObject dataObject = gson.fromJson(jsonString, DataObject.class);
        Assert.assertNotNull(dataObject);
    }

    @Test
    public void useCase001_ko() {

        // Define XML file
        Path xmlFilePath = USE_CASE_PATH.resolve(Paths.get("useCase001", "model-spire_fts_l2_light_chris.xml"));

        // Import model
        List<ModelAttrAssoc> assocs = importModel(xmlFilePath);
        Assert.assertNotNull(assocs);

        // Update gson factory
        for (ModelAttrAssoc assoc : assocs) {
            gsonFactory.registerAttribute(DEFAULT_TENANT, assoc.getAttribute());
        }

        // Define JSON file
        Path jsonFilePath = USE_CASE_PATH
                .resolve(Paths.get("useCase001", "elasticsearch response with two date ko.json"));

        // Read JSON
        String jsonString = readJson(jsonFilePath);
        LOGGER.debug(jsonString);

        // Parse JSON
        DataObject dataObject = gson.fromJson(jsonString, DataObject.class);
        Assert.assertNotNull(dataObject);
    }
}
