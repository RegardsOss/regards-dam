/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.opensearch.service.cache.attributemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.hateoas.HateoasUtils;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.dam.client.models.IAttributeModelClient;
import fr.cnes.regards.modules.dam.domain.entities.StaticProperties;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.domain.models.event.AttributeModelCreated;
import fr.cnes.regards.modules.dam.domain.models.event.AttributeModelDeleted;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;

/**
 * In this implementation, we choose to repopulate (and not only evict) the cache for a tenant in response to "create"
 * and "delete" events.<br>
 * This way the cache "anticipates" by repopulating immediately instead of waiting for the next user call.
 * @author Xavier-Alexandre Brochard
 * @author Marc Sordi
 */
@Service
@MultitenantTransactional
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
     * Store dynamic and static properties by tenant. <br/>
     * Allows intelligent guess of attribute from a partial or complete JSON path preventing potential conflicts!<br/>
     *
     * TODO explain
     */
    private final Map<String, Map<String, AttributeModel>> propertyMap = new HashMap<>();

    /**
     * Creates a new instance of the service with passed services/repos
     * @param attributeModelClient Service returning the list of attribute models
     * @param subscriber the AMQP events subscriber
     * @param runtimeTenantResolver the runtime tenant resolver
     */
    public AttributeModelCache(IAttributeModelClient attributeModelClient, ISubscriber subscriber,
            IRuntimeTenantResolver runtimeTenantResolver) {
        super();
        this.attributeModelClient = attributeModelClient;
        this.subscriber = subscriber;
        this.runtimeTenantResolver = runtimeTenantResolver;
    }

    @Override
    // no transaction needed here, it is call out of context with no acces to DB and fails prevent the application from
    // booting
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onApplicationEvent(ApplicationReadyEvent event) {
        subscriber.subscribeTo(AttributeModelCreated.class, new CreatedHandler());
        subscriber.subscribeTo(AttributeModelDeleted.class, new DeletedHandler());
    }

    /**
     * Initialize queryable static properties
     */
    public final void initStaticProperties(Map<String, AttributeModel> tenantMap) {

        // Unique identifier
        tenantMap.put(StaticProperties.FEATURE_ID, AttributeModelBuilder
                .build(StaticProperties.FEATURE_ID, AttributeType.STRING, null).isStatic().get());

        // SIP identifier alias provider identifier
        tenantMap.put(StaticProperties.FEATURE_PROVIDER_ID, AttributeModelBuilder
                .build(StaticProperties.FEATURE_PROVIDER_ID, AttributeType.STRING, null).isStatic().get());

        // Required label for minimal display purpose
        tenantMap.put(StaticProperties.FEATURE_LABEL, AttributeModelBuilder
                .build(StaticProperties.FEATURE_LABEL, AttributeType.STRING, null).isStatic().get());

        // Related model name
        tenantMap.put(StaticProperties.FEATURE_MODEL, AttributeModelBuilder
                .build(StaticProperties.FEATURE_MODEL, AttributeType.STRING, null).isStatic().get());

        // // Geometry
        // tenantMap.put(StaticProperties.FEATURE_GEOMETRY, AttributeModelBuilder
        // .build(StaticProperties.FEATURE_GEOMETRY, AttributeType.STRING, null).isStatic().get());

        // Tags
        tenantMap.put(StaticProperties.FEATURE_TAGS, AttributeModelBuilder
                .build(StaticProperties.FEATURE_TAGS, AttributeType.STRING, null).isStatic().get());

        // Allows to filter on dataset model id when searching for dataobjects
        tenantMap.put(StaticProperties.DATASET_MODEL_IDS, AttributeModelBuilder
                .build(StaticProperties.DATASET_MODEL_IDS, AttributeType.LONG, null).isInternal().get());

    }

    @Override
    public List<AttributeModel> getAttributeModels(String tenant) {
        return doGetAttributeModels(tenant);
    }

    @Override
    public List<AttributeModel> getAttributeModelsThenCache(String tenant) {
        return doGetAttributeModels(tenant);
    }

    /**
     * Use the feign client to retrieve all attribute models.<br>
     * The method is private because it is not expected to be used directly, but via its cached facade
     * "getAttributeModels" method.
     * @return the list of user's access groups
     */
    private List<AttributeModel> doGetAttributeModels(String tenant) {

        // Enable system call as follow (thread safe action)
        FeignSecurityManager.asSystem();

        // Retrieve the list of attribute models
        ResponseEntity<List<Resource<AttributeModel>>> response = attributeModelClient.getAttributes(null, null);
        List<AttributeModel> attModels = new ArrayList<>();
        if (response != null) {
            attModels = HateoasUtils.unwrapCollection(response.getBody());
        }

        // Build or rebuild the map
        Map<String, AttributeModel> tenantMap = new HashMap<>();
        // Add static properties
        initStaticProperties(tenantMap);
        // Reference tenant map (override current if any)
        propertyMap.put(tenant, tenantMap);

        // Conflictual dynamic keys to be removed
        List<String> conflictualKeys = new ArrayList<>();

        // Build intelligent map preventing conflicts
        for (AttributeModel attModel : attModels) {

            // - Add mapping between short property name and attribute if no conflict detected
            String key = attModel.getName();
            if (!tenantMap.containsKey(key) && !conflictualKeys.contains(key)) {
                // Bind short property name to attribute
                tenantMap.put(key, attModel);
            } else {
                // Conflictual dynamic property detected
                if (!conflictualKeys.contains(key)) {
                    // It not yet detected
                    if (tenantMap.get(key).isDynamic()) {
                        conflictualKeys.add(key);
                        tenantMap.remove(key);
                    }
                }
            }

            // - Add mapping between fragment qualified property and attribute
            if (attModel.hasFragment()) {
                String fragment = attModel.getFragment().getName();
                // Prevent conflicts with static properties
                if (!StaticProperties.FEATURES_STATICS.contains(fragment)) {
                    // Bind fragment qualified property name to attribute
                    tenantMap.put(attModel.buildJsonPath(""), attModel);
                }
            }

            // - Add mapping between fully qualified property and attribute
            tenantMap.put(attModel.buildJsonPath(StaticProperties.FEATURE_PROPERTIES), attModel);
        }

        return attModels;
    }

    @Override
    public AttributeModel findByName(String name) throws OpenSearchUnknownParameter {

        // Check dynamic properties
        String tenant = runtimeTenantResolver.getTenant();
        Map<String, AttributeModel> tenantMap = propertyMap.get(tenant);

        if (tenantMap == null) {
            String errorMessage = String.format("No property found for tenant %s. Unknown parameter %s", tenant, name);
            LOGGER.error(errorMessage);
            throw new OpenSearchUnknownParameter(errorMessage);
        }

        AttributeModel attModel = tenantMap.get(name);

        if (attModel == null) {
            String errorMessage = String.format("Unknown parameter %s for tenant %s", name, tenant);
            LOGGER.error(errorMessage);
            throw new OpenSearchUnknownParameter(errorMessage);
        }

        return attModel;
    }

    @Override
    public String findName(AttributeModel attribute) {
        // Check dynamic properties
        String name = attribute.getJsonPath();

        // Only dynamic attributes can have a reduce name path
        if (!attribute.isDynamic() && (attribute.getId() != null)) {
            return name;
        }

        String tenant = runtimeTenantResolver.getTenant();
        Map<String, AttributeModel> tenantMap = propertyMap.get(tenant);

        for (Entry<String, AttributeModel> entry : tenantMap.entrySet()) {
            AttributeModel att = entry.getValue();
            if (att.isDynamic() && (att.getId() != null) && att.getId().equals(attribute.getId())) {
                if (entry.getKey().length() < name.length()) {
                    name = entry.getKey();
                }
            }
        }

        return name;

    }

    /**
     * Handle {@link AttributeModel} creation
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
     * @author Xavier-Alexandre Brochard
     */
    private class DeletedHandler implements IHandler<AttributeModelDeleted> {

        @Override
        public void handle(TenantWrapper<AttributeModelDeleted> pWrapper) {
            LOGGER.info("New attribute model deleted, refreshing the cache", pWrapper.getContent().getAttributeName());
            getAttributeModelsThenCache(pWrapper.getTenant());
        }
    }

    public Map<String, Map<String, AttributeModel>> getPropertyMap() {
        return propertyMap;
    }
}
