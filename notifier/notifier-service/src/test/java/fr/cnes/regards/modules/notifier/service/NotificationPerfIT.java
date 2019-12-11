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
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.google.gson.JsonElement;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.reguards.modules.notifier.dto.in.NotificationActionEvent;

/**
 * Performance tests on Notification manager
 * @author Kevin Marchois
 *
 */
@TestPropertySource(
        properties = { "spring.jpa.properties.hibernate.default_schema=notification_perf",
                "regards.amqp.enabled=true" },
        locations = { "classpath:regards_perf.properties", "classpath:batch.properties" })
@ActiveProfiles(value = { "testAmqp" })
public class NotificationPerfIT extends AbstractNotificationMultitenantServiceTest {

    @Test
    public void testRegistrationAndProcessing()
            throws ExecutionException, NotAvailablePluginConfigurationException, ModuleException, InterruptedException {

        JsonElement feature = initElement();

        initPlugins(false);

        List<NotificationActionEvent> events = new ArrayList<NotificationActionEvent>();
        for (int i = 0; i < configuration.getMaxBulkSize(); i++) {
            events.add(NotificationActionEvent.build(feature, "CREATE"));
        }
        this.publisher.publish(events);
        // we should have  configuration.getMaxBulkSize() NotificationAction in database
        waitDatabaseCreation(this.notificationRepo, configuration.getMaxBulkSize(), 60);
        this.notificationService.scheduleRequests();
        // all send should work so we should have 0 NotificationAction left in database
        waitDatabaseCreation(this.notificationRepo, 0, 60);
        assertEquals(0, this.recipientErrorRepo.count());

    }

    @Test
    public void testRegistrationAndProcessingWith1RecipientFail()
            throws ExecutionException, NotAvailablePluginConfigurationException, ModuleException, InterruptedException {
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
        assertEquals(this.notificationRepo.count(), configuration.getMaxBulkSize().intValue());

    }
}
