package fr.cnes.regards.modules.notifier.service;

import org.junit.After;
import org.junit.Before;
import org.springframework.amqp.AmqpIOException;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.amqp.configuration.AmqpConstants;
import fr.cnes.regards.framework.amqp.configuration.IAmqpAdmin;
import fr.cnes.regards.framework.amqp.configuration.IRabbitVirtualHostAdmin;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.notifier.dao.IRecipientRepository;
import fr.cnes.regards.modules.notifier.dao.IRuleRepository;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender10;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender2;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender3;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender4;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender5;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender6;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender7;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender8;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender9;
import fr.cnes.regards.modules.notifier.service.flow.FeatureEventHandler;

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

    @Autowired(required = false)
    private IAmqpAdmin amqpAdmin;

    @Autowired(required = false)
    private IRabbitVirtualHostAdmin vhostAdmin;

    @Before
    public void before() throws InterruptedException {
        this.notificationService.cleanTenantCache(runtimeTenantResolver.getTenant());
        this.recipientRepo.deleteAll();
        this.ruleRepo.deleteAll();
        this.pluginConfRepo.deleteAll();
        simulateApplicationReadyEvent();
    }

    /**
     * Internal method to clean AMQP queues, if actives
     */
    public void cleanAMQPQueues(Class<? extends IHandler<?>> handler, Target target) {
        if (vhostAdmin != null) {
            // Re-set tenant because above simulation clear it!

            // Purge event queue
            try {
                vhostAdmin.bind(AmqpConstants.AMQP_MULTITENANT_MANAGER);
                amqpAdmin.purgeQueue(amqpAdmin.getSubscriptionQueueName(handler, target), false);
            } catch (AmqpIOException e) {
                //todo
            } finally {
                vhostAdmin.unbind();
            }
        }
    }

    @After
    public void after() {
        cleanAMQPQueues(FeatureEventHandler.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender2.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender3.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender4.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender5.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender6.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender7.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender8.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender9.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender10.class, Target.ONE_PER_MICROSERVICE_TYPE);
    }
}
