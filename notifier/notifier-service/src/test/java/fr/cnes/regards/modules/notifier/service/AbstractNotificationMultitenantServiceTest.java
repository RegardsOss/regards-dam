package fr.cnes.regards.modules.notifier.service;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.notifier.dao.IRecipientRepository;
import fr.cnes.regards.modules.notifier.dao.IRuleRepository;

public abstract class AbstractNotificationMultitenantServiceTest extends AbstractMultitenantServiceTest {

    @Autowired
    protected IRuleRepository ruleRepo;

    @Autowired
    protected IRecipientRepository recipientRepo;

    @Autowired
    protected IPluginConfigurationRepository pluginConfRepo;

    @Autowired
    protected NotificationRuleService notificationService;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Before
    public void before() throws InterruptedException {
        this.notificationService.cleanTenantCache(runtimeTenantResolver.getTenant());
        this.recipientRepo.deleteAll();
        this.ruleRepo.deleteAll();
        this.pluginConfRepo.deleteAll();
        simulateApplicationReadyEvent();
    }

}
