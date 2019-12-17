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
package fr.cnes.regards.modules.feature.service.job;

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
import fr.cnes.regards.modules.feature.dao.INotificationRequestRepository;
import fr.cnes.regards.modules.feature.domain.request.NotificationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.service.INotificationService;
import fr.cnes.reguards.modules.notifier.dto.in.NotificationActionEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Publish {@link NotificationActionEvent} for notify {@link Feature}
 * @author Kevin Marchois
 *
 */
public class NotificationRequestJob extends AbstractJob<Void> {

    public static final String IDS_PARAMETER = "ids";

    private List<NotificationRequest> notificationRequests;

    @Autowired
    private INotificationRequestRepository notificationRequestRepo;

    @Autowired
    private MeterRegistry registry;

    @Autowired
    private INotificationService notificationService;

    @Override
    public void setParameters(Map<String, JobParameter> parameters)
            throws JobParameterMissingException, JobParameterInvalidException {
        Type type = new TypeToken<Set<Long>>() {

        }.getType();
        notificationRequests = this.notificationRequestRepo.findAllById(getValue(parameters, IDS_PARAMETER, type));
    }

    @Override
    public void run() {
        Timer timer = Timer.builder(this.getClass().getName()).tag("job", "run").register(registry);
        LOGGER.info("[{}] Feature notification request job starts", jobInfoId);
        long start = System.currentTimeMillis();
        timer.record(() -> notificationService.processRequests(notificationRequests));
        LOGGER.info("[{}]{}{} notifications request(s) processed in {} ms", jobInfoId, INFO_TAB,
                    notificationRequests.size(), System.currentTimeMillis() - start);
    }
}
