/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.cache.attributemodel;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.hateoas.HateoasUtils;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.models.client.IAttributeModelClient;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.event.AttributeModelCreated;
import fr.cnes.regards.modules.models.domain.event.AttributeModelDeleted;

/**
 * In this implementation, we choose to repopulate (and not only evict) the cache for a tenant in response to "create" and "delete" events.<br>
 * This way the cache "anticipates" by repopulating immediately instead of waiting for the next user call.
 *
 * @author Xavier-Alexandre Brochard
 */
@Service
public class AttributeModelCache implements IAttributeModelCache, ApplicationListener<ApplicationReadyEvent> {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeModelCache.class);

    /**
     * Feign client for rs-dam {@link AttributeModel} controller. Autowired by Spring.
     */
    private final IAttributeModelClient attributeModelClient;

    /**
     * AMPQ messages subscriber. Autowired by Spring.
     */
    private final ISubscriber subscriber;

    /**
     * Retrieve the current tenant at runtime. Autowired by Spring.
     */
    private final IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * Creates a new instance of the service with passed services/repos
     *
     * @param attributeModelClient Service returning the list of attribute models
     * @param pSubscriber the AMQP events subscriber
     * @param pRuntimeTenantResolver the runtime tenant resolver
     */
    public AttributeModelCache(IAttributeModelClient attributeModelClient, ISubscriber pSubscriber,
                               IRuntimeTenantResolver pRuntimeTenantResolver) {
        super();
        this.attributeModelClient = attributeModelClient;
        subscriber = pSubscriber;
        runtimeTenantResolver = pRuntimeTenantResolver;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        subscriber.subscribeTo(AttributeModelCreated.class, new CreatedHandler());
        subscriber.subscribeTo(AttributeModelDeleted.class, new DeletedHandler());
    }

    @Override
    public List<AttributeModel> getAttributeModels(String pTenant) {
        return doGetAttributeModels(pTenant);
    }

    @Override
    public List<AttributeModel> getAttributeModelsThenCache(String pTenant) {
        return doGetAttributeModels(pTenant);
    }

    /**
     * Use the feign client to retrieve all attribute models.<br>
     * The method is private because it is not expected to be used directly, but via its cached facade "getAttributeModels" method.
     * @return the list of user's access groups
     */
    private List<AttributeModel> doGetAttributeModels(String pTenant) {
        // Enable system call as follow (thread safe action)
        FeignSecurityManager.asSystem();

        // Force tenant
        runtimeTenantResolver.forceTenant(pTenant);

        // Retrieve the list of attribute models
        ResponseEntity<List<Resource<AttributeModel>>> respone = attributeModelClient.getAttributes(null, null);

        // Disable system call if necessary after client request(s)
        FeignSecurityManager.reset();

        return HateoasUtils.unwrapCollection(respone.getBody());
    }

    @Override
    public AttributeModel findByName(String pName) throws EntityNotFoundException {
        String tenant = runtimeTenantResolver.getTenant();
        return getAttributeModels(tenant).stream().filter(el -> el.getName().equals(pName)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(pName, AttributeModel.class));
    }

    /**
     * Handle {@link AttributeModel} creation
     *
     * @author Xavier-Alexandre Brochard
     */
    private class CreatedHandler implements IHandler<AttributeModelCreated> {

        @Override
        public void handle(TenantWrapper<AttributeModelCreated> pWrapper) {
            LOGGER.info("New attribute model created, refreshing the cache", pWrapper.getContent().getAttributeName());
            getAttributeModelsThenCache(pWrapper.getTenant());
        }
    }

    /**
     * Handle {@link AttributeModel} deletion
     *
     * @author Xavier-Alexandre Brochard
     */
    private class DeletedHandler implements IHandler<AttributeModelDeleted> {

        @Override
        public void handle(TenantWrapper<AttributeModelDeleted> pWrapper) {
            LOGGER.info("New attribute model deleted, refreshing the cache", pWrapper.getContent().getAttributeName());
            getAttributeModelsThenCache(pWrapper.getTenant());
        }
    }

}
