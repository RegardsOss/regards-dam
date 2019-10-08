package fr.cnes.regards.modules.feature.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.repository.FeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.repository.FeatureEntityRepository;

@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature",
		"regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
		"spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp" })
public class FeatureCreationIT extends AbstractFeatureMultitenantServiceTest {

	private final int EVENTS_NUMBER = 1000;

	@Autowired
	private FeatureService featureService;

	@Autowired
	private FeatureEntityRepository featureRepo;

	@Autowired
	private FeatureCreationRequestRepository featureCreationRequestRepo;

	/**
	 * Test creation of EVENTS_NUMBER features Check if
	 * {@link FeatureCreationRequest} and {@link FeatureEntity}are stored in
	 * database then at the end of the job test if all
	 * {@link FeatureCreationRequest} are deleted
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testFeatureCreation() throws InterruptedException {

		List<FeatureCreationRequestEvent> events = new ArrayList<>();

		FeatureCreationRequestEvent toAdd;
		Feature featureToAdd;

		// create events to publish
		for (int i = 0; i < EVENTS_NUMBER; i++) {
			featureToAdd = new Feature();
			featureToAdd.setEntityType(EntityType.DATA);
			featureToAdd.setModel("model");
			featureToAdd.setGeometry(IGeometry.point(IGeometry.position(10.0, 20.0)));
			featureToAdd
					.setUrn(new UniformResourceName(OAISIdentifier.SIP, EntityType.DATA, "peps", UUID.randomUUID(), 1));
			toAdd = new FeatureCreationRequestEvent();
			toAdd.setRequestId(String.valueOf(i));
			toAdd.setFeature(featureToAdd);
			toAdd.setRequestTime(OffsetDateTime.now());
			events.add(toAdd);
		}

		featureService.handleFeatureCreationRequestEvents(events);

		assertEquals(EVENTS_NUMBER, this.featureCreationRequestRepo.count());

		int cpt = 0;
		long featureNumberInDatabase;
		do {
			featureNumberInDatabase = this.featureRepo.count();
			Thread.sleep(1000);
			cpt++;
		} while ((cpt < 100) && (featureNumberInDatabase != EVENTS_NUMBER));

		// in that case all features hasn't been saved
		if (cpt == 100) {
			fail("Doesn't have all features at the end of time");
		}

		assertEquals(0, this.featureCreationRequestRepo.count());

	}

	/**
	 * Test creation of EVENTS_NUMBER features one will be invalid test that this
	 * one will not be sored in databse
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testFeatureCreationWithInvalidFeature() throws InterruptedException {

		List<FeatureCreationRequestEvent> events = new ArrayList<>();

		FeatureCreationRequestEvent toAdd;
		Feature featureToAdd;

		// create events to publish
		for (int i = 0; i < EVENTS_NUMBER; i++) {
			featureToAdd = new Feature();
			featureToAdd.setEntityType(EntityType.DATA);
			featureToAdd.setModel("model");
			featureToAdd.setGeometry(IGeometry.point(IGeometry.position(10.0, 20.0)));
			featureToAdd
					.setUrn(new UniformResourceName(OAISIdentifier.SIP, EntityType.DATA, "peps", UUID.randomUUID(), 1));
			toAdd = new FeatureCreationRequestEvent();
			toAdd.setRequestId(String.valueOf(i));
			toAdd.setFeature(featureToAdd);
			events.add(toAdd);
		}

		events.get(0).getFeature().setUrn(null);

		featureService.handleFeatureCreationRequestEvents(events);

		assertEquals(EVENTS_NUMBER - 1, this.featureCreationRequestRepo.count());

		int cpt = 0;
		long featureNumberInDatabase;
		do {
			featureNumberInDatabase = this.featureRepo.count();
			Thread.sleep(1000);
			cpt++;
		} while ((cpt < 100) && (featureNumberInDatabase != (EVENTS_NUMBER - 1)));

		// in that case all features hasn't been saved
		if (cpt == 100) {
			fail("Doesn't have all features at the end of time");
		}
	}
}
