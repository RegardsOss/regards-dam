/**
 *
 */
package fr.cnes.regards.modules.notification.domain.plugin;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.feature.dto.Feature;

/**
 * @author kevin
 *
 */
@FunctionalInterface
@PluginInterface(description = "Recipient sender plugin")
public interface IRecipientSender {

    boolean send(Feature feature);

}
