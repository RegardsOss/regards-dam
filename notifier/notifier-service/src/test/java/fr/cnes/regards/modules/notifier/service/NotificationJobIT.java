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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.google.gson.JsonElement;

import fr.cnes.regards.framework.modules.jobs.domain.JobInfo;
import fr.cnes.regards.framework.modules.jobs.domain.JobStatus;
import fr.cnes.regards.modules.notifier.domain.NotificationAction;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.RecipientError;
import fr.cnes.reguards.modules.notifier.dto.in.NotificationActionEvent;

/**
 * Test notification job restart after recipient failure
 * @author Kevin Marchois
 *
 */
@TestPropertySource(
        properties = { "spring.jpa.properties.hibernate.default_schema=notification_job", "regards.amqp.enabled=true" },
        locations = { "classpath:regards_perf.properties", "classpath:batch.properties" })
@ActiveProfiles(value = { "testAmqp" })
public class NotificationJobIT extends AbstractNotificationMultitenantServiceTest {

    /**
     * Test notification job restart after {@link Recipient} fail
     * After the job fail we will restart ir then the {@link Recipient} should work
     * All {@link RecipientError} and {@link NotificationAction} must be deleted
     * @throws InterruptedException
     */
    @Test
    public void testRestartAfterFailure() throws InterruptedException {

        JsonElement element = initElement();

        initPlugins(true);

        List<NotificationActionEvent> events = new ArrayList<NotificationActionEvent>();
        for (int i = 0; i < configuration.getMaxBulkSize(); i++) {
            events.add(NotificationActionEvent.build(element, "CREATE"));
        }
        this.publisher.publish(events);
        // we should have  configuration.getMaxBulkSize() NotificationAction in database
        waitDatabaseCreation(this.notificationRepo, configuration.getMaxBulkSize(), 60);
        this.notificationService.scheduleRequests();
        // we will wait util configuration.getMaxBulkSize() recipient errors are stored in database
        // cause one of the RECIPIENTS_PER_RULE will fail so we will get 1 error per NotificationAction to send
        waitDatabaseCreation(this.recipientErrorRepo, configuration.getMaxBulkSize(), 60);
        JobInfo failJob = this.jobInforepo.findAll().iterator().next();
        failJob.updateStatus(JobStatus.QUEUED);
        RECIPIENT_FAIL = false;
        this.jobInforepo.save(failJob);

        waitDatabaseCreation(this.recipientErrorRepo, 0, 60);
        waitDatabaseCreation(this.notificationRepo, 0, 60);
        assertEquals(JobStatus.SUCCEEDED, this.jobInforepo.findAll().iterator().next().getStatus().getStatus());
    }
}
