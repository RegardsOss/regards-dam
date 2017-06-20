/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.cache.attributemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.domain.StaticProperties;
import fr.cnes.regards.modules.models.client.IAttributeModelClient;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.event.AttributeModelCreated;
import fr.cnes.regards.modules.models.domain.event.AttributeModelDeleted;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;

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
     * Store static queryable properties by name
     */
    private final Map<String, AttributeModel> staticPropertyMap;

    /**
     * Store dynamic properties by tenant and name (including namespace (i.e.) fragment name)
     *
     * All dynamic properties is wrapped in <code>properties</code> namespace.
     *
     */
    private final Map<String, Map<String, AttributeModel>> dynamicPropertyMap;

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
        // Init static attributes
        staticPropertyMap = new HashMap<>();
        dynamicPropertyMap = new HashMap<>();
        initStaticAttributes();
    }

    /**
     * Initialize queryable static attributes
     */
    public void initStaticAttributes() {

        staticPropertyMap
                .put(StaticProperties.IP_ID,
                     AttributeModelBuilder.build(StaticProperties.IP_ID, AttributeType.STRING, null).isStatic().get());
        staticPropertyMap.put(StaticProperties.GEOMETRY, AttributeModelBuilder
                .build(StaticProperties.GEOMETRY, AttributeType.STRING, null).isStatic().get());
        staticPropertyMap
                .put(StaticProperties.LABEL,
                     AttributeModelBuilder.build(StaticProperties.LABEL, AttributeType.STRING, null).isStatic().get());
        staticPropertyMap.put(StaticProperties.MODEL_NAME, AttributeModelBuilder
                .build(StaticProperties.MODEL_NAME, AttributeType.STRING, null).isStatic().get());
        staticPropertyMap.put(StaticProperties.LAST_UPDATE, AttributeModelBuilder
                .build(StaticProperties.LAST_UPDATE, AttributeType.DATE_ISO8601, null).isStatic().get());
        staticPropertyMap.put(StaticProperties.CREATION_DATE, AttributeModelBuilder
                .build(StaticProperties.CREATION_DATE, AttributeType.DATE_ISO8601, null).isStatic().get());
        staticPropertyMap
                .put(StaticProperties.TAGS,
                     AttributeModelBuilder.build(StaticProperties.TAGS, AttributeType.STRING, null).isStatic().get());
        staticPropertyMap.put(StaticProperties.ENTITY_TYPE, AttributeModelBuilder
                .build(StaticProperties.ENTITY_TYPE, AttributeType.STRING, null).isStatic().get());
        staticPropertyMap.put(StaticProperties.DATASET_MODEL_IDS, AttributeModelBuilder
                .build(StaticProperties.DATASET_MODEL_IDS, AttributeType.STRING, null).isStatic().get());
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

        try {
            // Enable system call as follow (thread safe action)
            FeignSecurityManager.asSystem();
            // Force tenant
            // FIXME remove this unnecessary action / test will be impacted
            runtimeTenantResolver.forceTenant(pTenant);

            // Retrieve the list of attribute models
            ResponseEntity<List<Resource<AttributeModel>>> response = attributeModelClient.getAttributes(null, null);
            List<AttributeModel> attModels = new ArrayList<>();
            if (response != null) {
                attModels = HateoasUtils.unwrapCollection(response.getBody());
            }

            // Fill dynamic property mapping
            Map<String, AttributeModel> tenantMap = dynamicPropertyMap.get(pTenant);
            if (tenantMap == null) {
                tenantMap = new HashMap<>();
                dynamicPropertyMap.put(pTenant, tenantMap);
            }

            for (AttributeModel attModel : attModels) {
                // Tenant map will contain mapping between properties.[fragmentNameNotDefault.]<attributeName> and the attribute model
                tenantMap.put(attModel.buildJsonPath(StaticProperties.PROPERTIES), attModel);
            }

            return attModels;
        } finally {
            // Disable system call if necessary after client request(s)
            FeignSecurityManager.reset();
            runtimeTenantResolver.clearTenant();
        }

    }

    @Override
    public AttributeModel findByName(String pName) throws OpenSearchUnknownParameter {

        // Check queryable static properties
        AttributeModel attModel = staticPropertyMap.get(pName);
        if (attModel != null) {
            return attModel;
        }

        // Check dynamic properties
        String tenant = runtimeTenantResolver.getTenant();
        Map<String, AttributeModel> tenantMap = dynamicPropertyMap.get(tenant);

        if (tenantMap == null) {
            String errorMessage = String.format("No property found for tenant %s. Unknown parameter %s", tenant, pName);
            LOGGER.error(errorMessage);
            throw new OpenSearchUnknownParameter(errorMessage);
        }

        attModel = tenantMap.get(pName);

        if (attModel == null) {
            String errorMessage = String.format("Unknown parameter %s for tenant %s", pName, tenant);
            LOGGER.error(errorMessage);
            throw new OpenSearchUnknownParameter(errorMessage);
        }

        return attModel;
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
