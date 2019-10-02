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

import java.util.List;
import java.util.Map;

import com.google.common.cache.LoadingCache;

import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;

/**
 * Cache proxy to handle model attributes
 *
 * @author Marc SORDI
 *
 */
public abstract class AbstractCacheableModelFinder implements IModelFinder {

    /**
    * Model cache is used to avoid useless database request as models rarely!
    */
    private Map<String, LoadingCache<String, List<ModelAttrAssoc>>> modelCacheMap;

    //    /**
    //    * Original IModelAttrAssocService (ie. not proxyfied)
    //    */
    //    private IModelAttrAssocService modelAttrAssocServiceNoProxy;
    //
    //
    //    public AbstractCacheableModelFinder() {
    //
    //    // Init the cache to speed up model attributes from model name retrieval
    //        modelCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES)
    //          .build(new CacheLoader<String, List<ModelAttrAssoc>>() {
    //
    //              @Override
    //              public List<ModelAttrAssoc> load(String modelName) throws Exception {
    //                  return findByModel(modelName);
    //              }
    //          });
    //    // Use cache when calling for getModelAttrAssocs with modelName
    //    InvocationHandler invocationHandler = (InvocationHandler) (proxy, method, args) -> {
    //      if (method.getName().equals("getModelAttrAssocs") && method.getReturnType().equals(List.class)
    //              && args.length == 1 && args[0] instanceof String) {
    //          return modelCache.get((String) args[0]);
    //      } else { // else call "true" modelService
    //          return method.invoke(modelAttrAssocServiceNoProxy, args);
    //      }
    //    };
    //    // Replace modelAttributeService by proxy which use cache
    //    super.modelAttributeService = (IModelAttrAssocService) Proxy
    //          .newProxyInstance(IModelAttrAssocService.class.getClassLoader(),
    //                            new Class<?>[] { IModelAttrAssocService.class }, invocationHandler);
    //    }
    //}

}
