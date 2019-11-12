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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dao.ILightFeatureUpdateRequestRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.domain.request.FeatureUpdateRequest;
import fr.cnes.regards.modules.feature.domain.request.LightFeatureUpdateRequest;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureUpdateRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.model.dto.properties.IProperty;

/**
 * @author kevin
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp", "noscheduler", "nohandler" })
public class FeatureUpdateIT extends AbstractFeatureMultitenantServiceTest {

    @Autowired
    private IFeatureUpdateService featureUpdateService;

    @Autowired
    private FeatureCreationService featureService;

    @Autowired
    private ILightFeatureUpdateRequestRepository lightFeatureUpdateRequestRepository;

    @Test
    public void testSchedulerSteps() throws InterruptedException {

        List<FeatureCreationRequestEvent> events = new ArrayList<>();

        super.initFeatureCreationRequestEvent(events, 2);
        this.featureService.registerRequests(events);

        this.featureService.scheduleRequests();
        int cpt = 0;
        long featureNumberInDatabase;
        do {
            featureNumberInDatabase = this.featureRepo.count();
            Thread.sleep(1000);
            cpt++;
        } while (cpt < 100 && featureNumberInDatabase != 2);

        FeatureEntity toUpdate = super.featureRepo.findAll().get(0);
        FeatureEntity updatingByScheduler = super.featureRepo.findAll().get(1);

        FeatureUpdateRequest fur1 = FeatureUpdateRequest.build("1", OffsetDateTime.now(), RequestState.GRANTED, null,
                                                               toUpdate.getFeature(), PriorityLevel.AVERAGE);
        fur1.setFeatureEntity(toUpdate);
        fur1.setFeature(toUpdate.getFeature());
        fur1.setStep(FeatureRequestStep.LOCAL_DELAYED);

        FeatureUpdateRequest fur2 = FeatureUpdateRequest.build("2", OffsetDateTime.now(), RequestState.GRANTED, null,
                                                               updatingByScheduler.getFeature(), PriorityLevel.AVERAGE);
        fur2.setFeatureEntity(updatingByScheduler);
        fur2.setStep(FeatureRequestStep.LOCAL_SCHEDULED);
        fur2.setPriority(PriorityLevel.AVERAGE);

        FeatureUpdateRequest fur3 = FeatureUpdateRequest.build("3", OffsetDateTime.now(), RequestState.GRANTED, null,
                                                               updatingByScheduler.getFeature(), PriorityLevel.AVERAGE);
        fur3.setFeature(updatingByScheduler.getFeature());
        fur3.setStep(FeatureRequestStep.LOCAL_DELAYED);
        fur3.setPriority(PriorityLevel.AVERAGE);

        super.featureUpdateRequestRepo.save(fur1);
        super.featureUpdateRequestRepo.save(fur2);
        super.featureUpdateRequestRepo.save(fur3);

        // wait 5 second to delay
        Thread.sleep(properties.getDelayBeforeProcessing() * 1000);

        this.featureUpdateService.scheduleRequests();

        List<FeatureUpdateRequest> updateRequests = this.featureUpdateRequestRepo.findAll();

        assertEquals(2, updateRequests.stream()
                .filter(request -> request.getStep().equals(FeatureRequestStep.LOCAL_SCHEDULED)).count());
        assertEquals(1, updateRequests.stream()
                .filter(request -> request.getStep().equals(FeatureRequestStep.LOCAL_DELAYED)).count());
    }

    /**
     * Test priority level for feature update we will schedule properties.getMaxBulkSize() {@link FeatureUpdateRequestEvent}
     * with priority set to average plus properties.getMaxBulkSize() /2 {@link FeatureUpdateRequestEvent} with {@link PriorityLevel}
     * to average
     * @throws InterruptedException
     */
    @Test
    public void testFeaturePriority() throws InterruptedException {

        List<FeatureCreationRequestEvent> events = new ArrayList<>();

        super.initFeatureCreationRequestEvent(events, properties.getMaxBulkSize() + properties.getMaxBulkSize() / 2);

        this.featureService.registerRequests(events);

        assertEquals(properties.getMaxBulkSize() + properties.getMaxBulkSize() / 2,
                     this.featureCreationRequestRepo.count());

        featureService.scheduleRequests();
        featureService.scheduleRequests();

        int cpt = 0;
        long featureNumberInDatabase;
        do {
            featureNumberInDatabase = this.featureRepo.count();
            Thread.sleep(1000);
            cpt++;
        } while (cpt < 100 && featureNumberInDatabase != properties.getMaxBulkSize() + properties.getMaxBulkSize() / 2);

        assertEquals(properties.getMaxBulkSize().intValue() + properties.getMaxBulkSize().intValue() / 2,
                     this.featureRepo.count());

        // in that case all features hasn't been saved
        if (cpt == 100) {
            fail("Doesn't have all features at the end of time");
        }
        List<FeatureUpdateRequestEvent> updateEvents = new ArrayList<>();
        updateEvents = events.stream()
                .map(event -> FeatureUpdateRequestEvent.build(event.getMetadata(), event.getFeature()))
                .collect(Collectors.toList());

        // we will set all priority to low for the (properties.getMaxBulkSize() / 2) last event
        for (int i = properties.getMaxBulkSize(); i < properties.getMaxBulkSize()
                + properties.getMaxBulkSize() / 2; i++) {
            updateEvents.get(i).getMetadata().setPriority(PriorityLevel.HIGH);
        }

        updateEvents.stream().forEach(event -> {
            event.getFeature().getProperties().clear();
            event.getFeature()
                    .setUrn(FeatureUniformResourceName
                            .build(FeatureIdentifier.FEATURE, EntityType.DATA, getDefaultTenant(),
                                   UUID.nameUUIDFromBytes(event.getFeature().getId().getBytes()), 1));
            event.getFeature()
                    .addProperty(IProperty.buildObject("file_characterization",
                                                       IProperty.buildBoolean("valid", Boolean.FALSE),
                                                       IProperty.buildDate("invalidation_date", OffsetDateTime.now())));
        });
        this.featureUpdateService.registerRequests(updateEvents);

        // we wait for delay before schedule
        Thread.sleep(this.properties.getDelayBeforeProcessing() * 1000 + 1000);

        this.featureUpdateService.scheduleRequests();
        this.waitUpdateRequestDeletion(properties.getMaxBulkSize() / 2, 10000);

        List<LightFeatureUpdateRequest> scheduled = this.lightFeatureUpdateRequestRepository
                .findRequestToSchedule(PageRequest.of(0, properties.getMaxBulkSize()), OffsetDateTime.now());
        // half of scheduled should be with priority HIGH
        assertEquals(properties.getMaxBulkSize().intValue() / 2, scheduled.size());
        // check that remaining FeatureUpdateRequest all their their priority not to high
        assertFalse(scheduled.stream().anyMatch(request -> PriorityLevel.HIGH.equals(request.getPriority())));

    }
}
