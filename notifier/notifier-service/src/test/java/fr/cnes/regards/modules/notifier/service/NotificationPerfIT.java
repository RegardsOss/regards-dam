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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.IPluginParam;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.StringPluginParam;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;
import fr.cnes.regards.modules.notifier.domain.NotificationAction;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.regards.modules.notifier.domain.state.NotificationState;
import fr.cnes.regards.modules.notifier.dto.NotificationEvent10;
import fr.cnes.regards.modules.notifier.dto.NotificationEvent2;
import fr.cnes.regards.modules.notifier.dto.NotificationEvent3;
import fr.cnes.regards.modules.notifier.dto.NotificationEvent4;
import fr.cnes.regards.modules.notifier.dto.NotificationEvent5;
import fr.cnes.regards.modules.notifier.dto.NotificationEvent6;
import fr.cnes.regards.modules.notifier.dto.NotificationEvent7;
import fr.cnes.regards.modules.notifier.dto.NotificationEvent8;
import fr.cnes.regards.modules.notifier.dto.NotificationEvent9;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender10;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender2;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender3;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender4;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender5;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender6;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender7;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender8;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender9;
import fr.cnes.reguards.modules.dto.type.NotificationType;

/**
 * @author kevin
 *
 */
@TestPropertySource(
        properties = { "spring.jpa.properties.hibernate.default_schema=notification", "regards.amqp.enabled=true" },
        locations = { "classpath:regards_perf.properties", "classpath:batch.properties" })
@ActiveProfiles(value = { "testAmqp" })
public class NotificationPerfIT extends AbstractNotificationMultitenantServiceTest {

    private static final int RECIPIENTS_PER_RULE = 10;

    private static final int FEATURE_EVENT_TO_RECEIVE = 10000;

    private static final int FEATURE_EVENT_BULK = 1000;

    @Autowired
    private ISubscriber subscriber;

    @Override
    @Before
    public void before() throws InterruptedException {
        super.before();

        subOrNot(NotificationEvent2.class, new RecipientSender2());
        subOrNot(NotificationEvent3.class, new RecipientSender3());
        subOrNot(NotificationEvent4.class, new RecipientSender4());
        subOrNot(NotificationEvent5.class, new RecipientSender5());
        subOrNot(NotificationEvent6.class, new RecipientSender6());
        subOrNot(NotificationEvent7.class, new RecipientSender7());
        subOrNot(NotificationEvent8.class, new RecipientSender8());
        subOrNot(NotificationEvent9.class, new RecipientSender9());
        subOrNot(NotificationEvent10.class, new RecipientSender10());
    }

    private <E extends ISubscribable> void subOrNot(Class<E> eventType, IHandler<E> handler) {
        subscriber.subscribeTo(eventType, handler);
        subscriber.unsubscribeFrom(eventType);
    }

    @Test
    public void testPerf() throws InterruptedException {

        String modelName = mockModelClient(GeodeProperties.getGeodeModel());

        Thread.sleep(5_000);

        Feature modifiedFeature = Feature.build("id", null, null, EntityType.DATA, modelName);

        // Properties of the feature
        GeodeProperties.addGeodeProperties(modifiedFeature);

        initPlugins(false);

        List<NotificationAction> events = new ArrayList<>();
        int bulk = 0;
        for (int i = 0; i < FEATURE_EVENT_TO_RECEIVE; i++) {
            bulk++;
            events.add(NotificationAction.build(modifiedFeature, FeatureManagementAction.CREATE,
                                                NotificationState.DELAYED));
            if (bulk == FEATURE_EVENT_BULK) {
                bulk = 0;
                assertEquals(FEATURE_EVENT_BULK * RECIPIENTS_PER_RULE, this.notificationService.processRequest(events));
                events.clear();
            }
        }

        if (bulk > 0) {
            assertEquals(bulk * RECIPIENTS_PER_RULE, this.notificationService.processRequest(events));
        }
    }

    private void initPlugins(boolean fail) {
        // configuration of the rule plugin
        PluginConfiguration rulePlugin = new PluginConfiguration();
        rulePlugin.setBusinessId("testRule");
        rulePlugin.setVersion("1.0.0");
        rulePlugin.setLabel("test");
        rulePlugin.setPluginId("DefaultRuleMatcher");

        StringPluginParam param = IPluginParam.build("attributeToSeek", "file_infos.nature");
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

        Rule rule = Rule.build(null, rulePlugin, true, NotificationType.IMMEDIATE);
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
     * In that test one the the fake RecipientSender will fail
     */
    @Test
    public void testPerfWithFail() {
        String model = mockModelClient(GeodeProperties.getGeodeModel());

        Feature modifiedFeature = Feature.build("id", null, null, EntityType.DATA, model);
        GeodeProperties.addGeodeProperties(modifiedFeature);

        initPlugins(true);

        List<NotificationAction> events = new ArrayList<>();
        for (int i = 0; i < FEATURE_EVENT_TO_RECEIVE; i++) {
            events.add(NotificationAction.build(modifiedFeature, FeatureManagementAction.CREATE,
                                                NotificationState.DELAYED));
        }

        assertEquals(FEATURE_EVENT_TO_RECEIVE * (RECIPIENTS_PER_RULE - 1),
                     this.notificationService.processRequest(events));
    }
}
