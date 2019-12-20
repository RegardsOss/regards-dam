/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.notifier.service;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpIOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.configuration.AmqpConstants;
import fr.cnes.regards.framework.amqp.configuration.IAmqpAdmin;
import fr.cnes.regards.framework.amqp.configuration.IRabbitVirtualHostAdmin;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.modules.jobs.dao.IJobInfoRepository;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.IPluginParam;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.StringPluginParam;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.model.client.IModelAttrAssocClient;
import fr.cnes.regards.modules.model.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.model.service.xml.IComputationPluginService;
import fr.cnes.regards.modules.notifier.dao.INotificationActionRepository;
import fr.cnes.regards.modules.notifier.dao.IRecipientErrorRepository;
import fr.cnes.regards.modules.notifier.dao.IRecipientRepository;
import fr.cnes.regards.modules.notifier.dao.IRuleRepository;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender10;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender2;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender3;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender4;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender5;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender6;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender7;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender8;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender9;
import fr.cnes.regards.modules.notifier.service.conf.NotificationConfigurationProperties;
import fr.cnes.regards.modules.notifier.service.flow.NotificationActionEventHandler;
import fr.cnes.reguards.modules.notifier.dto.in.NotificationActionEvent;

public abstract class AbstractNotificationMultitenantServiceTest extends AbstractMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNotificationMultitenantServiceTest.class);

    protected static final int RECIPIENTS_PER_RULE = 10;

    protected static final int EVENT_TO_RECEIVE = 1_000;

    protected static final int EVENT_BULK = 1_000;

    // used to param if the test Recipient will fail
    public static boolean RECIPIENT_FAIL = true;

    @Autowired
    protected IRuleRepository ruleRepo;

    @Autowired
    protected IRecipientRepository recipientRepo;

    @Autowired
    protected IPluginConfigurationRepository pluginConfRepo;

    @Autowired
    protected INotificationActionRepository notificationRepo;

    @Autowired
    protected IRecipientErrorRepository recipientErrorRepo;

    @Autowired
    protected IJobInfoRepository jobInforepo;

    @Autowired
    protected NotificationRuleService notificationService;

    @Autowired
    protected IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired(required = false)
    private IAmqpAdmin amqpAdmin;

    @Autowired(required = false)
    private IRabbitVirtualHostAdmin vhostAdmin;

    @Autowired
    protected MultitenantFlattenedAttributeAdapterFactory factory;

    // Mock for test purpose
    @Autowired
    protected IComputationPluginService cps;

    @Autowired
    protected IModelAttrAssocClient modelAttrAssocClientMock;

    @Autowired
    protected NotificationConfigurationProperties configuration;

    @Autowired
    protected IPublisher publisher;

    @Autowired
    private Gson gson;

    @Autowired
    private ISubscriber subscriber;

    @Before
    public void before() throws InterruptedException {
        RECIPIENT_FAIL = true;
        this.notificationService.cleanTenantCache(runtimeTenantResolver.getTenant());
        this.recipientErrorRepo.deleteAll();
        this.recipientRepo.deleteAll();
        this.ruleRepo.deleteAll();
        this.pluginConfRepo.deleteAll();
        this.notificationRepo.deleteAll();
        this.jobInforepo.deleteAll();
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

    /**
     * Wait data creation on a {@link JpaRepository}
     * @param repo {@link JpaRepository} where we wait data
     * @param expectedNumber number of data waited
     * @param timeout to throw exception
     * @throws InterruptedException
     */
    public void waitDatabaseCreation(JpaRepository<?, ?> repo, int expectedNumber, int timeout)
            throws InterruptedException {
        long notificationActionNumber = 0;
        int cpt = 0;
        do {
            notificationActionNumber = repo.count();
            cpt++;
            Thread.sleep(1000);
        } while ((notificationActionNumber != expectedNumber) && (cpt != timeout));

        if (notificationActionNumber != expectedNumber) {
            fail(String.format("Wrong notifications number in database after timeout expected %s got %s",
                               expectedNumber, notificationActionNumber));
        }
    }

    /**
     * Init 1 rule and RECIPIENTS_PER_RULE {@link Recipient}, one of the {@link Recipient} will fail
     * if the param fail is set to true
     * @param fail
     */
    protected void initPlugins(boolean fail) {
        // configuration of the rule plugin
        PluginConfiguration rulePlugin = new PluginConfiguration();
        rulePlugin.setBusinessId("testRule");
        rulePlugin.setVersion("1.0.0");
        rulePlugin.setLabel("test");
        rulePlugin.setPluginId("DefaultRuleMatcher");

        StringPluginParam param = IPluginParam.build("attributeToSeek", "nature");
        rulePlugin.getParameters().add(param);
        param = IPluginParam.build("attributeValueToSeek", "TM");
        rulePlugin.getParameters().add(param);

        rulePlugin = this.pluginConfRepo.save(rulePlugin);

        // configuration of the default recipient sender plugin
        PluginConfiguration recipientPlugin = new PluginConfiguration();
        recipientPlugin.setBusinessId(fail ? "failRecipient" : "testRecipient");
        recipientPlugin.setVersion("1.0.0");
        recipientPlugin.setLabel("test recipient");
        recipientPlugin.setPluginId(fail ? "fail" : "DefaultRecipientSender");
        recipientPlugin = this.pluginConfRepo.save(recipientPlugin);

        Rule rule = Rule.build(null, rulePlugin, true);
        rule = this.ruleRepo.save(rule);

        Recipient recipient = Recipient.build(rule, recipientPlugin);
        this.recipientRepo.save(recipient);

        // configuration of the fake recipient sender (for test)
        for (int i = 1; i < RECIPIENTS_PER_RULE; i++) {
            recipientPlugin = new PluginConfiguration();
            recipientPlugin.setBusinessId("testRecipient" + (i + 1));
            recipientPlugin.setVersion("1.0.0");
            recipientPlugin.setLabel("test recipient");
            recipientPlugin.setPluginId("RecipientSender" + (i + 1));
            recipientPlugin = this.pluginConfRepo.save(recipientPlugin);
            recipient = Recipient.build(rule, recipientPlugin);
            this.recipientRepo.save(recipient);
        }
    }

    /**
     * load {@link JsonElement} from a resource test
     * @return initialised {@link JsonElement}
     */
    protected JsonElement initElement() {
        try (InputStream input = this.getClass().getResourceAsStream("element.json");
                Reader reader = new InputStreamReader(input)) {

            return gson.fromJson(CharStreams.toString(reader), JsonElement.class);
        } catch (IOException e) {
            String errorMessage = "Cannot import model";
            LOGGER.debug(errorMessage);
            throw new AssertionError(errorMessage);
        }
    }

    @After
    public void after() {
        subscriber.unsubscribeFrom(NotificationActionEvent.class);
        cleanAMQPQueues(NotificationActionEventHandler.class, Target.ONE_PER_MICROSERVICE_TYPE);
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
