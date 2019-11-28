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

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dao.IFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureMetadataEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.AbstractFeatureMultitenantServiceTest;
import fr.cnes.regards.modules.storage.client.RequestInfo;

/**
 * @author kevin
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature" })
@ActiveProfiles({ "noscheduler", "nohandler" })
public class FeatureStorageListenerCreationIT extends AbstractFeatureMultitenantServiceTest {

    @Autowired
    private IFeatureCreationRequestRepository fcrRepo;

    @Autowired
    private IFeatureEntityRepository featureRepo;

    @Autowired
    private FeatureStorageListener listener;

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

    private void initData(RequestInfo info) {
        FeatureCreationRequest fcr = FeatureCreationRequest
                .build("id1", OffsetDateTime.now(), RequestState.GRANTED, new HashSet<String>(),
                       Feature.build("id1",
                                     FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, EntityType.DATA,
                                                                      "peps", UUID.randomUUID(), 1),
                                     IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA, "model"),
                       FeatureMetadataEntity.build("owner", "session", Lists.emptyList()),
                       FeatureRequestStep.LOCAL_SCHEDULED, PriorityLevel.NORMAL);
        fcr.setGroupId(info.getGroupId());

        FeatureEntity feature = FeatureEntity
                .build("owner", "session",
                       Feature.build("id2",
                                     FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, EntityType.DATA,
                                                                      "peps", UUID.randomUUID(), 1),
                                     IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA, "model"));
        this.featureRepo.save(feature);
        fcr.setFeatureEntity(feature);
        this.fcrRepo.save(fcr);
    }
}
