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
package fr.cnes.regards.modules.feature.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureMetadata;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureUpdateRequestEvent;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.model.dto.properties.IProperty;

/**
 * Test feature mutation based on null property values.
 *
 * @author Marc SORDI
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature_perf",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true", "regards.scheduler.pool.size=4" })
@ActiveProfiles(value = { "testAmqp" })
public class FeaturePerformanceIT extends AbstractFeatureMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeaturePerformanceIT.class);

    private static final Integer NB_FEATURES = 30;

    @Autowired
    private IPublisher publisher;

    @Test
    public void createAndUpdateTest() throws InterruptedException {

        String format = "F%05d";

        // Register creation requests
        FeatureMetadata metadata = FeatureMetadata.build("sessionOwner", "session", Lists.emptyList());
        String modelName = mockModelClient("feature_mutation_model.xml");

        Thread.sleep(5_000);

        long creationStart = System.currentTimeMillis();
        for (int i = 1; i <= NB_FEATURES; i++) {
            Feature feature = Feature.build(String.format(format, i), null, IGeometry.unlocated(), EntityType.DATA,
                                            modelName);
            feature.addProperty(IProperty.buildString("data_type", "TYPE01"));
            feature.addProperty(IProperty.buildObject("file_characterization",
                                                      IProperty.buildBoolean("valid", Boolean.TRUE)));
            publisher.publish(FeatureCreationRequestEvent.build(metadata, feature));
        }

        // Wait for feature creation
        waitFeature(NB_FEATURES, null, 1_200_000);
        LOGGER.info(">>>>>>>>>>>>>>>>> {} creation requests done in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - creationStart);

        // Register update requests

        long updateStart = System.currentTimeMillis();
        OffsetDateTime requestDate = OffsetDateTime.now();
        for (int i = 1; i <= NB_FEATURES; i++) {
            String id = String.format(format, i);
            Feature feature = Feature.build(id, getURN(id), IGeometry.unlocated(), EntityType.DATA, modelName);
            feature.addProperty(IProperty.buildObject("file_characterization",
                                                      IProperty.buildBoolean("valid", Boolean.FALSE),
                                                      IProperty.buildDate("invalidation_date", OffsetDateTime.now())));
            publisher.publish(FeatureUpdateRequestEvent.build(feature, requestDate));
        }

        // Wait for feature update
        waitFeature(NB_FEATURES, requestDate, 1_200_000);
        LOGGER.info(">>>>>>>>>>>>>>>>> {} update requests done in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - updateStart);

        LOGGER.info(">>>>>>>>>>>>>>>>> {} creation requests and {} update requests done in {} ms", NB_FEATURES,
                    NB_FEATURES, System.currentTimeMillis() - creationStart);
    }

    private FeatureUniformResourceName getURN(String id) {
        UUID uuid = UUID.nameUUIDFromBytes(id.getBytes());
        return FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, EntityType.DATA, getDefaultTenant(), uuid,
                                                1);
    }
}
