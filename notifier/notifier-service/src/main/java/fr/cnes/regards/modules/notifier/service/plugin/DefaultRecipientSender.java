/**
 *
 */
package fr.cnes.regards.modules.notifier.service.plugin;

import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;
import fr.cnes.regards.modules.notification.domain.plugin.IRecipientSender;
import fr.cnes.reguards.modules.notifier.dto.out.NotificationEvent;

/**
 * @author kevin
 *
 */
@Plugin(author = "REGARDS Team", description = "Default rule matcher for feature", id = "DefaultRecipientSender",
        version = "1.0.0", contact = "regards@c-s.fr", license = "GPLv3", owner = "CNES",
        url = "https://regardsoss.github.io/")
public class DefaultRecipientSender implements IRecipientSender {

    @Autowired
    private IPublisher publisher;

    @Override
    public boolean send(Feature feature, FeatureManagementAction action) {
        this.publisher.publish(NotificationEvent.build(feature, action));
        return true;
    }
}
