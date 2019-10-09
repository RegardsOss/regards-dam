/**
 *
 */
package fr.cnes.regards.modules.feature.service.request;

import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureMetadataDto;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.repository.FeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.repository.FeatureEntityRepository;
import fr.cnes.regards.modules.feature.service.AbstractFeatureMultitenantServiceTest;
import fr.cnes.regards.modules.storagelight.client.RequestInfo;
import fr.cnes.regards.modules.storagelight.domain.dto.request.RequestResultInfoDTO;

/**
 * @author kevin
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature" })
public class FeatureStorageListenerIT extends AbstractFeatureMultitenantServiceTest {

	@Autowired
	private FeatureCreationRequestRepository fcrRepo;

	@Autowired
	private FeatureEntityRepository featureRepo;

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
		// and its status must be to success
		assertEquals(FeatureRequestStep.REMOTE_STORAGE_SUCCESS, featureRepo.findAll().get(0).getState());

	}

	@Test
	public void testHandlerStorageError() {

		RequestInfo info = RequestInfo.build();

		initData(info);

		this.listener.onStoreError(info, new ArrayList<RequestResultInfoDTO>(), new ArrayList<RequestResultInfoDTO>());

		// the FeatureCreationRequest must remain
		assertEquals(1, fcrRepo.count());
		// it's state must be on error
		assertEquals(RequestState.ERROR, fcrRepo.findAll().get(0).getState());
		// the FeatureEntity must remain
		assertEquals(1, featureRepo.count());
		// and its status must be to success
		assertEquals(FeatureRequestStep.REMOTE_STORAGE_ERROR, featureRepo.findAll().get(0).getState());

	}

	private void initData(RequestInfo info) {
		FeatureCreationRequest fcr = FeatureCreationRequest.build("id1", OffsetDateTime.now(), RequestState.GRANTED,
				new HashSet<String>(),
				Feature.builder(
						new FeatureUniformResourceName(FeatureIdentifier.FEATURE, EntityType.DATA, "peps",
								UUID.randomUUID(), 1),
						IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA, "model"),
				new ArrayList<FeatureMetadataDto>());
		fcr.setGroupId(info.getGroupId());
		FeatureEntity feature = FeatureEntity
				.build(Feature.builder(
						new FeatureUniformResourceName(FeatureIdentifier.FEATURE, EntityType.DATA, "peps",
								UUID.randomUUID(), 1),
						IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA, "model"),
						OffsetDateTime.now(), FeatureRequestStep.REMOTE_STORAGE_REQUESTED);
		this.featureRepo.save(feature);
		fcr.setFeatureEntity(feature);
		this.fcrRepo.save(fcr);
	}
}
