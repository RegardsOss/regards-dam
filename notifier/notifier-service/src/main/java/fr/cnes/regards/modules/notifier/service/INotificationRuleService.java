/**
 *
 */
package fr.cnes.regards.modules.notifier.service;

import java.util.concurrent.ExecutionException;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.Rule;

/**
 * @author kevin
 *
 */
public interface INotificationRuleService {

    /**
     * Handle a {@link Feature} and check if it matches with enabled {@link Rule} in that case
     * send a notifaction to a {@link Recipient}
     * @param toHandle {@link Feature} to handle
     * @return number of notifacations sended
     * @throws NotAvailablePluginConfigurationException
     * @throws ModuleException
     * @throws ExecutionException
     */
    public int handleFeatures(Feature toHandle)
            throws NotAvailablePluginConfigurationException, ModuleException, ExecutionException;
}
