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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.gson.entities.IAttributeHelper;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;

/**
 *
 * Helper class to retrieve model attributes
 * @author Marc Sordi
 *
 */
@ConditionalOnMissingClass("fr.cnes.regards.modules.search.service.CatalogAttributeHelper")
@Service
public class DamAttributeHelper implements IAttributeHelper {

    /**
     * Runtime tenant resolver
     */
    private final IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * AttributeModel service to retrieve AttributeModels entities.
     */
    private final IAttributeModelService attributeModelService;

    public DamAttributeHelper(IRuntimeTenantResolver runtimeTenantResolver,
            IAttributeModelService attributeModelService) {
        this.runtimeTenantResolver = runtimeTenantResolver;
        this.attributeModelService = attributeModelService;
    }

    @Override
    public List<AttributeModel> getAllAttributes(String pTenant) {
        try {
            runtimeTenantResolver.forceTenant(pTenant);
            return attributeModelService.getAttributes(null, null, null);
        } finally {
            runtimeTenantResolver.clearTenant();
        }
    }
}
