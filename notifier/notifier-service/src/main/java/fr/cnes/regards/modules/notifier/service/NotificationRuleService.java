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

import java.util.List;
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
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureEvent;
import fr.cnes.regards.modules.notification.domain.plugin.IRecipientSender;
import fr.cnes.regards.modules.notification.domain.plugin.IRuleMatcher;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.regards.modules.notifier.service.cache.AbstractCacheableRule;

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
    public int handleFeature(Feature toHandle, FeatureManagementAction action)
            throws ExecutionException, ModuleException, NotAvailablePluginConfigurationException {
        int notificationNumber = 0;
        for (Rule rule : getRules()) {
            try {
                // check if the  feature match with the rule
                if (((IRuleMatcher) this.pluginService.getPlugin(rule.getPluginCondConfiguration().getBusinessId()))
                        .match(toHandle)) {

                    for (Recipient recipient : rule.getRecipients()) {
                        if (notifyRecipient(toHandle, recipient, action)) {
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
     * @param action {@link FeatureManagementAction} action done on {@link Feature}
     * @return
     */
    private boolean notifyRecipient(Feature toHandle, Recipient recipient, FeatureManagementAction action) {
        try {
            // check that all send method of recipiens return true
            return ((IRecipientSender) this.pluginService
                    .getPlugin(recipient.getPluginCondConfiguration().getBusinessId())).send(toHandle, action);
        } catch (ModuleException | NotAvailablePluginConfigurationException e) {
            LOGGER.error("Error while sending notification to receiver ", e);
            return false;
        }
    }

    @Override
    public int handleFeatures(List<FeatureEvent> toHandles) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("------------->>> Reception of {} Feature event, start of notification process {} ms",
                     toHandles.size(), startTime);
        int nbSend = 0;
        long averageFeatureTreatmentTime = 0;
        long startFeatureTreatmentTime = 0;

        for (FeatureEvent feature : toHandles) {
            startFeatureTreatmentTime = System.currentTimeMillis();
            try {
                nbSend += handleFeature(feature.getFeature(), feature.getAction());
            } catch (ExecutionException | ModuleException | NotAvailablePluginConfigurationException e) {
                LOGGER.error("Error during feature notification", e);
            }
            averageFeatureTreatmentTime += System.currentTimeMillis() - startFeatureTreatmentTime;
        }
        LOGGER.debug("------------->>> End of notification process in {} ms, {} notifications sended"
                + " with a average feature treatment time of {} ms", System.currentTimeMillis() - startTime, nbSend,
                     averageFeatureTreatmentTime / (nbSend == 0 ? 1 : nbSend));
        return nbSend;
    }

}
