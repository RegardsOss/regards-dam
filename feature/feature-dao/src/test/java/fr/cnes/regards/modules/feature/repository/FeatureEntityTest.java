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
package fr.cnes.regards.modules.feature.repository;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTest;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.dto.properties.ObjectProperty;
import fr.cnes.regards.modules.model.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.model.service.exception.ImportException;
import fr.cnes.regards.modules.model.service.xml.XmlImportHelper;

/**
 * @author Marc SORDI
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature_dao",
        "spring.jpa.properties.hibernate.jdbc.batch_size=1024", "spring.jpa.properties.hibernate.order_inserts=true" },
        locations = { "classpath:regards_perf.properties" })
@ContextConfiguration(classes = FeatureDaoConfiguration.class)
public class FeatureEntityTest extends AbstractDaoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureEntityTest.class);

    private static final Integer NB_FEATURES = 10000;

    private static final Integer BULK_SIZE = 1000;

    @Autowired
    private IFeatureEntityRepository entityRepo;

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory factory;

    @Before
    public void clean() {
        entityRepo.deleteAllInBatch();
        mockModelClient("feature_dao_model.xml");
        mockModelClient("model_geode.xml");
    }

    @Test
    public void createFeatures() {

        String format = "F%05d";
        List<FeatureEntity> entities = new ArrayList<>();
        for (int i = 1; i <= NB_FEATURES; i++) {
            String id = String.format(format, i);
            Feature feature = Feature.build(id, getURN(id), IGeometry.unlocated(), EntityType.DATA, "model");
            feature.addProperty(IProperty.buildString("data_type", "TYPE01"));
            feature.addProperty(IProperty.buildObject("file_characterization",
                                                      IProperty.buildBoolean("valid", Boolean.TRUE)));
            entities.add(FeatureEntity.build("sessionOwner", "session", feature, null));
        }

        long creationStart = System.currentTimeMillis();
        entityRepo.saveAll(entities);
        LOGGER.info(">>>>>>>>>>>>>>>>> {} creation requests done in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - creationStart);
    }

    @Test
    public void createBulkFeatures() {

        long creationStart = System.currentTimeMillis();
        String format = "F%05d";
        List<FeatureEntity> entities = new ArrayList<>();
        int bulk = 0;
        long bulkCreationStart = System.currentTimeMillis();
        for (int i = 1; i <= NB_FEATURES; i++) {
            bulk++;
            String id = String.format(format, i);
            Feature feature = Feature.build(id, getURN(id), IGeometry.unlocated(), EntityType.DATA, "model");
            addGeodeProperties(feature);
            entities.add(FeatureEntity.build("sessionOwner", "session", feature, null));

            if (bulk == BULK_SIZE) {
                bulkCreationStart = System.currentTimeMillis();
                entityRepo.saveAll(entities);
                LOGGER.info(">>>>>>>>>>>>>>>>> {} creation requests saved in {} ms", BULK_SIZE,
                            System.currentTimeMillis() - bulkCreationStart);
                entities.clear();
                bulk = 0;
            }
        }

        if (bulk > 0) {
            bulkCreationStart = System.currentTimeMillis();
            entityRepo.saveAll(entities);
            LOGGER.info(">>>>>>>>>>>>>>>>> {} creation requests saved in {} ms", bulk,
                        System.currentTimeMillis() - bulkCreationStart);
        }

        LOGGER.info(">>>>>>>>>>>>>>>>> {} creation requests saved in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - creationStart);
    }

    private FeatureUniformResourceName getURN(String id) {
        UUID uuid = UUID.nameUUIDFromBytes(id.getBytes());
        return FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, EntityType.DATA, getDefaultTenant(), uuid,
                                                1);
    }

    private void addGeodeProperties(Feature feature) {
        // System
        ObjectProperty system = IProperty.buildObject("system", IProperty.buildInteger("filesize", 8648),
                                                      IProperty.buildDate("creation_date", OffsetDateTime.now()),
                                                      IProperty.buildDate("modification_date", OffsetDateTime.now()),
                                                      IProperty.buildStringArray("urls", "file://home/geode/test.tar"),
                                                      IProperty.buildString("filename", "test.tar"),
                                                      IProperty.buildString("checksum",
                                                                            "4e188bd8a6288164c25c3728ce394927"),
                                                      IProperty.buildString("extension", "tar"));
        // File infos
        ObjectProperty fileInfos = IProperty.buildObject("file_infos", IProperty.buildString("type", "L0A_LR_Packet"),
                                                         IProperty.buildString("nature", "TM"),
                                                         IProperty.buildString("date_type", "BEGINEND"),
                                                         IProperty.buildString("level", "L0A"),
                                                         IProperty.buildDate("production_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("utc_start_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("utc_end_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("tai_start_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("tai_end_date", OffsetDateTime.now()),
                                                         IProperty.buildBoolean("valid", true));
        // Ground segment
        ObjectProperty groundSegment = IProperty
                .buildObject("ground_segment", IProperty.buildBoolean("sended", true),
                             IProperty.buildDate("sending_date", OffsetDateTime.now()),
                             IProperty.buildStringArray("recipients", "JPL", "REGARDS"),
                             IProperty.buildBoolean("archived", true),
                             IProperty.buildDate("archiving_date", OffsetDateTime.now()),
                             IProperty.buildBoolean("public", false), IProperty.buildBoolean("distributed", false),
                             IProperty.buildBoolean("restored", false), IProperty.buildString("state", "NOT ARCHIVED"));

        // SWOT
        ObjectProperty swot = IProperty
                .buildObject("swot", IProperty.buildString("CRID", "crid"),
                             IProperty.buildInteger("product_counter", 1),
                             IProperty.buildBoolean("is_last_version", true), IProperty.buildString("station", "KUX"),
                             IProperty.buildDate("day_date", OffsetDateTime.now()), IProperty.buildInteger("cycle", 23),
                             IProperty.buildInteger("pass", 125), IProperty.buildInteger("tile", 25),
                             IProperty.buildString("tile_side", "Full"), IProperty.buildString("granule_type", "Cycle"),
                             IProperty.buildStringArray("continent_id", "eu"),
                             IProperty.buildString("bassin_id", "bass1"));
        // CORPUS
        ObjectProperty corpus = IProperty.buildObject("corpus", IProperty.buildInteger("corpus_id", 10),
                                                      IProperty.buildString("corpus_lot", "lot2"));

        feature.setProperties(IProperty.set(system, fileInfos, groundSegment, swot, corpus));
    }

    /**
     * Mock model client importing model specified by its filename
     * @param filename model filename found using {@link Class#getResourceAsStream(String)}
     * @return mocked model name
     */
    protected String mockModelClient(String filename) {

        try (InputStream input = this.getClass().getResourceAsStream(filename)) {
            // Import model
            Iterable<ModelAttrAssoc> assocs = XmlImportHelper.importModel(input, null);

            // Translate to resources and attribute models and extract model name
            String modelName = null;
            List<AttributeModel> atts = new ArrayList<>();
            List<EntityModel<ModelAttrAssoc>> resources = new ArrayList<>();
            for (ModelAttrAssoc assoc : assocs) {
                atts.add(assoc.getAttribute());
                resources.add(new EntityModel<ModelAttrAssoc>(assoc));
                if (modelName == null) {
                    modelName = assoc.getModel().getName();
                }
            }

            // Property factory registration
            factory.registerAttributes(getDefaultTenant(), atts);

            return modelName;
        } catch (IOException | ImportException e) {
            String errorMessage = "Cannot import model";
            LOGGER.debug(errorMessage);
            throw new AssertionError(errorMessage);
        }
    }
}
