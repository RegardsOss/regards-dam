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
package fr.cnes.regards.modules.dam.rest.entities;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsIT;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.dam.dao.entities.ICollectionRepository;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.model.dao.IModelRepository;
import fr.cnes.regards.modules.model.domain.Model;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 */

public abstract class AbstractCollectionControllerIT extends AbstractRegardsIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCollectionControllerIT.class);

    protected Model model1;

    protected Collection collection1;

    protected Collection collection3;

    protected Collection collection4;

    @Autowired
    protected ICollectionRepository collectionRepository;

    @Autowired
    protected IModelRepository modelRepository;

    protected RequestBuilderCustomizer customizer;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @After
    public void clear() {
        tenantResolver.forceTenant(getDefaultTenant());
        collectionRepository.deleteAll();
        modelRepository.deleteAll();
        tenantResolver.clearTenant();
    }

    @Before
    public void initRepos() {
        clear();
        tenantResolver.forceTenant(getDefaultTenant());
        customizer = customizer();
        // Bootstrap default values
        model1 = Model.build("modelName1", "model desc", EntityType.COLLECTION);
        model1 = modelRepository.save(model1);

        collection1 = new Collection(model1, "PROJECT", "COL1", "collection1");
        collection1.setProviderId("ProviderId1");
        collection1.setLabel("label");
        collection1.setCreationDate(OffsetDateTime.now());
        collection3 = new Collection(model1, "PROJECT", "COL3", "collection3");
        collection3.setProviderId("ProviderId3");
        collection3.setLabel("label");
        collection3.setCreationDate(OffsetDateTime.now());
        collection4 = new Collection(model1, "PROJECT", "COL4", "collection4");
        collection4.setProviderId("ProviderId4");
        collection4.setLabel("label");
        collection4.setCreationDate(OffsetDateTime.now());
        final Set<String> col1Tags = new HashSet<>();
        final Set<String> col4Tags = new HashSet<>();
        col1Tags.add(collection4.getIpId().toString());
        col4Tags.add(collection1.getIpId().toString());
        collection1.setTags(col1Tags);
        collection4.setTags(col4Tags);

        collection1 = collectionRepository.save(collection1);
        collection3 = collectionRepository.save(collection3);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
