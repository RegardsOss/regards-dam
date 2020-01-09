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

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.feature.dao.IFeatureReferenceRequestRepository;
import fr.cnes.regards.modules.feature.dto.FeatureSessionMetadata;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.StorageMetadata;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureReferenceRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

/**
 * @author kevin
 *
 */
@TestPropertySource(
        properties = { "spring.jpa.properties.hibernate.default_schema=feature_reference", "regards.amqp.enabled=true",
                "spring.task.scheduling.pool.size=2", "regards.feature.metrics.enabled=true" },
        locations = { "classpath:regards_perf.properties", "classpath:batch.properties",
                "classpath:metrics.properties" })
@ActiveProfiles(value = { "testAmqp" })
public class FeatureReferenceServiceIT extends AbstractFeatureMultitenantServiceTest {

    @Autowired
    private IFeatureReferenceRequestRepository referenceRequestRepo;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepo;

    @SpyBean
    private IPluginService pluginService;

    @Before
    public void setup() throws InterruptedException {
        this.referenceRequestRepo.deleteAll();
        this.pluginConfRepo.deleteAll();
        super.before();
    }

    @Test
    public void testProcessReference() {
        PluginConfiguration recipientPlugin = new PluginConfiguration();
        recipientPlugin.setBusinessId("testFeatureGeneration");
        recipientPlugin.setVersion("1.0.0");
        recipientPlugin.setLabel("test recipient");
        recipientPlugin.setPluginId("DefaultFeatureGenerator");
        recipientPlugin = this.pluginConfRepo.save(recipientPlugin);

        List<FeatureReferenceRequestEvent> eventsToPublish = new ArrayList<>();
        for (int i = 0; i < this.properties.getMaxBulkSize(); i++) {
            eventsToPublish.add(FeatureReferenceRequestEvent.build(FeatureSessionMetadata
                    .build("bibi", "session", PriorityLevel.NORMAL, new StorageMetadata[0]), "dtc " + i,
                                                                   "testFeatureGeneration"));
        }
        this.publisher.publish(eventsToPublish);

        this.waitRequest(this.featureCreationRequestRepo, this.properties.getMaxBulkSize(), 60000);
        this.waitRequest(this.featureRepo, this.properties.getMaxBulkSize(), 60000);

        assertEquals(0, this.referenceRequestRepo.count());
    }

    @Test
    public void testProcessReferenceWithErrors()
            throws ModuleException, NotAvailablePluginConfigurationException, InterruptedException {
        PluginConfiguration recipientPlugin = new PluginConfiguration();
        recipientPlugin.setBusinessId("testFeatureGeneration");
        recipientPlugin.setVersion("1.0.0");
        recipientPlugin.setLabel("test recipient");
        recipientPlugin.setPluginId("DefaultFeatureGenerator");
        recipientPlugin = this.pluginConfRepo.save(recipientPlugin);

        List<FeatureReferenceRequestEvent> eventsToPublish = new ArrayList<>();
        for (int i = 0; i < this.properties.getMaxBulkSize(); i++) {
            eventsToPublish.add(FeatureReferenceRequestEvent.build(FeatureSessionMetadata
                    .build("bibi", "session", PriorityLevel.NORMAL, new StorageMetadata[0]), "dtc " + i,
                                                                   "testFeatureGeneration"));
        }
        this.publisher.publish(eventsToPublish);

        Mockito.doThrow(new ModuleException("")).when(pluginService).getPlugin(Mockito.anyString());
        this.waitRequest(this.referenceRequestRepo, this.properties.getMaxBulkSize(), 60000);
        int cpt = 0;
        // we will expect that all feature reference remain in database with the error state
        do {
            Thread.sleep(1000);
            if (cpt == 60) {
                fail("Timeout");
            }
            cpt++;
        } while (!this.referenceRequestRepo.findAll().stream()
                .allMatch(request -> RequestState.ERROR.equals(request.getState())));
        // no feature creation should be in database
        assertEquals(0, this.featureCreationRequestRepo.count());

    }
}
