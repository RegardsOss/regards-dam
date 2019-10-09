package fr.cnes.regards.modules.feature.service;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.modules.feature.dao.IFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;

public abstract class AbstractFeatureMultitenantServiceTest extends AbstractMultitenantServiceTest {

    @Autowired
    private IFeatureCreationRequestRepository featureCreationRequestRepo;

    @Autowired
    private IFeatureEntityRepository featureRepo;

    @Before
    public void before() {
        this.featureCreationRequestRepo.deleteAll();
        this.featureRepo.deleteAll();
        simulateApplicationReadyEvent();
    }

}
