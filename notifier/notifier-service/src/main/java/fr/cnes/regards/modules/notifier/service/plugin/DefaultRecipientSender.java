/**
 *
 */
package fr.cnes.regards.modules.notifier.service.plugin;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.notification.domain.plugin.IRecipientSender;

/**
 * @author kevin
 *
 */
@Plugin(author = "REGARDS Team", description = "Default rule matcher for feature", id = "DefaultRecipientSender",
        version = "1.0.0", contact = "regards@c-s.fr", license = "GPLv3", owner = "CNES",
        url = "https://regardsoss.github.io/")
public class DefaultRecipientSender implements IRecipientSender {

    @Override
    public boolean send(Feature feature) {
        // TODO faire quelque chose! mais quoi?
        return !feature.getModel().equals("fail");
    }

}
