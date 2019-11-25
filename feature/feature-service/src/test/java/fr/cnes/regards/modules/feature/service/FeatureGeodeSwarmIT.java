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

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Ignore;
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
import fr.cnes.regards.modules.feature.dto.FeatureSessionMetadata;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;

/**
 * Test feature mutation based on null property values.
 *
 * @author Marc SORDI
 *
 */
@Ignore
@TestPropertySource(
        properties = { "spring.jpa.properties.hibernate.default_schema=feature_geode_swarm",
                "regards.amqp.enabled=true", "regards.tenant=project1" },
        locations = { "classpath:regards_geode.properties", "classpath:batch.properties" })
@ActiveProfiles(value = { "testAmqp", "nohandler", "noscheduler" })
public class FeatureGeodeSwarmIT extends AbstractFeatureMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureGeodeSwarmIT.class);

    private static final Integer NB_FEATURES = 100_000;

    private static final String PROVIDER_ID_FORMAT = "F%05d";

    private static final Integer PUBLISH_BULK_SIZE = 2000;

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
    public void requestCreation() {
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

            if (bulk == PUBLISH_BULK_SIZE) {
                publish(events, "creation", i, NB_FEATURES);
                events.clear();
                bulk = 0;
            }
        }

        if (bulk > 0) {
            publish(events, "creation", NB_FEATURES, NB_FEATURES);
        }

        LOGGER.info(">>>>>>>>>>>>>>>>> {} creation requests published in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - creationStart);
    }

    private void publish(List<? extends ISubscribable> events, String type, int count, int total) {
        long creationStart = System.currentTimeMillis();
        publisher.publish(events);
        LOGGER.info(">>>>>>>>>>>>>>>>> {} {} events published in {} ms ({}/{})", events.size(), type,
                    System.currentTimeMillis() - creationStart, count, total);
    }
}
