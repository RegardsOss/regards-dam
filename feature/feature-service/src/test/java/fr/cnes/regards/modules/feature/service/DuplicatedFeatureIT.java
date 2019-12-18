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

import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureDeletionRequest;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.service.request.FeatureStorageListener;
import fr.cnes.regards.modules.storage.client.RequestInfo;

/**
 * Test feature mutation based on null property values.
 *
 * @author Marc SORDI
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature_duplication",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp" })
public class DuplicatedFeatureIT extends AbstractFeatureMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DuplicatedFeatureIT.class);

    @Autowired
    private FeatureStorageListener listener;

    @Autowired
    private IPublisher publisher;

    @Test
    public void testDuplicatedFeatureCreationWithOverride() throws InterruptedException {
        List<FeatureCreationRequestEvent> events = new ArrayList<>();

        // publish a feature in creation
        super.initFeatureCreationRequestEvent(events, 1);
        events.get(0).getFeature().setId("id");
        publisher.publish(events);
        waitRequest(this.featureCreationRequestRepo, 1, 30000);
        this.featureCreationService.scheduleRequests();
        waitRequest(this.featureRepo, 1, 30000);

        // mock storage response to indicate creation succed
        FeatureEntity featureInDatabase = this.featureRepo.findAll().get(0);
        FeatureCreationRequest fcr = this.featureCreationRequestRepo.findAll().get(0);
        RequestInfo info = RequestInfo.build();
        fcr.setGroupId(info.getGroupId());
        fcr = this.featureCreationRequestRepo.save(fcr);
        this.listener.onStoreSuccess(Sets.newHashSet(info));

        // publish a duplicated feature creation
        publisher.publish(events);
        waitRequest(this.featureCreationRequestRepo, 1, 30000);
        this.featureCreationService.scheduleRequests();
        waitRequest(this.featureRepo, 2, 30000);

        // mock storage response to indicate creation succed
        fcr = this.featureCreationRequestRepo.findAll().get(0);
        fcr.setGroupId(info.getGroupId());
        fcr = this.featureCreationRequestRepo.save(fcr);
        this.listener.onStoreSuccess(Sets.newHashSet(info));

        // that must publish a FeatureDeletionRequestEvent
        waitRequest(this.featureDeletionRepo, 1, 30000);
        this.featureDeletionService.scheduleRequests();

        // mock the deletion succed for storage
        FeatureDeletionRequest fdr = this.featureDeletionRepo.findAll().get(0);
        fdr.setGroupId(info.getGroupId());
        fdr = this.featureDeletionRepo.save(fdr);
        this.listener.onDeletionSuccess(Sets.newHashSet(info));

        // it must remain only 1 FeatureEntity in database
        waitRequest(this.featureRepo, 1, 30000);
        // it mustn't be the created one of the fist feature creation
        assertNotEquals(featureInDatabase.getId(), this.featureRepo.findAll().get(0).getId());

    }

    @Test
    public void testDuplicatedFeatureCreationWithOverrideCaseNoFiles() throws InterruptedException {
        List<FeatureCreationRequestEvent> events = new ArrayList<>();

        // publish a feature in creation
        super.initFeatureCreationRequestEvent(events, 1);
        events.get(0).getFeature().setId("id");
        events.get(0).getFeature().setFiles(new ArrayList<>());
        publisher.publish(events);
        LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! {}", featureCreationRequestRepo.count());
        waitRequest(this.featureCreationRequestRepo, 1, 30000);

        this.featureCreationService.scheduleRequests();

        waitRequest(this.featureRepo, 1, 30000);

        // mock storage response to indicate creation succed
        FeatureEntity featureInDatabase = this.featureRepo.findAll().get(0);

        // publish a duplicated feature creation
        publisher.publish(events);
        waitRequest(this.featureCreationRequestRepo, 1, 30000);
        this.featureCreationService.scheduleRequests();
        waitRequest(this.featureRepo, 2, 30000);

        // that must publish a FeatureDeletionRequestEvent
        waitRequest(this.featureDeletionRepo, 1, 30000);
        this.featureDeletionService.scheduleRequests();

        waitRequest(this.featureRepo, 1, 30000);
        // it mustn't be the created one of the fist feature creation
        assertNotEquals(featureInDatabase.getId(), this.featureRepo.findAll().get(0).getId());

    }

    @Test
    public void testDuplicatedFeatureCreationWithoutOverride() throws InterruptedException {
        List<FeatureCreationRequestEvent> events = new ArrayList<>();

        // publish a feature in creation
        super.initFeatureCreationRequestEvent(events, 1);
        events.get(0).setOverridePrviousVersion(false);

        events.get(0).getFeature().setId("id");
        publisher.publish(events);
        waitRequest(this.featureCreationRequestRepo, 1, 30000);
        this.featureCreationService.scheduleRequests();
        waitRequest(this.featureRepo, 1, 30000);

        // mock storage response to indicate creation succed
        FeatureCreationRequest fcr = this.featureCreationRequestRepo.findAll().get(0);
        RequestInfo info = RequestInfo.build();
        fcr.setGroupId(info.getGroupId());
        fcr = this.featureCreationRequestRepo.save(fcr);
        this.listener.onStoreSuccess(Sets.newHashSet(info));

        // publish a duplicated feature creation
        publisher.publish(events);
        waitRequest(this.featureCreationRequestRepo, 1, 30000);
        this.featureCreationService.scheduleRequests();
        waitRequest(this.featureRepo, 2, 30000);

        // mock storage response to indicate creation succed
        fcr = this.featureCreationRequestRepo.findAll().get(0);
        fcr.setGroupId(info.getGroupId());
        fcr = this.featureCreationRequestRepo.save(fcr);
        this.listener.onStoreSuccess(Sets.newHashSet(info));

        // it must remain the 2 FeatureEntity in database
        waitRequest(this.featureRepo, 2, 30000);

    }

    @Test
    public void testDuplicatedFeatureCreationWithoutOverrideCaseNoFiles() throws InterruptedException {
        List<FeatureCreationRequestEvent> events = new ArrayList<>();

        // publish a feature in creation
        super.initFeatureCreationRequestEvent(events, 1);
        events.get(0).setOverridePrviousVersion(false);

        events.get(0).getFeature().setId("id");
        publisher.publish(events);
        waitRequest(this.featureCreationRequestRepo, 1, 30000);
        this.featureCreationService.scheduleRequests();
        waitRequest(this.featureRepo, 1, 30000);

        // mock storage response to indicate creation succed
        FeatureCreationRequest fcr = this.featureCreationRequestRepo.findAll().get(0);
        RequestInfo info = RequestInfo.build();
        fcr.setGroupId(info.getGroupId());
        fcr = this.featureCreationRequestRepo.save(fcr);
        this.listener.onStoreSuccess(Sets.newHashSet(info));

        // publish a duplicated feature creation
        publisher.publish(events);
        waitRequest(this.featureCreationRequestRepo, 1, 30000);
        this.featureCreationService.scheduleRequests();

        // it must remain the 2 FeatureEntity in database
        waitRequest(this.featureRepo, 2, 30000);

    }
}
