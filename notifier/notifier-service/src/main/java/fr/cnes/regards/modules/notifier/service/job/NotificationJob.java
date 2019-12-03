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
package fr.cnes.regards.modules.notifier.service.job;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.reflect.TypeToken;

import fr.cnes.regards.framework.modules.jobs.domain.AbstractJob;
import fr.cnes.regards.framework.modules.jobs.domain.JobParameter;
import fr.cnes.regards.framework.modules.jobs.domain.exception.JobParameterInvalidException;
import fr.cnes.regards.framework.modules.jobs.domain.exception.JobParameterMissingException;
import fr.cnes.regards.modules.notifier.dao.INotificationRequestRepository;
import fr.cnes.regards.modules.notifier.domain.NotificationRequest;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.regards.modules.notifier.service.INotificationRuleService;

/**
 * Job implementation to notify {@link Recipient} according {@link Rule}
 * @author Kevin Marchois
 *
 */
public class NotificationJob extends AbstractJob<Void> {

    public static final String IDS_PARAMETER = "ids";

    private List<NotificationRequest> notificationRequests;

    @Autowired
    private INotificationRequestRepository notificationRequestsRepo;

    @Autowired
    private INotificationRuleService notificationService;

    @Override
    public void setParameters(Map<String, JobParameter> parameters)
            throws JobParameterMissingException, JobParameterInvalidException {
        Type type = new TypeToken<Set<Long>>() {

        }.getType();
        notificationRequests = this.notificationRequestsRepo.findAllById(getValue(parameters, IDS_PARAMETER, type));
    }

    @Override
    public void run() {
        LOGGER.info("[{}] Notification job starts", jobInfoId);
        long start = System.currentTimeMillis();
        int notificationNumber = this.notificationService.processRequest(notificationRequests);
        LOGGER.info("[{}]{}{} notification processed in {} ms", jobInfoId, INFO_TAB, notificationNumber,
                    System.currentTimeMillis() - start);

    }

}
