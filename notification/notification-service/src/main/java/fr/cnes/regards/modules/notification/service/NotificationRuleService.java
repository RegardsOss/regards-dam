/**
 *
 */
package fr.cnes.regards.modules.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.service.PluginService;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.notification.dao.IRuleRepository;
import fr.cnes.regards.modules.notification.domain.Rule;
import fr.cnes.regards.modules.notification.domain.plugin.IRuleMatcher;
import fr.cnes.reguards.modules.dto.type.NotificationType;

/**
 * @author kevin
 *
 */
@Service
@MultitenantTransactional
public class NotificationRuleService implements INotificationRuleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationRuleService.class);

    @Autowired
    private PluginService pluginService;

    @Autowired
    private IRuleRepository ruleRepo;

    @Override
    public boolean handleFeatures(Feature toHandle) throws NotAvailablePluginConfigurationException, ModuleException {
        // FIXME gestion du cache
        for (Rule rule : this.ruleRepo.findByEnableTrueAndType(NotificationType.IMMEDIATE)) {
            try {
                if (((IRuleMatcher) this.pluginService.getPlugin(rule.getPluginCondConfiguration().getId()))
                        .match(toHandle)) {
                    // TODO envoyer message au destinataires

                    return true;
                }
            } catch (ModuleException | NotAvailablePluginConfigurationException e) {
                LOGGER.error("Error while get plugin with id {}", rule.getPluginCondConfiguration().getId(), e);
                throw e;
            }
        }
        return false;

    }

}
