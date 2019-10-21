package fr.cnes.regards.modules.feature.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureCollection;
import fr.cnes.regards.modules.feature.dto.FeatureMetadataDto;
import fr.cnes.regards.modules.feature.dto.FeatureSessionDto;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;

@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp", "noscheduler" })
public class FeatureCreationIT extends AbstractFeatureMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureCreationIT.class);

    private final int EVENTS_NUMBER = 1000;

    @Autowired
    private FeatureService featureService;

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

        super.initFeatureCreationRequestEvent(events, EVENTS_NUMBER);

        this.featureService.handleFeatureCreationRequestEvents(events);

        assertEquals(EVENTS_NUMBER, this.featureCreationRequestRepo.count());

        int cpt = 0;
        long featureNumberInDatabase;
        do {
            featureNumberInDatabase = this.featureRepo.count();
            Thread.sleep(1000);
            cpt++;
        } while (cpt < 100 && featureNumberInDatabase != EVENTS_NUMBER);

        assertEquals(EVENTS_NUMBER, this.featureRepo.count());

        // in that case all features hasn't been saved
        if (cpt == 100) {
            fail("Doesn't have all features at the end of time");
        }
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

        super.initFeatureCreationRequestEvent(events, EVENTS_NUMBER);

        Feature f = events.get(0).getFeature();
        f.setEntityType(null);
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>> Entity type set to null for feature {}", f.getId());

        this.featureService.handleFeatureCreationRequestEvents(events);

        assertEquals(EVENTS_NUMBER - 1, this.featureCreationRequestRepo.count());

        int cpt = 0;
        long featureNumberInDatabase;
        do {
            featureNumberInDatabase = this.featureRepo.count();
            Thread.sleep(1000);
            cpt++;
        } while (cpt < 100 && featureNumberInDatabase != EVENTS_NUMBER - 1);

        assertEquals(EVENTS_NUMBER - 1, this.featureRepo.count());

        // in that case all features hasn't been saved
        if (cpt == 100) {
            fail("Doesn't have all features at the end of time");
        }
    }

    @Test
    public void testCreateFeatureRequestEvent() {
        List<Feature> features = new ArrayList<Feature>();
        for (int i = 0; i < EVENTS_NUMBER; i++) {
            features.add(Feature.build("id" + i, null, IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA,
                                       "model"));
        }

        FeatureCollection collection = FeatureCollection.build(new ArrayList<FeatureMetadataDto>(),
                                                               FeatureSessionDto.builder("owner", "session"));
        collection.addAll(features);
        collection.getMetadata().add(FeatureMetadataDto.builder("id ", null, null));
        this.featureService.createFeatureRequestEvent(collection);

        assertEquals(EVENTS_NUMBER, this.featureCreationRequestRepo.count());
    }
}
