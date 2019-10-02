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
package fr.cnes.regards.modules.dam.service.entities;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.service.IModelAttrAssocService;
import fr.cnes.regards.modules.model.service.validation.IModelFinder;

/**
 * Retrieve model attributes in DAM context (directly from database)
 * @author Marc SORDI
 */
@Component
public class DamModelFinder implements IModelFinder {

    // FIXME add cache behaviour
    //public DataObjectService(IModelFinder modelFinder, IModelAttrAssocService modelAttributeService,
    //  Validator dataObjectValidator) {
    //super(modelFinder);
    //this.modelAttrAssocServiceNoProxy = modelAttributeService;
    //this.dataObjectValidator = dataObjectValidator;
    //// Init the cache to speed up model attributes from model name retrieval
    //modelServiceCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES)
    //      .build(new CacheLoader<String, List<ModelAttrAssoc>>() {
    //
    //          @Override
    //          public List<ModelAttrAssoc> load(String modelName) throws Exception {
    //              return modelAttributeService.getModelAttrAssocs(modelName);
    //          }
    //      });
    //// Use cache when calling for getModelAttrAssocs with modelName
    //InvocationHandler invocationHandler = (InvocationHandler) (proxy, method, args) -> {
    //  if (method.getName().equals("getModelAttrAssocs") && method.getReturnType().equals(List.class)
    //          && args.length == 1 && args[0] instanceof String) {
    //      return modelServiceCache.get((String) args[0]);
    //  } else { // else call "true" modelService
    //      return method.invoke(modelAttrAssocServiceNoProxy, args);
    //  }
    //};
    //// Replace modelAttributeService by proxy which use cache
    //super.modelAttributeService = (IModelAttrAssocService) Proxy
    //      .newProxyInstance(IModelAttrAssocService.class.getClassLoader(),
    //                        new Class<?>[] { IModelAttrAssocService.class }, invocationHandler);
    //}

    //
    ///**
    //* Data object validation is done on all data objects from ingestion, ie for a thousands objects in a very limited
    //* duration so a cache is used
    //*/
    //private LoadingCache<String, List<ModelAttrAssoc>> modelServiceCache;
    //
    ///**
    //* Original IModelAttrAssocService (ie. not proxyfied)
    //*/
    //private IModelAttrAssocService modelAttrAssocServiceNoProxy;

    @Autowired
    protected IModelAttrAssocService modelAttributeService;

    @Override
    public List<ModelAttrAssoc> findByModel(String model) {
        return modelAttributeService.getModelAttrAssocs(model);
    }
}
