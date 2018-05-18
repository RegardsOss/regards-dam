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

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;

/**
 * Implement {@link IAttributeFinder} using {@link AttributeModelCache} properly through proxyfied cacheable class.
 * @author Marc Sordi
 *
 */
@Service
public class AttributeFinder implements IAttributeFinder {

    /**
     * Provides the {@link AttributeModel}s with caching facilities.
     */
    private final IAttributeModelCache attributeModelCache;

    /**
     * Retrieve the current tenant at runtime. Autowired by Spring.
     */
    private final IRuntimeTenantResolver runtimeTenantResolver;

    public AttributeFinder(IRuntimeTenantResolver runtimeTenantResolver, IAttributeModelCache attributeModelCache) {
        this.runtimeTenantResolver = runtimeTenantResolver;
        this.attributeModelCache = attributeModelCache;
    }

    @Override
    public AttributeModel findByName(String name) throws OpenSearchUnknownParameter {

        // Activate cache refresh if necessary
        attributeModelCache.getAttributeModels(runtimeTenantResolver.getTenant());

        // Check queryable static properties
        return attributeModelCache.findByName(name);
    }

}
