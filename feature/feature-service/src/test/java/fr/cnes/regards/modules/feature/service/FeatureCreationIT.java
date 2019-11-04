/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dao.ILightFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.domain.request.LightFeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureCreationCollection;
import fr.cnes.regards.modules.feature.dto.FeatureSessionMetadata;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.RequestInfo;
import fr.cnes.regards.modules.feature.dto.StorageMetadata;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;

@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp", "noscheduler", "nohandler" })
public class FeatureCreationIT extends AbstractFeatureMultitenantServiceTest {

    @Autowired
    protected ILightFeatureCreationRequestRepository featureCreationRequestLightRepo;

    @Autowired
    private IFeatureCreationService featureService;

    /**
     * Test creation of properties.getMaxBulkSize() features Check if
     * {@link FeatureCreationRequest} and {@link FeatureEntity}are stored in
     * database then at the end of the job test if all
     * {@link FeatureCreationRequest} are deleted
     *
     * @throws InterruptedException
     */
    @Test
    public void testFeatureCreation() throws InterruptedException {

        List<FeatureCreationRequestEvent> events = new ArrayList<>();

        super.initFeatureCreationRequestEvent(events, properties.getMaxBulkSize());

        this.featureService.registerRequests(events);

        assertEquals(properties.getMaxBulkSize().intValue(), this.featureCreationRequestRepo.count());

        featureService.scheduleRequests();

        int cpt = 0;
        long featureNumberInDatabase;
        do {
            featureNumberInDatabase = this.featureRepo.count();
            Thread.sleep(1000);
            cpt++;
        } while (cpt < 100 && featureNumberInDatabase != properties.getMaxBulkSize());

        assertEquals(properties.getMaxBulkSize().intValue(), this.featureRepo.count());

        // in that case all features hasn't been saved
        if (cpt == 100) {
            fail("Doesn't have all features at the end of time");
        }
    }

    /**
     * Test creation of properties.getMaxBulkSize() features one will be invalid test that this
     * one will not be sored in databse
     *
     * @throws InterruptedException
     */
    @Test
    public void testFeatureCreationWithInvalidFeature() throws InterruptedException {

        List<FeatureCreationRequestEvent> events = new ArrayList<>();

        super.initFeatureCreationRequestEvent(events, properties.getMaxBulkSize());

        Feature f = events.get(0).getFeature();
        f.setEntityType(null);

        this.featureService.registerRequests(events);

        assertEquals(properties.getMaxBulkSize() - 1, this.featureCreationRequestRepo.count());

        featureService.scheduleRequests();

        int cpt = 0;
        long featureNumberInDatabase;
        do {
            featureNumberInDatabase = this.featureRepo.count();
            Thread.sleep(1000);
            cpt++;
        } while (cpt < 100 && featureNumberInDatabase != properties.getMaxBulkSize() - 1);

        assertEquals(properties.getMaxBulkSize() - 1, this.featureRepo.count());

        // in that case all features hasn't been saved
        if (cpt == 100) {
            fail("Doesn't have all features at the end of time");
        }
    }

    @Test
    public void testRegisterScheduleProcess() {
        List<Feature> features = new ArrayList<>();
        for (int i = 0; i < properties.getMaxBulkSize(); i++) {
            features.add(Feature.build("id" + i, null, IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA,
                                       "model"));
        }

        StorageMetadata.build("id ");
        FeatureCreationCollection collection = FeatureCreationCollection.build(FeatureSessionMetadata
                .build("owner", "session", PriorityLevel.AVERAGE, StorageMetadata.build("id ")), features);
        RequestInfo<String> infos = this.featureService.registerRequests(collection);

        assertEquals(properties.getMaxBulkSize().intValue(), this.featureCreationRequestRepo.count());
        assertEquals(properties.getMaxBulkSize().intValue(), infos.getGranted().size());
        assertEquals(0, infos.getDenied().size());
    }

    @Test
    public void testRegisterScheduleProcessWithErros() {
        List<Feature> features = new ArrayList<>();
        for (int i = 0; i < properties.getMaxBulkSize(); i++) {
            features.add(Feature.build("id" + i, null, IGeometry.point(IGeometry.position(10.0, 20.0)), null, "model"));
        }

        StorageMetadata.build("id ");
        FeatureCreationCollection collection = FeatureCreationCollection.build(FeatureSessionMetadata
                .build("owner", "session", PriorityLevel.AVERAGE, StorageMetadata.build("id ")), features);
        RequestInfo<String> infos = this.featureService.registerRequests(collection);

        assertEquals(0, infos.getGranted().size());
        assertEquals(properties.getMaxBulkSize().intValue(), infos.getDenied().size());
    }

    /**
     * Test priority level for feature creation we will schedule properties.getMaxBulkSize() {@link FeatureCreationRequestEvent}
     * with priority set to average plus properties.getMaxBulkSize() /2 {@link FeatureCreationRequestEvent} with {@link PriorityLevel}
     * to average
     * @throws InterruptedException
     */
    @Test
    public void testFeaturePriority() throws InterruptedException {

        List<FeatureCreationRequestEvent> events = new ArrayList<>();

        super.initFeatureCreationRequestEvent(events, properties.getMaxBulkSize() + properties.getMaxBulkSize() / 2);

        // we will set all priority to low for the (properties.getMaxBulkSize() / 2) last event
        for (int i = properties.getMaxBulkSize(); i < properties.getMaxBulkSize()
                + properties.getMaxBulkSize() / 2; i++) {
            events.get(i).getMetadata().setPriority(PriorityLevel.HIGH);
        }

        this.featureService.registerRequests(events);

        assertEquals(properties.getMaxBulkSize() + properties.getMaxBulkSize() / 2,
                     this.featureCreationRequestRepo.count());

        featureService.scheduleRequests();

        int cpt = 0;
        long featureNumberInDatabase;
        do {
            featureNumberInDatabase = this.featureRepo.count();
            Thread.sleep(1000);
            cpt++;
        } while (cpt < 100 && featureNumberInDatabase != properties.getMaxBulkSize());

        assertEquals(properties.getMaxBulkSize().intValue(), this.featureRepo.count());

        // in that case all features hasn't been saved
        if (cpt == 100) {
            fail("Doesn't have all features at the end of time");
        }

        // check that half of the FeatureCreationRequest with step to LOCAL_SCHEDULED
        // have their priority to HIGH and half to AVERAGE
        Page<LightFeatureCreationRequest> scheduled = this.featureCreationRequestLightRepo
                .findByStep(FeatureRequestStep.LOCAL_SCHEDULED, PageRequest.of(0, properties.getMaxBulkSize()));
        int highPriorityNumber = 0;
        int otherPriorityNumber = 0;
        for (LightFeatureCreationRequest request : scheduled) {
            if (request.getPriority().equals(PriorityLevel.HIGH)) {
                highPriorityNumber++;
            } else {
                otherPriorityNumber++;
            }
        }
        // half of scheduled should be with priority HIGH
        assertEquals(properties.getMaxBulkSize().intValue(), highPriorityNumber + otherPriorityNumber);
        assertTrue(highPriorityNumber == otherPriorityNumber);

    }
}
