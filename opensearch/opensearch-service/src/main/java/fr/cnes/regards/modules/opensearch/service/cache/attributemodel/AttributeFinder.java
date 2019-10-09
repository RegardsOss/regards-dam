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
package fr.cnes.regards.modules.opensearch.service.cache.attributemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.hateoas.HateoasUtils;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.dam.domain.entities.StaticProperties;
import fr.cnes.regards.modules.model.client.IAttributeModelClient;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.model.domain.event.AttributeModelCreated;
import fr.cnes.regards.modules.model.domain.event.AttributeModelDeleted;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;

/**
 * Implement {@link IAttributeFinder}.
 * @author Marc Sordi
 *
 */
@Service
public class AttributeFinder implements IAttributeFinder, ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeFinder.class);

    /**
     * Feign client for rs-dam {@link AttributeModel} controller. Autowired by Spring.
     */
    private final IAttributeModelClient attributeModelClient;

    private final ISubscriber subscriber;

    private final IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * Store dynamic and static properties by tenant. <br/>
     * Allows intelligent guess of attribute from a partial or complete JSON path preventing potential conflicts!<br/>
     */
    private final Map<String, Map<String, AttributeModel>> propertyMap = new HashMap<>();

    /**
     * Store dynamic and static properties by tenant and type for full text search
     */
    private final Map<String, Multimap<PropertyType, AttributeModel>> typedPropertyMap = new HashMap<>();

    public AttributeFinder(IAttributeModelClient attributeModelClient, ISubscriber subscriber,
            IRuntimeTenantResolver runtimeTenantResolver) {
        this.attributeModelClient = attributeModelClient;
        this.subscriber = subscriber;
        this.runtimeTenantResolver = runtimeTenantResolver;
    }

    @Override
    public AttributeModel findByName(String name) throws OpenSearchUnknownParameter {

        AttributeModel attModel = getTenantMap().get(name);

        if (attModel == null) {
            String errorMessage = String.format("Unknown parameter %s for tenant %s", name,
                                                runtimeTenantResolver.getTenant());
            LOGGER.error(errorMessage);
            throw new OpenSearchUnknownParameter(errorMessage);
        }

        return attModel;
    }

    @Override
    public Set<AttributeModel> findByType(PropertyType type) throws OpenSearchUnknownParameter {

        Collection<AttributeModel> ppties = getTenantTypedMap().get(type);
        if (ppties == null) {
            String errorMessage = String.format("No parameter found with type %s for tenant %s", type,
                                                runtimeTenantResolver.getTenant());
            LOGGER.error(errorMessage);
            throw new OpenSearchUnknownParameter(errorMessage);
        }
        return new HashSet<>(ppties);
    }

    @Override
    public String findName(AttributeModel attribute) {
        // Check dynamic properties
        String name = attribute.getFullJsonPath();

        // Only dynamic attributes can have a reduce name path
        if (!attribute.isDynamic() && (attribute.getId() != null)) {
            return name;
        }

        for (Entry<String, AttributeModel> entry : getTenantMap().entrySet()) {
            AttributeModel att = entry.getValue();
            if (att.isDynamic() && (att.getId() != null) && att.getId().equals(attribute.getId())) {
                if (entry.getKey().length() < name.length()) {
                    name = entry.getKey();
                }
            }
        }
        return name;
    }

    @Override
    public void refresh(String tenant) {
        computePropertyMap(tenant);
    }

    private Map<String, AttributeModel> getTenantMap() {
        String tenant = runtimeTenantResolver.getTenant();
        if (!propertyMap.containsKey(tenant)) {
            computePropertyMap(tenant);
        }
        return propertyMap.get(tenant);
    }

    private Multimap<PropertyType, AttributeModel> getTenantTypedMap() {
        String tenant = runtimeTenantResolver.getTenant();
        if (!typedPropertyMap.containsKey(tenant)) {
            computePropertyMap(tenant);
        }
        return typedPropertyMap.get(tenant);
    }

    /**
     * Initialize queryable static properties
     */
    private void initStaticProperties(Map<String, AttributeModel> tenantMap,
            Multimap<PropertyType, AttributeModel> tenantTypeMap) {

        // Unique identifier
        tenantMap.put(StaticProperties.FEATURE_ID, AttributeModelBuilder
                .build(StaticProperties.FEATURE_ID, PropertyType.STRING, null).isStatic().get());

        // SIP identifier alias provider identifier
        tenantMap.put(StaticProperties.FEATURE_PROVIDER_ID, AttributeModelBuilder
                .build(StaticProperties.FEATURE_PROVIDER_ID, PropertyType.STRING, null).isStatic().get());

        // Required label for minimal display purpose
        tenantMap.put(StaticProperties.FEATURE_LABEL, AttributeModelBuilder
                .build(StaticProperties.FEATURE_LABEL, PropertyType.STRING, null).isStatic().get());

        // Related model name
        tenantMap.put(StaticProperties.FEATURE_MODEL, AttributeModelBuilder
                .build(StaticProperties.FEATURE_MODEL, PropertyType.STRING, null).isStatic().get());

        // // Geometry
        // tenantMap.put(StaticProperties.FEATURE_GEOMETRY, AttributeModelBuilder
        // .build(StaticProperties.FEATURE_GEOMETRY, PropertyType.STRING, null).isStatic().get());

        // Tags
        tenantMap.put(StaticProperties.FEATURE_TAGS, AttributeModelBuilder
                .build(StaticProperties.FEATURE_TAGS, PropertyType.STRING, null).isStatic().get());

        // Allows to filter on dataset model id when searching for dataobjects
        tenantMap.put(StaticProperties.DATASET_MODEL_NAMES, AttributeModelBuilder
                .build(StaticProperties.DATASET_MODEL_NAMES, AttributeType.STRING, null).isInternal().get());

        // Register static properties by types
        tenantMap.values().forEach(attModel -> tenantTypeMap.put(attModel.getType(), attModel));
    }

    /**
     * Use the feign client to retrieve all attribute models.<br>
     * The method is private because it is not expected to be used directly, but via its cached facade
     * "getAttributeModels" method.
     */
    protected void computePropertyMap(String tenant) {

        // Enable system call as follow (thread safe action)
        FeignSecurityManager.asSystem();

        // Retrieve the list of attribute models
        ResponseEntity<List<Resource<AttributeModel>>> response = attributeModelClient.getAttributes(null, null);
        List<AttributeModel> attModels = new ArrayList<>();
        if (response != null) {
            attModels = HateoasUtils.unwrapCollection(response.getBody());
        }

        // Build or rebuild the maps
        Map<String, AttributeModel> tenantMap = new HashMap<>();
        Multimap<PropertyType, AttributeModel> tenantTypeMap = ArrayListMultimap.create();

        // Add static properties
        initStaticProperties(tenantMap, tenantTypeMap);

        // Reference tenant maps (override maybe)
        propertyMap.put(tenant, tenantMap);
        typedPropertyMap.put(tenant, tenantTypeMap);

        // Conflictual dynamic keys to be removed
        List<String> conflictualKeys = new ArrayList<>();

        // Build intelligent map preventing conflicts
        for (AttributeModel attModel : attModels) {

            // Register properties by types
            tenantTypeMap.put(attModel.getType(), attModel);

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
                    tenantMap.put(attModel.getJsonPathForNamespace(""), attModel);
                }
            }

            // - Add mapping between public json path and attribute model
            tenantMap.put(attModel.getJsonPathForNamespace(StaticProperties.FEATURE_PROPERTIES), attModel);

            // - Add mapping between fully qualified property and attribute
            tenantMap.put(attModel.getFullJsonPath(), attModel);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        subscriber.subscribeTo(AttributeModelCreated.class, new CreatedHandler());
        subscriber.subscribeTo(AttributeModelDeleted.class, new DeletedHandler());
    }

    /**
     * Handle {@link AttributeModel} creation
     * @author Xavier-Alexandre Brochard
     */
    private class CreatedHandler implements IHandler<AttributeModelCreated> {

        @Override
        public void handle(TenantWrapper<AttributeModelCreated> pWrapper) {
            try {
                runtimeTenantResolver.forceTenant(pWrapper.getTenant());
                LOGGER.info("New attribute model created, refreshing the cache",
                            pWrapper.getContent().getAttributeName());
                computePropertyMap(pWrapper.getTenant());
            } finally {
                runtimeTenantResolver.clearTenant();
            }
        }
    }

    /**
     * Handle {@link AttributeModel} deletion
     * @author Xavier-Alexandre Brochard
     */
    private class DeletedHandler implements IHandler<AttributeModelDeleted> {

        @Override
        public void handle(TenantWrapper<AttributeModelDeleted> pWrapper) {
            try {
                runtimeTenantResolver.forceTenant(pWrapper.getTenant());
                LOGGER.info("New attribute model deleted, refreshing the cache",
                            pWrapper.getContent().getAttributeName());
                computePropertyMap(pWrapper.getTenant());
            } finally {
                runtimeTenantResolver.clearTenant();
            }
        }
    }

    protected Map<String, Map<String, AttributeModel>> getPropertyMap() {
        return propertyMap;
    }

}
