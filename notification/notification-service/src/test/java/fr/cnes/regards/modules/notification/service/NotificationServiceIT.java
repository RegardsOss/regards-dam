/**
 *
 */
package fr.cnes.regards.modules.notification.service;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.StringPluginParam;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.model.dto.properties.AbstractProperty;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;
import fr.cnes.regards.modules.notification.domain.Recipient;
import fr.cnes.regards.modules.notification.domain.Rule;
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

    @Autowired
    private INotificationRuleService notificationService;

    @Test
    public void testRuleMacher() throws NotAvailablePluginConfigurationException, ModuleException {
        Feature feature = Feature.builder(null, IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA,
                                          "model", "id");
        Set<AbstractProperty<?>> properties = new HashSet<>();
        Set<AbstractProperty<?>> property1Set = new HashSet<AbstractProperty<?>>();
        AbstractProperty<String> prop = new AbstractProperty<String>() {

            @Override
            public boolean represents(PropertyType pAttributeType) {
                // TODO Auto-generated method stub
                return false;
            }
        };
        AbstractProperty<Set<AbstractProperty<?>>> property1 = new AbstractProperty<Set<AbstractProperty<?>>>() {

            @Override
            public boolean represents(PropertyType pAttributeType) {
                // TODO Auto-generated method stub
                return false;
            }
        };
        property1.setName("file_infos");
        property1.setValue(property1Set);
        property1Set.add(prop);
        prop.setName("fem_type");
        prop.setValue("TM");
        properties.add(property1);
        feature.setProperties(properties);

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

        Rule rule = Rule.builder(NotificationType.IMMEDIATE, rulePlugin);
        this.ruleRepo.save(rule);

        PluginConfiguration recipientPlugin = new PluginConfiguration();
        recipientPlugin.setBusinessId("testRecipient");
        recipientPlugin.setVersion("1.0.0");
        recipientPlugin.setLabel("test recipient");
        recipientPlugin.setPluginId("DefaultRecipientSender");
        recipientPlugin = this.pluginConfRepo.save(recipientPlugin);
        Recipient recipient = Recipient.builder(rule, recipientPlugin);
        this.recipientRepo.save(recipient);

        assertTrue(this.notificationService.handleFeatures(feature));
    }

}
