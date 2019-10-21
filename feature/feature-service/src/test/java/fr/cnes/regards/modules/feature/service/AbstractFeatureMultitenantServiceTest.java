package fr.cnes.regards.modules.feature.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeType;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dao.IFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureUpdateRequestRepository;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureFile;
import fr.cnes.regards.modules.feature.dto.FeatureFileAttributes;
import fr.cnes.regards.modules.feature.dto.FeatureFileLocation;
import fr.cnes.regards.modules.feature.dto.FeatureMetadataDto;
import fr.cnes.regards.modules.feature.dto.FeatureSessionDto;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;

public abstract class AbstractFeatureMultitenantServiceTest extends AbstractMultitenantServiceTest {

    @Autowired
    protected IFeatureCreationRequestRepository featureCreationRequestRepo;

    @Autowired
    protected IFeatureEntityRepository featureRepo;

    @Autowired
    protected IFeatureUpdateRequestRepository featureUpdateRequestRepo;

    @Before
    public void before() throws InterruptedException {
        this.featureCreationRequestRepo.deleteAll();
        this.featureUpdateRequestRepo.deleteAll();
        this.featureRepo.deleteAll();
        simulateApplicationReadyEvent();
    }

    protected void initFeatureCreationRequestEvent(List<FeatureCreationRequestEvent> events,
            int featureNumberToCreate) {
        FeatureCreationRequestEvent toAdd;
        Feature featureToAdd;
        FeatureFile file;
        // create events to publish
        for (int i = 0; i < featureNumberToCreate; i++) {
            file = FeatureFile.build(
                                     FeatureFileAttributes.build(DataType.DESCRIPTION, new MimeType("mime"), "toto",
                                                                 1024l, "MD5", "checksum"),
                                     FeatureFileLocation.build("www.google.com", "GPFS"));
            featureToAdd = Feature
                    .build("id" + i, null, IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA, "model")
                    .withFiles(file);
            toAdd = FeatureCreationRequestEvent.build(featureToAdd, new ArrayList<FeatureMetadataDto>(),
                                                      OffsetDateTime.now(),
                                                      FeatureSessionDto.builder("owner", "session"));
            toAdd.setRequestId(String.valueOf(i));
            toAdd.setFeature(featureToAdd);
            toAdd.setRequestDate(OffsetDateTime.now());
            events.add(toAdd);
        }
    }

}
