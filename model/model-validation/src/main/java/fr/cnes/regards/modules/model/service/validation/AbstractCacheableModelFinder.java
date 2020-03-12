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
package fr.cnes.regards.modules.model.service.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.dto.event.ModelChangeEvent;

/**
 * Cache proxy to handle model attributes
 *
 * @author Marc SORDI
 *
 */
public abstract class AbstractCacheableModelFinder
        implements IModelFinder, ApplicationListener<ApplicationReadyEvent>, IHandler<ModelChangeEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCacheableModelFinder.class);

    /**
    * Model cache is used to avoid useless database request as models rarely change!<br/>
    * tenant key -> model key / attributes val
    */
    private final Map<String, LoadingCache<String, List<ModelAttrAssoc>>> modelCacheMap = new HashMap<>();

    @Autowired
    private ISubscriber subscriber;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        subscriber.subscribeTo(ModelChangeEvent.class, this);
    }

    @Override
    public List<ModelAttrAssoc> findByModel(String model) {
        String tenant = runtimeTenantResolver.getTenant();
        try {
            return getTenantCache(tenant).get(model);
        } catch (ExecutionException e) {
            LOGGER.error("Error during cache initialisation", e);
            return null;
        }
    }

    private LoadingCache<String, List<ModelAttrAssoc>> getTenantCache(String tenant) {

        LoadingCache<String, List<ModelAttrAssoc>> modelCache = modelCacheMap.get(tenant);
        if (modelCache == null) {
            // Dynamically initialize cache
            modelCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES)
                    .build(new CacheLoader<String, List<ModelAttrAssoc>>() {

                        @Override
                        public List<ModelAttrAssoc> load(String modelName) throws Exception {
                            List<ModelAttrAssoc> attributesByModel = loadAttributesByModel(modelName);
                            if (attributesByModel == null) {
                                throw new Exception(String.format("Unknow Model %s", modelName));
                            }
                            return attributesByModel;
                        }
                    });
            modelCacheMap.put(tenant, modelCache);
        }
        return modelCache;
    }

    private void cleanTenantCache(String tenant, String model) {
        LoadingCache<String, List<ModelAttrAssoc>> modelCache = modelCacheMap.get(tenant);
        if (modelCache != null) {
            modelCache.invalidate(model);
        }
    }

    protected abstract List<ModelAttrAssoc> loadAttributesByModel(String modelName);

    @Override
    public void handle(TenantWrapper<ModelChangeEvent> pWrapper) {
        String tenant = pWrapper.getTenant();
        String model = pWrapper.getContent().getModel();
        LOGGER.info("Change detected for model \"{}\" of tenant \"{}\"", model, tenant);
        cleanTenantCache(tenant, model);
    }
}
