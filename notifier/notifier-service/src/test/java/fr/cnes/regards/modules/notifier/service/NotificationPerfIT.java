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
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.StringPluginParam;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureEvent;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.reguards.modules.dto.type.NotificationType;

/**
 * @author kevin
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=notification",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp" })
public class NotificationPerfIT extends AbstractNotificationMultitenantServiceTest {

    private static final int RECIPIENTS_PER_RULE = 10;

    private static final int FEATURE_EVENT_TO_RECEIVE = 1000;

    @Autowired
    ISubscriber sub;

    @Test
    public void testPerf() {
        String model = mockModelClient("feature_model_01.xml", cps, factory, this.getDefaultTenant(),
                                       modelAttrAssocClientMock);
        Feature modifiedFeature = Feature.build("id", null, null, EntityType.DATA, model);
        // Properties of the feature
        Set<IProperty<?>> properties = IProperty
                .set(IProperty.buildObject("file_infos", IProperty.buildString("fem_type", "TM")));
        modifiedFeature.setProperties(properties);

        initPlugins(false);

        List<FeatureEvent> events = new ArrayList<>();
        for (int i = 0; i < FEATURE_EVENT_TO_RECEIVE; i++) {
            events.add(FeatureEvent.build(modifiedFeature, FeatureManagementAction.CREATE));
        }

        assertEquals(FEATURE_EVENT_TO_RECEIVE * RECIPIENTS_PER_RULE, this.notificationService.handleFeatures(events));

    }

    private void initPlugins(boolean fail) {
        // configuration of the rule plugin
        PluginConfiguration rulePlugin = new PluginConfiguration();
        rulePlugin.setBusinessId("testRule");
        rulePlugin.setVersion("1.0.0");
        rulePlugin.setLabel("test");
        rulePlugin.setPluginId("DefaultRuleMatcher");

        StringPluginParam param = new StringPluginParam();
        param.setName("attributeToSeek");
        param.setValue("fem_type");
        rulePlugin.getParameters().add(param);
        param = new StringPluginParam();
        param.setName("attributeValueToSeek");
        param.setValue("TM");
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
        String model = mockModelClient("feature_model_01.xml", cps, factory, this.getDefaultTenant(),
                                       modelAttrAssocClientMock);
        Feature modifiedFeature = Feature.build("id", null, null, EntityType.DATA, model);
        // Properties of the feature
        Set<IProperty<?>> properties = IProperty
                .set(IProperty.buildObject("file_infos", IProperty.buildString("fem_type", "TM")));
        modifiedFeature.setProperties(properties);

        initPlugins(true);

        List<FeatureEvent> events = new ArrayList<>();
        for (int i = 0; i < FEATURE_EVENT_TO_RECEIVE; i++) {
            events.add(FeatureEvent.build(modifiedFeature, FeatureManagementAction.CREATE));
        }

        assertEquals(FEATURE_EVENT_TO_RECEIVE * (RECIPIENTS_PER_RULE - 1),
                     this.notificationService.handleFeatures(events));
    }
}
