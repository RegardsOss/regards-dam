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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.configuration.IAmqpAdmin;
import fr.cnes.regards.framework.amqp.configuration.IRabbitVirtualHostAdmin;
import fr.cnes.regards.framework.amqp.converter.JsonMessageConverters;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.amqp.event.JsonMessageConverter;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.framework.amqp.event.WorkerMode;
import fr.cnes.regards.framework.amqp.single.SingleVhostPublisher;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;

/**
 * {@link TestPublisher} uses {@link IRuntimeTenantResolver} to resolve current thread tenant to publish an event in the
 * multitenant context.
 * @author svissier
 * @author lmieulet
 * @author Marc Sordi
 */
public class TestPublisher extends SingleVhostPublisher implements IPublisher {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(TestPublisher.class);

    @Autowired
    private MessageConverter converter;

    public TestPublisher(RabbitTemplate rabbitTemplate, IAmqpAdmin amqpAdmin,
            IRabbitVirtualHostAdmin rabbitVirtualHostAdmin, IRuntimeTenantResolver threadTenantResolver) {
        super(rabbitTemplate, amqpAdmin, rabbitVirtualHostAdmin, threadTenantResolver);
    }

    @Override
    protected <T> void publish(final T event, final WorkerMode workerMode, final Target target, final int priority,
            boolean purgeQueue) {
        TenantWrapper<T> wrapper = new TenantWrapper<T>();
        wrapper.setContent(event);
        MessageProperties messages = new MessageProperties();
        messages.setHeader(JsonMessageConverters.TENANT_HEADER, JsonMessageConverter.GSON);
        generateMessageDocumentation(event, wrapper, messages);
        String tenant = resolveTenant();
        if (tenant != null) {
            publish(tenant, resolveVirtualHost(tenant), event, workerMode, target, priority, purgeQueue);
        } else {
            String errorMessage = String.format("Unable to publish event %s cause no tenant found.", event.getClass());
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private <T> void generateMessageDocumentation(final T event, TenantWrapper<T> wrapper, MessageProperties messages) {
        File file = new File(String.format("target/%s.md", event.getClass().getSimpleName()));
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.write(new String(converter.toMessage(wrapper, messages).getBody()));
            writer.close();
            generateAttributeDocumentation(event);
        } catch (IOException e) {
            LOGGER.error("Error during file Creation : {} ", e);
        }
    }

    /**
     * @param event
     */
    private <T> void generateAttributeDocumentation(final T event) {
        File file = new File(String.format("target/%s-attributes.md", event.getClass().getSimpleName()));
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            //            writer.write(new String(converter.toMessage(wrapper, messages).getBody()));
            writer.close();
        } catch (IOException e) {
            LOGGER.error("Error during file Creation : {} ", e);
        }
    }
}
