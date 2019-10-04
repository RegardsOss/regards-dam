package fr.cnes.regards.modules.feature.service;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.modules.feature.repository.FeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.repository.FeatureEntityRepository;

public abstract class AbstractFeatureMultitenantServiceTest extends AbstractMultitenantServiceTest {

    @Autowired
    private FeatureCreationRequestRepository featureCreationRequestRepo;

    @Autowired
    private FeatureEntityRepository featureRepo;

    @Before
    public void before() {
        this.featureCreationRequestRepo.deleteAll();
        this.featureRepo.deleteAll();
        simulateApplicationReadyEvent();
    }

}
