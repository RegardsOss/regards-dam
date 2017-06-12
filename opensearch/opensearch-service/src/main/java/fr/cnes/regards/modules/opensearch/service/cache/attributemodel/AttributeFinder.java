/*
 * LICENSE_PLACEHOLDER
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

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder#findByName(java.lang.String)
     */
    @Override
    public AttributeModel findByName(String pName) throws OpenSearchUnknownParameter {

        // Activate cache refresh if necessary
        attributeModelCache.getAttributeModels(runtimeTenantResolver.getTenant());

        // Check queryable static properties
        return attributeModelCache.findByName(pName);
    }

}
