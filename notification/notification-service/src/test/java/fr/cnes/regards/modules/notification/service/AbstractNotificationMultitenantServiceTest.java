package fr.cnes.regards.modules.notification.service;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.modules.notification.dao.IRecipientRepository;
import fr.cnes.regards.modules.notification.dao.IRuleRepository;

public abstract class AbstractNotificationMultitenantServiceTest extends AbstractMultitenantServiceTest {

    @Autowired
    protected IRuleRepository ruleRepo;

    @Autowired
    protected IRecipientRepository recipientRepo;

    @Autowired
    protected IPluginConfigurationRepository pluginConfRepo;

    @Before
    public void before() throws InterruptedException {
        this.ruleRepo.deleteAll();
        this.recipientRepo.deleteAll();
        this.pluginConfRepo.deleteAll();
        simulateApplicationReadyEvent();
    }

}
