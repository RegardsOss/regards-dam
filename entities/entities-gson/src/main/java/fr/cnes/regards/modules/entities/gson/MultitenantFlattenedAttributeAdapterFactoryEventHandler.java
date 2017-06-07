/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.gson;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.event.AttributeModelCreated;
import fr.cnes.regards.modules.models.domain.event.AttributeModelDeleted;
import fr.cnes.regards.modules.models.domain.event.FragmentDeletedEvent;

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

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent pEvent) {
        subscriber.subscribeTo(AttributeModelCreated.class, new RegisterHandler());
        subscriber.subscribeTo(AttributeModelDeleted.class, new UnregisterHandler());
        subscriber.subscribeTo(FragmentDeletedEvent.class, new UnregisterFragmentHandler());
        // Retrieve all tenants
        for (final String tenant : tenantResolver.getAllActiveTenants()) {
            // Register for tenant
            final List<AttributeModel> atts = attributeHelper.getAllAttributes(tenant);
            LOGGER.debug("Registering allready configured attributes and fragments");
            // Use factory algorithm
            factory.registerAttributes(tenant, atts);
        }
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
