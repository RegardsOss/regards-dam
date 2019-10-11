/**
 *
 */
package fr.cnes.regards.modules.feature.service;

import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.domain.request.FeatureUpdateRequest;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

/**
 * @author kevin
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp" })
public class FeatureUpdateIT extends AbstractFeatureMultitenantServiceTest {

    @Autowired
    private IFeatureUpdateService featureUpdateService;

    @Autowired
    private FeatureService featureService;

    @Test
    public void testSchedulerSteps() throws InterruptedException {

        List<FeatureCreationRequestEvent> events = new ArrayList<>();

        super.initFeatureCreationRequestEvent(events, 2);
        this.featureService.handleFeatureCreationRequestEvents(events);

        int cpt = 0;
        long featureNumberInDatabase;
        do {
            featureNumberInDatabase = this.featureRepo.count();
            Thread.sleep(1000);
            cpt++;
        } while ((cpt < 10) && (featureNumberInDatabase != 2));

        FeatureEntity toUpdate = super.featureRepo.findAll().get(0);
        FeatureEntity updatingByScheduler = super.featureRepo.findAll().get(1);

        FeatureUpdateRequest fur1 = new FeatureUpdateRequest();
        fur1.setFeatureEntity(toUpdate);
        fur1.setFeature(toUpdate.getFeature());
        fur1.setStep(FeatureRequestStep.LOCAL_DELAYED);
        fur1.setRegistrationDate(OffsetDateTime.now());
        fur1.setState(RequestState.GRANTED);
        fur1.setRequestDate(OffsetDateTime.now());
        fur1.setRequestId("1");
        fur1.setUrn(toUpdate.getFeature().getUrn());

        FeatureUpdateRequest fur2 = new FeatureUpdateRequest();
        fur2.setFeatureEntity(updatingByScheduler);
        fur2.setFeature(updatingByScheduler.getFeature());
        fur2.setStep(FeatureRequestStep.LOCAL_SCHEDULED);
        fur2.setRegistrationDate(OffsetDateTime.now());
        fur2.setRequestDate(OffsetDateTime.now());
        fur2.setState(RequestState.GRANTED);
        fur2.setRequestId("2");
        fur2.setUrn(updatingByScheduler.getFeature().getUrn());

        FeatureUpdateRequest fur3 = new FeatureUpdateRequest();
        fur3.setFeature(updatingByScheduler.getFeature());
        fur3.setStep(FeatureRequestStep.LOCAL_DELAYED);
        fur3.setRegistrationDate(OffsetDateTime.now());
        fur3.setState(RequestState.GRANTED);
        fur3.setRequestDate(OffsetDateTime.now());
        fur3.setRequestId("3");
        fur3.setUrn(updatingByScheduler.getFeature().getUrn());

        super.featureUpdateRequestRepo.save(fur1);
        super.featureUpdateRequestRepo.save(fur2);
        super.featureUpdateRequestRepo.save(fur3);

        // wait 5 second to delay
        Thread.sleep(5000);

        featureUpdateService.scheduleUpdateRequestProcessing();

        List<FeatureUpdateRequest> updateRequests = this.featureUpdateRequestRepo.findAll();

        assertEquals(2, updateRequests.stream()
                .filter(request -> request.getStep().equals(FeatureRequestStep.LOCAL_SCHEDULED)).count());
        assertEquals(1, updateRequests.stream()
                .filter(request -> request.getStep().equals(FeatureRequestStep.LOCAL_DELAYED)).count());
    }
}
