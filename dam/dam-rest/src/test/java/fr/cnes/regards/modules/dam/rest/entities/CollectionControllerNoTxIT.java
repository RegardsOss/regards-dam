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
package fr.cnes.regards.modules.dam.rest.entities;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.rest.DamRestConfiguration;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 */
@TestPropertySource(locations = "classpath:test.properties",
        properties = { "spring.jpa.properties.hibernate.default_schema=collectionitnotx" })
@ContextConfiguration(classes = { DamRestConfiguration.class })
public class CollectionControllerNoTxIT extends AbstractCollectionControllerIT {

    @Autowired
    protected IRuntimeTenantResolver runtimetenantResolver;

    @Override
    public void initRepos() {
        runtimetenantResolver.forceTenant(getDefaultTenant());
        super.initRepos();
    }

    @Test
    public void testDissociateCollections() {
        List<UniformResourceName> toDissociate = new ArrayList<>();
        toDissociate.add(collection3.getIpId());
        customizer.expectStatusNoContent();
        performDefaultPut(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_DISSOCIATE_MAPPING,
                          toDissociate, customizer, "Failed to dissociate collections from one collection using its id",
                          collection1.getId());

        List<UniformResourceName> toAssociate = new ArrayList<>();
        toAssociate.add(collection4.getIpId());
        performDefaultPut(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_ASSOCIATE_MAPPING,
                          toAssociate, customizer, "Failed to associate collections from one collection using its id",
                          collection1.getId());
    }
}
