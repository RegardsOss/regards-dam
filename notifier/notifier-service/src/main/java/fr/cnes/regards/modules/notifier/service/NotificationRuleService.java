/**
 *
 */
package fr.cnes.regards.modules.notifier.service;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.notification.domain.plugin.IRecipientSender;
import fr.cnes.regards.modules.notification.domain.plugin.IRuleMatcher;
import fr.cnes.regards.modules.notifier.cache.AbstractCacheableRule;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.Rule;

/**
 * Service for checking {@link Rule} applied to {@link Feature} for notification sending
 * @author kevin
 *
 */
@Service
@MultitenantTransactional
public class NotificationRuleService extends AbstractCacheableRule implements INotificationRuleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationRuleService.class);

    @Autowired
    private IPluginService pluginService;

    @Override
    public int handleFeatures(Feature toHandle)
            throws ExecutionException, ModuleException, NotAvailablePluginConfigurationException {
        int notificationNumber = 0;
        for (Rule rule : getRules()) {
            try {
                // check if the  feature match with the rule
                if (((IRuleMatcher) this.pluginService.getPlugin(rule.getPluginCondConfiguration().getBusinessId()))
                        .match(toHandle)) {
                    for (Recipient recipient : rule.getRecipients()) {
                        if (notifyRecipient(toHandle, recipient)) {
                            notificationNumber++;
                        } else {
                            // TODO notifier feature manager

                        }
                    }
                }
            } catch (ModuleException | NotAvailablePluginConfigurationException e) {
                LOGGER.error("Error while get plugin with id {}", rule.getPluginCondConfiguration().getId(), e);
                throw e;
            }
        }
        return notificationNumber;
    }

    /**
     * Notify a recipient return false if a problem occurs
     * @param toHandle {@link Feature} about to notify
     * @param recipient {@link Recipient} of the notification
     * @return
     */
    private boolean notifyRecipient(Feature toHandle, Recipient recipient) {
        try {
            // check that all send method of recipiens return true
            return ((IRecipientSender) this.pluginService
                    .getPlugin(recipient.getPluginCondConfiguration().getBusinessId())).send(toHandle);
        } catch (ModuleException | NotAvailablePluginConfigurationException e) {
            LOGGER.error("Error while sending notification to receiver ", e);
            return false;
        }
    }

}
