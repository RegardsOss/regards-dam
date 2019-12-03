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

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.authentication.IAuthenticationResolver;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.jobs.domain.JobInfo;
import fr.cnes.regards.framework.modules.jobs.domain.JobParameter;
import fr.cnes.regards.framework.modules.jobs.service.IJobInfoService;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;
import fr.cnes.regards.modules.notifier.dao.INotificationRequestRepository;
import fr.cnes.regards.modules.notifier.domain.NotificationRequest;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.regards.modules.notifier.plugin.IRecipientNotifier;
import fr.cnes.regards.modules.notifier.plugin.IRuleMatcher;
import fr.cnes.regards.modules.notifier.service.cache.AbstractCacheableRule;
import fr.cnes.regards.modules.notifier.service.conf.NotificationConfigurationProperties;
import fr.cnes.regards.modules.notifier.service.job.NotificationJob;
import fr.cnes.reguards.modules.notifier.dto.in.NotificationRequestEvent;

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

    @Autowired
    private INotificationRequestRepository notificationRequestRepo;

    @Autowired
    private Validator validator;

    @Autowired
    private NotificationConfigurationProperties properties;

    @Autowired
    private IAuthenticationResolver authResolver;

    @Autowired
    private IJobInfoService jobInfoService;

    @Override
    public int handleFeature(Feature toHandle, FeatureManagementAction action)
            throws ExecutionException, ModuleException, NotAvailablePluginConfigurationException {
        int notificationNumber = 0;
        for (Rule rule : getRules()) {
            try {
                // check if the  feature match with the rule
                if (((IRuleMatcher) this.pluginService.getPlugin(rule.getRulePlugin().getBusinessId()))
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
                LOGGER.error("Error while get plugin with id {}", rule.getRulePlugin().getId(), e);
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
            return ((IRecipientNotifier) this.pluginService.getPlugin(recipient.getRecipientPlugin().getBusinessId()))
                    .send(toHandle, action);
        } catch (ModuleException | NotAvailablePluginConfigurationException e) {
            LOGGER.error("Error while sending notification to receiver ", e);
            return false;
        }
    }

    @Override
    public int processRequest(List<NotificationRequest> toHandles) {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("------------->>> Reception of {} Feature event, start of notification process {} ms",
                     toHandles.size(), startTime);
        int nbSend = 0;
        long averageFeatureTreatmentTime = 0;
        long startFeatureTreatmentTime = 0;

        for (NotificationRequest feature : toHandles) {
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

    @Override
    public void registerNotifications(List<NotificationRequestEvent> events) {
        Set<NotificationRequest> notificationToRegister = events.stream().map(event -> initNotificationRequest(event))
                .collect(Collectors.toSet());
        notificationToRegister.removeAll(null);
        this.notificationRequestRepo.saveAll(notificationToRegister);
    }

    /**
     * Check if a notification event is valid and create a request publish an error otherwise
     * @param event
     * @return a implemented {@link NotificationRequest} or null if invalid
     */
    private NotificationRequest initNotificationRequest(NotificationRequestEvent event) {
        Errors errors = new MapBindingResult(new HashMap<>(), NotificationRequestEvent.class.getName());
        this.validator.validate(event, errors);

        if (!errors.hasErrors()) {
            return NotificationRequest.build(event.getFeature(), event.getAction());
        }
        //TODO on fait quoi?
        return null;
    }

    @Override
    public int scheduleRequests() {
        List<NotificationRequest> notificationToschedule = this.notificationRequestRepo
                .findAll(PageRequest.of(0, properties.getMaxBulkSize(), Sort.by(Order.asc("requestDate"))))
                .getContent();
        // Shedule job
        Set<JobParameter> jobParameters = Sets.newHashSet();
        Set<Long> requestIds = notificationToschedule.stream().map(request -> request.getId())
                .collect(Collectors.toSet());
        long scheduleStart = System.currentTimeMillis();

        jobParameters.add(new JobParameter(NotificationJob.IDS_PARAMETER, requestIds));

        // the job priority will be set according the priority of the first request to schedule
        JobInfo jobInfo = new JobInfo(false, 0, jobParameters, authResolver.getUser(), NotificationJob.class.getName());
        jobInfoService.createAsQueued(jobInfo);

        LOGGER.trace("------------->>> {} notifications scheduled in {} ms", requestIds.size(),
                     System.currentTimeMillis() - scheduleStart);
        return requestIds.size();
    }

}
