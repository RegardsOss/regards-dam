/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.notifier.service.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.notifier.dao.IRuleRepository;
import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.reguards.modules.dto.type.NotificationType;

/**
 * Cache for {@link Rule}
 * @author Kevin Marchois
 *
 */
public abstract class AbstractCacheableRule {

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private IRuleRepository ruleRepo;

    /**
     * Rule cache is used to avoid useless database request as models rarely change!<br/>
     * tenant key -> model key / attributes val
     */
    private final Map<String, LoadingCache<String, Set<Rule>>> ruleCacheMap = new HashMap<>();

    /**
     * Get all enabled {@link Rule} for the current tenant if the cache is empty we will load it
     * with data from database
     * @return all enabled {@link Rule}
     * @throws ExecutionException
     */
    protected Set<Rule> getRules() throws ExecutionException {
        String tenant = runtimeTenantResolver.getTenant();
        LoadingCache<String, Set<Rule>> ruleCache = ruleCacheMap.get(tenant);
        if (ruleCacheMap.get(tenant) == null) {
            ruleCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Set<Rule>>() {

                @Override
                public Set<Rule> load(String key) throws Exception {
                    return ruleRepo.findByEnableTrueAndType(NotificationType.IMMEDIATE);
                }

            });
            ruleCacheMap.put(tenant, ruleCache);

        }
        Set<Rule> rules = ruleCacheMap.get(tenant).get(tenant);
        return rules;
    }

    /**
     * Clean all {@link Rule} in cache for a tenant
     * @param tenant to clean
     */
    public void cleanTenantCache(String tenant) {
        LoadingCache<String, Set<Rule>> ruleCache = ruleCacheMap.get(tenant);
        if (ruleCache != null) {
            ruleCache.invalidate(tenant);
        }
    }
}
