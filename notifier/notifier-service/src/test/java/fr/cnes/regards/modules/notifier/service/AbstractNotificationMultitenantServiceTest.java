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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpIOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;

import fr.cnes.regards.framework.amqp.configuration.AmqpConstants;
import fr.cnes.regards.framework.amqp.configuration.IAmqpAdmin;
import fr.cnes.regards.framework.amqp.configuration.IRabbitVirtualHostAdmin;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.model.client.IModelAttrAssocClient;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.model.service.exception.ImportException;
import fr.cnes.regards.modules.model.service.xml.IComputationPluginService;
import fr.cnes.regards.modules.model.service.xml.XmlImportHelper;
import fr.cnes.regards.modules.notifier.dao.INotificationActionRepository;
import fr.cnes.regards.modules.notifier.dao.IRecipientErrorRepository;
import fr.cnes.regards.modules.notifier.dao.IRecipientRepository;
import fr.cnes.regards.modules.notifier.dao.IRuleRepository;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender10;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender2;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender3;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender4;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender5;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender6;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender7;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender8;
import fr.cnes.regards.modules.notifier.plugin.RecipientSender9;
import fr.cnes.regards.modules.notifier.service.flow.NotificationActionEventHandler;

public abstract class AbstractNotificationMultitenantServiceTest extends AbstractMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNotificationMultitenantServiceTest.class);

    private static final String RESOURCE_PATH = "fr/cnes/regards/modules/notifier/service/";

    @Autowired
    protected IRuleRepository ruleRepo;

    @Autowired
    protected IRecipientRepository recipientRepo;

    @Autowired
    protected IPluginConfigurationRepository pluginConfRepo;

    @Autowired
    protected INotificationActionRepository notificationRepo;

    @Autowired
    protected IRecipientErrorRepository recipientErrorRepo;

    @Autowired
    protected NotificationRuleService notificationService;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired(required = false)
    private IAmqpAdmin amqpAdmin;

    @Autowired(required = false)
    private IRabbitVirtualHostAdmin vhostAdmin;

    @Autowired
    protected MultitenantFlattenedAttributeAdapterFactory factory;

    // Mock for test purpose
    @Autowired
    protected IComputationPluginService cps;

    @Autowired
    protected IModelAttrAssocClient modelAttrAssocClientMock;

    @Before
    public void before() throws InterruptedException {
        this.notificationService.cleanTenantCache(runtimeTenantResolver.getTenant());
        this.recipientRepo.deleteAll();
        this.ruleRepo.deleteAll();
        this.pluginConfRepo.deleteAll();
        this.notificationRepo.deleteAll();
        this.recipientErrorRepo.deleteAll();
        simulateApplicationReadyEvent();
    }

    /**
     * Internal method to clean AMQP queues, if actives
     */
    public void cleanAMQPQueues(Class<? extends IHandler<?>> handler, Target target) {
        if (vhostAdmin != null) {
            // Re-set tenant because above simulation clear it!

            // Purge event queue
            try {
                vhostAdmin.bind(AmqpConstants.AMQP_MULTITENANT_MANAGER);
                amqpAdmin.purgeQueue(amqpAdmin.getSubscriptionQueueName(handler, target), false);
            } catch (AmqpIOException e) {
                //todo
            } finally {
                vhostAdmin.unbind();
            }
        }
    }

    /**
     * Mock model client importing model specified by its filename
     * @param filename model filename found using {@link Class#getResourceAsStream(String)}
     * @return mocked model name
     */
    public String mockModelClient(String filename, IComputationPluginService cps,
            MultitenantFlattenedAttributeAdapterFactory factory, String tenant,
            IModelAttrAssocClient modelAttrAssocClientMock) {

        try (InputStream input = new ClassPathResource(RESOURCE_PATH + filename).getInputStream()) {
            // Import model
            Iterable<ModelAttrAssoc> assocs = XmlImportHelper.importModel(input, cps);

            // Translate to resources and attribute models and extract model name
            String modelName = null;
            List<AttributeModel> atts = new ArrayList<>();
            List<Resource<ModelAttrAssoc>> resources = new ArrayList<>();
            for (ModelAttrAssoc assoc : assocs) {
                atts.add(assoc.getAttribute());
                resources.add(new Resource<ModelAttrAssoc>(assoc));
                if (modelName == null) {
                    modelName = assoc.getModel().getName();
                }
            }

            // Property factory registration
            factory.registerAttributes(tenant, atts);

            // Mock client
            Mockito.when(modelAttrAssocClientMock.getModelAttrAssocs(modelName))
                    .thenReturn(ResponseEntity.ok(resources));

            return modelName;
        } catch (IOException | ImportException e) {
            String errorMessage = "Cannot import model";
            LOGGER.debug(errorMessage);
            throw new AssertionError(errorMessage);
        }
    }

    public String mockModelClient(String filename) {
        return mockModelClient(filename, cps, factory, getDefaultTenant(), modelAttrAssocClientMock);
    }

    public void waitNotificationRegistry(int expectedNumber, int timeout) throws InterruptedException {
        long notificationActionNumber = 0;
        int cpt = 0;
        do {
            notificationActionNumber = this.notificationRepo.count();
            cpt++;
            Thread.sleep(1000);
        } while ((notificationActionNumber != expectedNumber) && (cpt != timeout));

        if (notificationActionNumber != expectedNumber) {
            fail(String.format("Wrong notifications number in database after timeout expected %s got %s",
                               expectedNumber, notificationActionNumber));
        }
    }

    @After
    public void after() {
        cleanAMQPQueues(NotificationActionEventHandler.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender2.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender3.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender4.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender5.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender6.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender7.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender8.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender9.class, Target.ONE_PER_MICROSERVICE_TYPE);
        cleanAMQPQueues(RecipientSender10.class, Target.ONE_PER_MICROSERVICE_TYPE);
    }
}
