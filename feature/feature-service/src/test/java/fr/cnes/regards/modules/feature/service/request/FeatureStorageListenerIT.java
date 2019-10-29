/**
 *
 */
package fr.cnes.regards.modules.feature.service.request;

import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

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
import fr.cnes.regards.modules.storagelight.client.RequestInfo;
import fr.cnes.regards.modules.storagelight.domain.dto.request.RequestResultInfoDTO;

/**
 * @author kevin
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature" })
@ActiveProfiles(value = { "noscheduler" })
public class FeatureStorageListenerIT extends AbstractFeatureMultitenantServiceTest {

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

        this.listener.onStoreSuccess(info, new ArrayList<RequestResultInfoDTO>());

        // the FeatureCreationRequest must be deleted
        assertEquals(0, fcrRepo.count());
        // the FeatureEntity must remain
        assertEquals(1, featureRepo.count());

    }

    @Test
    public void testHandlerStorageError() {

        RequestInfo info = RequestInfo.build();

        initData(info);

        this.listener.onStoreError(info, new ArrayList<RequestResultInfoDTO>(), new ArrayList<RequestResultInfoDTO>());

        // the FeatureCreationRequest must remain
        assertEquals(1, fcrRepo.count());
        // it
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
                       FeatureRequestStep.LOCAL_SCHEDULED, PriorityLevel.AVERAGE);
        fcr.setGroupId(info.getGroupId());

        FeatureEntity feature = FeatureEntity
                .build("owner", "session",
                       Feature.build("id2",
                                     FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, EntityType.DATA,
                                                                      "peps", UUID.randomUUID(), 1),
                                     IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA, "model"),
                       OffsetDateTime.now());
        this.featureRepo.save(feature);
        fcr.setFeatureEntity(feature);
        this.fcrRepo.save(fcr);
    }
}
