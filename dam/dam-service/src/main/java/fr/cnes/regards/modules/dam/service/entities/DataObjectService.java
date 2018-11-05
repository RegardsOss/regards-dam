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
package fr.cnes.regards.modules.dam.service.entities;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.service.entities.validator.AttributeTypeValidator;
import fr.cnes.regards.modules.dam.service.entities.validator.ComputationModeValidator;
import fr.cnes.regards.modules.dam.service.entities.validator.restriction.RestrictionValidatorFactory;
import fr.cnes.regards.modules.dam.service.models.IModelAttrAssocService;

/**
 * Specific EntityService for data objects.
 * By now it concerns only data object validation.
 * <b>NOTE : this service is not transactional because data objects are not persisted into database, only
 * ElasticSearch</b>
 * @author oroussel
 */
@Service
public class DataObjectService extends AbstractValidationService<DataObject> {

    /**
     * Classic Spring validator used to validate each DataObject (ie with annotations)
     */
    private Validator dataObjectValidator;

    /**
     * Data object validation is done on all data objects from ingestion, ie for a thousands objects in a very limited
     * duration so a cache is used
     */
    private LoadingCache<String, List<ModelAttrAssoc>> modelServiceCache;

    /**
     * Original IModelAttrAssocService (ie. not proxyfied)
     */
    private IModelAttrAssocService modelAttrAssocServiceNoProxy;

    /**
     * Constructor
     * @param modelAttributeService model attribute service autowired by Spring on which a proxy with cache is created
     * @param dataObjectValidator classic Spring validator (inspecting class annotations)
     */
    public DataObjectService(final IModelAttrAssocService modelAttributeService, Validator dataObjectValidator) {
        super(modelAttributeService);
        this.modelAttrAssocServiceNoProxy = modelAttributeService;
        this.dataObjectValidator = dataObjectValidator;
        // Init the cache to speed up model attributes from model name retrieval
        modelServiceCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES)
                .build(new CacheLoader<String, List<ModelAttrAssoc>>() {

                    @Override
                    public List<ModelAttrAssoc> load(String modelName) throws Exception {
                        return modelAttributeService.getModelAttrAssocs(modelName);
                    }
                });
        // Use cache when calling for getModelAttrAssocs with modelName
        InvocationHandler invocationHandler = (InvocationHandler) (proxy, method, args) -> {
            if (method.getName().equals("getModelAttrAssocs") && method.getReturnType().equals(List.class)
                    && (args.length == 1) && (args[0] instanceof String)) {
                return modelServiceCache.get((String) args[0]);
            } else { // else call "true" modelService
                return method.invoke(modelAttrAssocServiceNoProxy, args);
            }
        };
        // Replace modelAttributeService by proxy which use cache
        super.modelAttributeService = (IModelAttrAssocService) Proxy
                .newProxyInstance(IModelAttrAssocService.class.getClassLoader(),
                                  new Class<?>[] { IModelAttrAssocService.class }, invocationHandler);
    }

    @Override
    public void validate(DataObject entity, Errors inErrors, boolean manageAlterable) throws EntityInvalidException {
        // First validate data object regarding its annotations
        this.dataObjectValidator.validate(entity, inErrors);
        // Then validate its associated attributes using inherited validation service
        super.validate(entity, inErrors, manageAlterable);
    }

    @Override
    protected List<Validator> getValidators(ModelAttrAssoc modelAttrAssoc, String attributeKey, boolean manageAlterable,
            AbstractEntity<?> entity) {
        AttributeModel attModel = modelAttrAssoc.getAttribute();

        List<Validator> validators = new ArrayList<>();
        // Check computation mode
        validators.add(new ComputationModeValidator(modelAttrAssoc.getMode(), attributeKey));
        // Check attribute type
        validators.add(new AttributeTypeValidator(attModel.getType(), attributeKey));
        // Check restriction
        if (attModel.hasRestriction()) {
            validators.add(RestrictionValidatorFactory.getValidator(attModel.getRestriction(), attributeKey));
        }
        return validators;
    }
}
