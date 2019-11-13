/**
 *
 */
package fr.cnes.regards.modules.notifier.service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureEvent;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.Rule;

/**
 * @author kevin
 *
 */
public interface INotificationRuleService {

    /**
     * Handle a {@link Feature} and check if it matches with enabled {@link Rule} in that case
     * send a notification to a {@link Recipient}
     * @param toHandle {@link Feature} to handle
     * @param action action did of the {@link Feature} to handle
     * @return number of notification sended
     * @throws NotAvailablePluginConfigurationException
     * @throws ModuleException
     * @throws ExecutionException
     */
    public int handleFeature(Feature toHandle, FeatureManagementAction action)
            throws NotAvailablePluginConfigurationException, ModuleException, ExecutionException;

    /**
     * Handle a list of {@link FeatureEvent} it can be CREATE/UPDATE/DELETE event on a {@link Feature}
     * Check if this event is compliant with a {@link Rule} and in that case notify all {@link Recipient} associated
     * with this {@link Rule}
     * @param toHandles
     * @return
     */
    public int handleFeatures(List<FeatureEvent> toHandles);
}
