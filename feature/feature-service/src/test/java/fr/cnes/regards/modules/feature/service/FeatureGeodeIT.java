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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureMetadata;
import fr.cnes.regards.modules.feature.dto.FeatureSessionMetadata;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureUpdateRequestEvent;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;

/**
 * Test feature mutation based on null property values.
 *
 * @author Marc SORDI
 *
 */
//@Ignore
@TestPropertySource(
        properties = { "spring.jpa.properties.hibernate.default_schema=feature_geode", "regards.amqp.enabled=true" },
        locations = { "classpath:regards_perf.properties", "classpath:batch.properties" })
@ActiveProfiles(value = { "testAmqp" })
public class FeatureGeodeIT extends AbstractFeatureMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureGeodeIT.class);

    private static final Integer NB_FEATURES = 1;

    private static final String PROVIDER_ID_FORMAT = "F%05d";

    private String modelName;

    @Autowired
    private IPublisher publisher;

    @Before
    public void prepareContext() throws InterruptedException {
        super.before();

        // Manage model
        modelName = mockModelClient(GeodeProperties.getGeodeModel(), this.getCps(), this.getFactory(),
                                    this.getDefaultTenant(), this.getModelAttrAssocClientMock());
        Thread.sleep(5_000);
    }

    @Test
    public void createAndUpdateTest() throws InterruptedException {

        // Request creations
        long creationStart = System.currentTimeMillis();
        requestCreation();
        // Wait for request handling and feature creation
        waitFeature(NB_FEATURES, null, 300_000);
        LOGGER.info(">>>>>>>>>>>>>>>>> {} creation requests done in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - creationStart);

        // Request update
        long updateStart = System.currentTimeMillis();
        OffsetDateTime requestDate = requestUpdate();
        // Wait for request handling and feature update
        waitFeature(NB_FEATURES, requestDate, 300_000);
        LOGGER.info(">>>>>>>>>>>>>>>>> {} update requests done in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - updateStart);

        LOGGER.info(">>>>>>>>>>>>>>>>> {} creation requests and {} update requests done in {} ms", NB_FEATURES,
                    NB_FEATURES, System.currentTimeMillis() - creationStart);
    }

    private void requestCreation() {
        FeatureSessionMetadata metadata = FeatureSessionMetadata.build("sessionOwner", "session", PriorityLevel.AVERAGE,
                                                                       Lists.emptyList());

        long creationStart = System.currentTimeMillis();
        List<FeatureCreationRequestEvent> events = new ArrayList<>();
        int bulk = 0;
        for (int i = 1; i <= NB_FEATURES; i++) {
            bulk++;
            Feature feature = Feature.build(String.format(PROVIDER_ID_FORMAT, i), null, IGeometry.unlocated(),
                                            EntityType.DATA, modelName);
            GeodeProperties.addGeodeProperties(feature);
            events.add(FeatureCreationRequestEvent.build(metadata, feature));

            if (bulk == properties.getMaxBulkSize()) {
                publish(events);
                events.clear();
                bulk = 0;
            }
        }

        if (bulk > 0) {
            publish(events);
        }

        LOGGER.info(">>>>>>>>>>>>>>>>> {} creation requests published in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - creationStart);
    }

    private OffsetDateTime requestUpdate() {
        long updateStart = System.currentTimeMillis();
        OffsetDateTime requestDate = OffsetDateTime.now();
        FeatureMetadata featureMetadata = FeatureMetadata.build(PriorityLevel.AVERAGE, new ArrayList<>());
        List<FeatureUpdateRequestEvent> uEvents = new ArrayList<>();
        for (int i = 1; i <= NB_FEATURES; i++) {
            String id = String.format(PROVIDER_ID_FORMAT, i);
            Feature feature = Feature.build(id, getURN(id), IGeometry.unlocated(), EntityType.DATA, modelName);
            GeodeProperties.addGeodeUpdateProperties(feature);
            uEvents.add(FeatureUpdateRequestEvent.build(featureMetadata, feature, requestDate));
        }
        LOGGER.info(">>>>>>>>>>>>>>>>> {} update requests batched in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - updateStart);
        publisher.publish(uEvents);
        LOGGER.info(">>>>>>>>>>>>>>>>> {} update requests published in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - updateStart);

        return requestDate;
    }

    private void publish(List<? extends ISubscribable> events) {
        long creationStart = System.currentTimeMillis();
        publisher.publish(events);
        LOGGER.info(">>>>>>>>>>>>>>>>> {} event published in {} ms", events.size(),
                    System.currentTimeMillis() - creationStart);
    }

    private FeatureUniformResourceName getURN(String id) {
        UUID uuid = UUID.nameUUIDFromBytes(id.getBytes());
        return FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, EntityType.DATA, getDefaultTenant(), uuid,
                                                1);
    }
}
