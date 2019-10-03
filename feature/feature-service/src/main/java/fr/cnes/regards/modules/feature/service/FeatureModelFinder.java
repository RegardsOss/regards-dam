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
package fr.cnes.regards.modules.feature.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.hateoas.HateoasUtils;
import fr.cnes.regards.modules.model.client.IModelAttrAssocClient;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.service.validation.AbstractCacheableModelFinder;
import fr.cnes.regards.modules.model.service.validation.IModelFinder;

/**
 * @author Marc SORDI
 *
 */
@Service
public class FeatureModelFinder extends AbstractCacheableModelFinder implements IModelFinder {

    @Autowired
    private IModelAttrAssocClient modelAttrAssocClient;

    @Override
    protected List<ModelAttrAssoc> loadAttributesByModel(String modelName) {
        try {
            FeignSecurityManager.asSystem();
            ResponseEntity<List<Resource<ModelAttrAssoc>>> response = modelAttrAssocClient
                    .getModelAttrAssocs(modelName);
            List<ModelAttrAssoc> attModelAssocs = new ArrayList<>();
            if (response != null) {
                attModelAssocs = HateoasUtils.unwrapCollection(response.getBody());
            }
            return attModelAssocs;
        } finally {
            FeignSecurityManager.reset();
        }
    }
}
