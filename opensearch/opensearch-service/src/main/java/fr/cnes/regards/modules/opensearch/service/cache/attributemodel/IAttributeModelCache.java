/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;

/**
 * Provider for {@link AttributeModel}s with caching facilities.
 *
 * @author Xavier-Alexandre Brochard
 */
public interface IAttributeModelCache {

    /**
     * The call will first check the cache "attributemodels" before actually invoking the method and then caching the
     * result.
     * @param pTenant the tenant. Only here for auto-building a multitenant cache, and might not be used in the implementation.
     * @return the list of attribute models
     */
    @Cacheable(value = "attributemodels")
    List<AttributeModel> getAttributeModels(String pTenant);

    /**
     * The call will first check the cache "attributemodels" before actually invoking the method and then caching the
     * result.
     * @param pTenant the tenant. Only here for auto-building a multitenant cache, and might not be used in the implementation.
     * @return the list of attribute models
     */
    @CachePut(value = "attributemodels")
    List<AttributeModel> getAttributeModelsThenCache(String pTenant);

    /**
    * Return the {@link AttributeModel} of passed name. It will search in a cached list for performance.
    * @param pName the attribute model name
    * @return the attribute model
    * @throws EntityNotFoundException when no attribute model with passed name could be found
    */
    AttributeModel findByName(String pName) throws OpenSearchUnknownParameter;

}