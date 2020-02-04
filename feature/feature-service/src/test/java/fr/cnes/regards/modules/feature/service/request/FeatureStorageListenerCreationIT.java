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
package fr.cnes.regards.modules.feature.service.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.MimeType;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dao.IFeatureCopyRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationMetadataEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureFile;
import fr.cnes.regards.modules.feature.dto.FeatureFileAttributes;
import fr.cnes.regards.modules.feature.dto.FeatureFileLocation;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.AbstractFeatureMultitenantServiceTest;
import fr.cnes.regards.modules.storage.client.RequestInfo;
import fr.cnes.regards.modules.storage.domain.dto.FileReferenceDTO;
import fr.cnes.regards.modules.storage.domain.dto.request.RequestResultInfoDTO;

/**
 * @author kevin
 *
 */
@TestPropertySource(
        properties = { "spring.jpa.properties.hibernate.default_schema=feature", "regards.amqp.enabled=true",
                "spring.task.scheduling.pool.size=2", "regards.feature.metrics.enabled=true" },
        locations = { "classpath:regards_perf.properties", "classpath:batch.properties",
                "classpath:metrics.properties" })
@ActiveProfiles({ "testAmqp" })
public class FeatureStorageListenerCreationIT extends AbstractFeatureMultitenantServiceTest {

    @Autowired
    private IFeatureCreationRequestRepository fcrRepo;

    @Autowired
    private IFeatureCopyRequestRepository featureCopyRepo;

    @Autowired
    private IFeatureEntityRepository featureRepo;

    @Autowired
    private FeatureStorageListener listener;

    @Override
    @Before
    public void before() throws InterruptedException {
        this.featureCopyRepo.deleteAll();
        super.before();
    }

    @Test
    public void testHandlerStorageOk() {

        RequestInfo info = RequestInfo.build();

        initData(info);

        this.listener.onStoreSuccess(Sets.newHashSet(info));

        // the FeatureCreationRequest must be deleted
        assertEquals(0, fcrRepo.count());
        // the FeatureEntity must remain
        assertEquals(1, featureRepo.count());

    }

    @Test
    public void testHandlerStorageError() {

        RequestInfo info = RequestInfo.build();

        initData(info);

        this.listener.onStoreError(Sets.newHashSet(info));

        // the FeatureCreationRequest must remain
        assertEquals(1, fcrRepo.count());
        // it state must be an error
        assertEquals(RequestState.ERROR, fcrRepo.findAll().get(0).getState());
        // the FeatureEntity must remain
        assertEquals(1, featureRepo.count());

    }

    @Test
    public void testHandlerReferenceOk() {

        RequestInfo info = RequestInfo.build();

        initData(info);

        this.listener.onReferenceSuccess(Sets.newHashSet(info));

        // the FeatureCreationRequest must be deleted
        assertEquals(0, fcrRepo.count());
        // the FeatureEntity must remain
        assertEquals(1, featureRepo.count());

    }

    @Test
    public void testHandlerReferenceError() {

        RequestInfo info = RequestInfo.build();

        initData(info);

        this.listener.onReferenceError(Sets.newHashSet(info));

        // the FeatureCreationRequest must remain
        assertEquals(1, fcrRepo.count());
        // it state must be an error
        assertEquals(RequestState.ERROR, fcrRepo.findAll().get(0).getState());
        // the FeatureEntity must remain
        assertEquals(1, featureRepo.count());

    }

    @Test
    public void testOnCopyOk() {

        RequestInfo info = RequestInfo.build();

        initData(info);
        RequestResultInfoDTO resultInfo = RequestResultInfoDTO
                .build(null, "checksum", null, "dtc",
                       Sets.newHashSet(featureRepo.findAll().get(0).getFeature().getUrn().toString()),
                       new FileReferenceDTO(), null);

        info.getSuccessRequests().add(resultInfo);
        this.listener.onCopySuccess(Sets.newHashSet(info));
        // we should have 1 copy request in database
        assertEquals(1, this.featureCopyRepo.count());
        // wait that this request is treated
        waitRequest(this.featureCopyRepo, 0, 60000);

        // we expect have dtc in FileLocation
        assertEquals(3, featureRepo.findAll().get(0).getFeature().getFiles().get(0).getLocations().size());
        assertTrue(featureRepo.findAll().get(0).getFeature().getFiles().get(0).getLocations().stream()
                .anyMatch(loc -> loc.getUrl().equals("dtc")));

    }

    @Test
    public void testOnCopyFailWithNonExistingChecksum() throws InterruptedException {

        RequestInfo info = RequestInfo.build();

        initData(info);
        RequestResultInfoDTO resultInfo = RequestResultInfoDTO
                .build(null, "fail", null, "dtc",
                       Sets.newHashSet(featureRepo.findAll().get(0).getFeature().getUrn().toString()),
                       new FileReferenceDTO(), null);

        info.getSuccessRequests().add(resultInfo);
        this.listener.onCopySuccess(Sets.newHashSet(info));
        this.waitForErrorState(featureCopyRepo);

        // we expect have always 2 FileLocation
        assertEquals(2, featureRepo.findAll().get(0).getFeature().getFiles().get(0).getLocations().size());

    }

    @Test
    public void testOnCopyFailWithNonExistingUrn() throws InterruptedException {

        RequestInfo info = RequestInfo.build();

        initData(info);
        RequestResultInfoDTO resultInfo = RequestResultInfoDTO
                .build(null, "checksum", null, "dtc", Sets.newHashSet(FeatureUniformResourceName
                        .build(FeatureIdentifier.FEATURE, EntityType.DATA, "fail", UUID.randomUUID(), 1).toString()),
                       new FileReferenceDTO(), null);

        info.getSuccessRequests().add(resultInfo);
        this.listener.onCopySuccess(Sets.newHashSet(info));
        this.waitForErrorState(featureCopyRepo);

        // we expect have always 2 FileLocation
        assertEquals(2, featureRepo.findAll().get(0).getFeature().getFiles().get(0).getLocations().size());

    }

    private void initData(RequestInfo info) {
        String model = "model";
        FeatureCreationRequest fcr = FeatureCreationRequest
                .build("id1", OffsetDateTime.now(), RequestState.GRANTED, new HashSet<String>(),
                       Feature.build("id1",
                                     FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, EntityType.DATA, "lol",
                                                                      UUID.randomUUID(), 1),
                                     IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA, model),
                       FeatureCreationMetadataEntity.build("owner", "session", Lists.emptyList(), true),
                       FeatureRequestStep.LOCAL_SCHEDULED, PriorityLevel.NORMAL);
        fcr.setGroupId(info.getGroupId());

        FeatureEntity feature = FeatureEntity
                .build("owner", "session",
                       Feature.build("id2",
                                     FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, EntityType.DATA,
                                                                      "peps", UUID.randomUUID(), 1),
                                     IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA, model),
                       null, model);
        List<FeatureFile> filles = new ArrayList<>();
        filles.add(FeatureFile.build(
                                     FeatureFileAttributes.build(DataType.DESCRIPTION, new MimeType("mime"), "toto",
                                                                 1024l, "MD5", "checksum"),
                                     FeatureFileLocation.build("www.google.com", "GPFS"),
                                     FeatureFileLocation.build("www.perdu.com", "GPFS")));
        feature.getFeature().setFiles(filles);
        this.featureRepo.save(feature);
        fcr.setFeatureEntity(feature);
        this.fcrRepo.save(fcr);
    }
}
