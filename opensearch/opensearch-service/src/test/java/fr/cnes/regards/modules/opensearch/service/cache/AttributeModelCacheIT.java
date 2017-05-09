/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.cache;

import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Lists;

import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeModelCache;

/**
 * TestUriCompBuilder verifying {@link IAttributeModelCache} caching facilities.<br>
 * Widely inspired by Oliver Gierke (lead dev. of Spring Data project) example below.
 *
 * For some reason, Mockito fails when mocking the cache, and tells taht the method verify(mock) is not used correctyl.
 * Which is wrong, it is used correctly.
 *
 * @see http://stackoverflow.com/questions/24221569/how-to-test-springs-declarative-caching-support-on-spring-data-repositories
 * @author Xavier-Alexandre Brochard
 */
@Ignore
@RunWith(SpringRunner.class)
public class AttributeModelCacheIT {

    @Configuration
    @EnableCaching
    static class Config {

        // Simulating the caching configuration.
        // Actually, Spring auto-configures a suitable CacheManager according to the implementation
        // as long as the caching support is enabled via the @EnableCaching annotation.
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("attributemodels");
        }

        @Bean
        IAttributeModelCache cache() {
            return Mockito.mock(IAttributeModelCache.class);
        }

    }

    /**
     * Spring cache manager
     */
    @Autowired
    private CacheManager manager;

    /**
     * Cache under test
     */
    @Autowired
    private IAttributeModelCache cache;

    @After
    public void validate() {
        Mockito.validateMockitoUsage();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getAttributeModelsShouldBeCached() {
        // Define the current tenant
        String TENANT = "tenant";

        // Set up the mock to return *different* objects for the first and second call
        List<AttributeModel> first = Lists.newArrayList();
        List<AttributeModel> second = Lists.newArrayList();
        Mockito.when(cache.getAttributeModels(TENANT)).thenReturn(first, second);

        // First invocation returns object returned by the method
        List<AttributeModel> result = cache.getAttributeModels(TENANT);
        Assert.assertThat(result, CoreMatchers.is(first));

        // Second invocation should return cached value, *not* second (as set up above)
        result = cache.getAttributeModels(TENANT);
        Assert.assertThat(result, CoreMatchers.is(first));

        // Verify cache method was invoked once
        Mockito.verify(cache).getAttributeModels(Mockito.anyString());
        Assert.assertThat(manager.getCache("attributemodels").get(TENANT),
                          CoreMatchers.is(CoreMatchers.notNullValue()));

        // Third invocation triggers the second invocation of the repo method (because the stub was configured so)
        result = cache.getAttributeModels(TENANT);
        Assert.assertThat(result, CoreMatchers.is(second));
    }

    @Test
    @Purpose("Check that the cache is multitenant")
    public void checkThatTheCacheIsMultiTenant() {
        // Define two tenants
        String TENANT_A = "tenantA";
        String TENANT_B = "tenantB";

        // Populate the cache
        cache.getAttributeModels(TENANT_A);
        cache.getAttributeModels(TENANT_B);

        // Verify we have an entry in the cache for each tenant
        Assert.assertThat(manager.getCache("attributemodels").get(TENANT_A),
                          CoreMatchers.is(CoreMatchers.notNullValue()));
        Assert.assertThat(manager.getCache("attributemodels").get(TENANT_B),
                          CoreMatchers.is(CoreMatchers.notNullValue()));
    }
}