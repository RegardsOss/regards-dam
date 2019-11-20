/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.notifier.service;

import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.hateoas.HateoasUtils;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.model.client.IAttributeModelClient;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.gson.IAttributeHelper;

/**
*
* Helper class to retrieve model attributes
* @author Kevin Marchois
*
* FIXME centralize code (catalog + feature + notification)
*
*/
@Component
public class AttributeHelper implements IAttributeHelper {

    /**
     * Runtime tenant resolver
     */
    private final IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * Feign client for rs-dam {@link AttributeModel} controller. Autowired by Spring.
     */
    private final IAttributeModelClient attributeModelClient;

    public AttributeHelper(IRuntimeTenantResolver runtimeTenantResolver, IAttributeModelClient attributeModelClient) {
        this.runtimeTenantResolver = runtimeTenantResolver;
        this.attributeModelClient = attributeModelClient;
    }

    @Override
    public List<AttributeModel> getAllAttributes(String pTenant) {
        try {
            runtimeTenantResolver.forceTenant(pTenant);
            FeignSecurityManager.asSystem();

            ResponseEntity<List<Resource<AttributeModel>>> resources = attributeModelClient.getAttributes(null, null);
            if (resources != null) {
                return HateoasUtils.unwrapList(resources.getBody());
            }
        } finally {
            runtimeTenantResolver.clearTenant();
            FeignSecurityManager.reset();
        }
        return null;
    }
}
