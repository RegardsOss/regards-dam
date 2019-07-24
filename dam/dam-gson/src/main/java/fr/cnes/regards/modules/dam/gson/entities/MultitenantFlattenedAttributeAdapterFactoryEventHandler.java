/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.gson.entities;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.dam.domain.models.attributes.Fragment;
import fr.cnes.regards.modules.dam.domain.models.event.AttributeModelCreated;
import fr.cnes.regards.modules.dam.domain.models.event.AttributeModelDeleted;
import fr.cnes.regards.modules.dam.domain.models.event.FragmentDeletedEvent;

/**
 * Handler to initialize subTypes for MultitenantFlattenedAttributeAdapterFactory after ApplicationReadyEvent sent.
 *
 * @author Sébastien Binda
 */
@Component
public class MultitenantFlattenedAttributeAdapterFactoryEventHandler
        implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MultitenantFlattenedAttributeAdapterFactoryEventHandler.class);

    /**
     * AMQ Subscriber
     */
    @Autowired
    private ISubscriber subscriber;

    /**
     * Tenant resolver
     */
    @Autowired
    private ITenantResolver tenantResolver;

    /**
     * Helper class to initialize factory based on stored {@link AttributeModel}
     */
    @Autowired
    private IAttributeHelper attributeHelper;

    /**
     * Factory to work with
     */
    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory factory;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent pEvent) {
        subscriber.subscribeTo(AttributeModelCreated.class, new RegisterHandler());
        subscriber.subscribeTo(AttributeModelDeleted.class, new UnregisterHandler());
        subscriber.subscribeTo(FragmentDeletedEvent.class, new UnregisterFragmentHandler());
        // Retrieve all tenants
        for (final String tenant : tenantResolver.getAllActiveTenants()) {
            // Register for tenant
            final List<AttributeModel> atts = attributeHelper.getAllAttributes(tenant);
            LOGGER.info("Registering already configured attributes and fragments");
            // Use factory algorithm
            factory.registerAttributes(tenant, atts);
            LOGGER.info("Registering attributes for tenant {} done", tenant);
        }
        applicationEventPublisher.publishEvent(new DamGsonReadyEvent(this));
    }

    /**
     * Handle {@link AttributeModel} creation
     *
     * @author Marc Sordi
     */
    private class RegisterHandler implements IHandler<AttributeModelCreated> {

        @Override
        public void handle(final TenantWrapper<AttributeModelCreated> pWrapper) {
            final AttributeModelCreated amc = pWrapper.getContent();

            String tenant = pWrapper.getTenant();
            Fragment fragment = new Fragment();
            fragment.setName(amc.getFragmentName());
            AttributeModel attributeModel = AttributeModelBuilder
                    .build(amc.getAttributeName(), amc.getAttributeType(), null).fragment(fragment).get();

            // Use factory algorithm
            factory.registerAttribute(tenant, attributeModel);
        }
    }

    /**
     * Handle {@link AttributeModel} deletion
     *
     * @author Marc Sordi
     */
    private class UnregisterHandler implements IHandler<AttributeModelDeleted> {

        @Override
        public void handle(final TenantWrapper<AttributeModelDeleted> pWrapper) {
            AttributeModelDeleted amd = pWrapper.getContent();

            String tenant = pWrapper.getTenant();
            Fragment fragment = new Fragment();
            fragment.setName(amd.getFragmentName());
            AttributeModel attributeModel = AttributeModelBuilder
                    .build(amd.getAttributeName(), amd.getAttributeType(), null).fragment(fragment).get();

            // Use factory algorithm
            factory.unregisterAttribute(tenant, attributeModel);
        }
    }

    /**
     * Handle {@link Fragment} deletion
     *
     * @author Marc Sordi
     *
     */
    private class UnregisterFragmentHandler implements IHandler<FragmentDeletedEvent> {

        @Override
        public void handle(final TenantWrapper<FragmentDeletedEvent> pWrapper) {
            String tenant = pWrapper.getTenant();
            FragmentDeletedEvent fragmentDeleted = pWrapper.getContent();

            Fragment fragment = new Fragment();
            fragment.setName(fragmentDeleted.getFragmentName());

            // Use factory algorithm
            factory.unregisterFragment(tenant, fragment);
        }
    }

}
