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
package fr.cnes.regards.modules.feature.service;

import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.google.gson.Gson;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.in.NotificationRequestEvent;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.reguards.modules.notifier.dto.in.NotificationActionEvent;

/**
 * Test for {@link NotificationRequestEvent} publishing
 * @author Kevin Marchois
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature_notif",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp", "noscheduler" })
@DirtiesContext
public class FeatureNotificationServiceIT extends AbstractFeatureMultitenantServiceTest {

    @Autowired
    private IFeatureNotificationService notificationService;

    @SpyBean
    private IPublisher publisher;

    @Autowired
    private Gson gson;

    @Captor
    private ArgumentCaptor<List<NotificationActionEvent>> recordsCaptor;

    @Test
    public void testNotification() {

        // mock the publish method to not broke other tests
        Mockito.doNothing().when(publisher).publish(Mockito.any(NotificationActionEvent.class));

        // use it only to initialize Feature
        List<FeatureCreationRequestEvent> list = new ArrayList<>();
        initFeatureCreationRequestEvent(list, 2);
        list.get(0).getFeature().setUrn(FeatureUniformResourceName.pseudoRandomUrn(FeatureIdentifier.FEATURE,
                                                                                   EntityType.DATA, "tenant", 1));
        list.get(1).getFeature().setUrn(FeatureUniformResourceName.pseudoRandomUrn(FeatureIdentifier.FEATURE,
                                                                                   EntityType.DATA, "tenant", 1));
        FeatureEntity createdEntity = FeatureEntity.build("moi", "session", list.get(0).getFeature(), null);
        FeatureEntity updatedEntity = FeatureEntity.build("moi", "session", list.get(1).getFeature(), null);
        updatedEntity.setLastUpdate(OffsetDateTime.now().plusSeconds(1));

        createdEntity.setUrn(list.get(0).getFeature().getUrn());
        updatedEntity.setUrn(list.get(1).getFeature().getUrn());

        // to skip creation and update process
        this.featureRepo.save(createdEntity);
        this.featureRepo.save(updatedEntity);

        this.publisher.publish(NotificationRequestEvent.build(createdEntity.getUrn(), PriorityLevel.LOW));
        this.publisher.publish(NotificationRequestEvent.build(updatedEntity.getUrn(), PriorityLevel.LOW));

        this.waitRequest(notificationRepo, 2, 30000);
        assertEquals(2, notificationService.scheduleRequests());
        this.waitRequest(notificationRepo, 0, 30000);

        Mockito.verify(publisher).publish(recordsCaptor.capture());
        // the first publish message to be intercepted must be the creation of createdEntity
        assertEquals("CREATION", recordsCaptor.getValue().get(0).getAction());
        assertEquals(gson.toJson(createdEntity.getFeature()), recordsCaptor.getValue().get(0).getElement().toString());
        // the second message is the update of updatedEntity
        assertEquals("UPDATE", recordsCaptor.getValue().get(1).getAction());
        assertEquals(gson.toJson(updatedEntity.getFeature()), recordsCaptor.getValue().get(1).getElement().toString());

    }
}
