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

import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.StringPluginParam;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
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
public class NotificationServiceIT extends AbstractNotificationMultitenantServiceTest {

    @Test
    public void testRuleMacher() throws ExecutionException, NotAvailablePluginConfigurationException, ModuleException {

        String model = mockModelClient("feature_model_01.xml", cps, factory, this.getDefaultTenant(),
                                       modelAttrAssocClientMock);
        Feature feature = Feature
                .build("id",
                       FeatureUniformResourceName.pseudoRandomUrn(FeatureIdentifier.FEATURE, EntityType.DATA,
                                                                  getDefaultTenant(), 1),
                       IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA, model);

        // Properties of the feature
        Set<IProperty<?>> properties = IProperty
                .set(IProperty.buildObject("file_infos", IProperty.buildString("fem_type", "TM")));
        feature.setProperties(properties);

        // configuration of the rule plugin
        PluginConfiguration rulePlugin = new PluginConfiguration();

        rulePlugin.setBusinessId("testRule");
        rulePlugin.setVersion("1.0.0");
        rulePlugin.setLabel("test");
        rulePlugin.setPluginId("DefaultRuleMatcher");

        StringPluginParam param = new StringPluginParam();
        param.setName("attributeToSeek");
        param.setValue("file_infos.fem_type");
        rulePlugin.getParameters().add(param);
        param = new StringPluginParam();
        param.setName("attributeValueToSeek");
        param.setValue("TM");
        rulePlugin.getParameters().add(param);

        rulePlugin = this.pluginConfRepo.save(rulePlugin);

        Rule rule = Rule.build(null, rulePlugin, true, NotificationType.IMMEDIATE);
        this.ruleRepo.save(rule);

        // configuration of the recipient sender plugin
        PluginConfiguration recipientPlugin = new PluginConfiguration();
        recipientPlugin.setBusinessId("testRecipient");
        recipientPlugin.setVersion("1.0.0");
        recipientPlugin.setLabel("test recipient");
        recipientPlugin.setPluginId("DefaultRecipientSender");
        recipientPlugin = this.pluginConfRepo.save(recipientPlugin);
        Recipient recipient = Recipient.build(rule, recipientPlugin);
        this.recipientRepo.save(recipient);

        assertEquals(1, this.notificationService.handleFeature(feature, FeatureManagementAction.CREATE));

    }

    @Test
    public void testRuleMacherWithNonMatcherFeature()
            throws NotAvailablePluginConfigurationException, ModuleException, ExecutionException {

        Feature feature = Feature
                .build("id",
                       FeatureUniformResourceName.pseudoRandomUrn(FeatureIdentifier.FEATURE, EntityType.DATA,
                                                                  getDefaultTenant(), 1),
                       IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA, "model");

        // Properties of the feature
        Set<IProperty<?>> properties = IProperty
                .set(IProperty.buildObject("file_infos", IProperty.buildString("fem_type", "Not TM")));
        feature.setProperties(properties);

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

        Rule rule = Rule.build(null, rulePlugin, true, NotificationType.IMMEDIATE);
        this.ruleRepo.save(rule);

        // configuration of the recipient sender plugin
        PluginConfiguration recipientPlugin = new PluginConfiguration();
        recipientPlugin.setBusinessId("testRecipient");
        recipientPlugin.setVersion("1.0.0");
        recipientPlugin.setLabel("test recipient");
        recipientPlugin.setPluginId("DefaultRecipientSender");
        recipientPlugin = this.pluginConfRepo.save(recipientPlugin);
        Recipient recipient = Recipient.build(rule, recipientPlugin);

        this.recipientRepo.save(recipient);
        assertEquals(0, this.notificationService.handleFeature(feature, FeatureManagementAction.CREATE));

    }

}
