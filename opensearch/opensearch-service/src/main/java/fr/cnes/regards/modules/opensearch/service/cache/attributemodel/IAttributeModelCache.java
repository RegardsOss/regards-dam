/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.cache.attributemodel;

import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

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
    AttributeModel findByName(String pName) throws EntityNotFoundException;

}