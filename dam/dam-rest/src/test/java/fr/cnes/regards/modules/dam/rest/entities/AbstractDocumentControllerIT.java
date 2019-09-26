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

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsIT;
import fr.cnes.regards.modules.dam.dao.entities.ICollectionRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDocumentRepository;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.domain.entities.Document;
import fr.cnes.regards.modules.model.dao.IModelRepository;
import fr.cnes.regards.modules.model.domain.Model;

/**
 * @author lmieulet
 */
public abstract class AbstractDocumentControllerIT extends AbstractRegardsIT {

    protected Model model1;

    protected Model model2;

    protected Collection collection1;

    protected Collection collection2;

    protected Document document1;

    protected Document document2;

    @Autowired
    protected IDocumentRepository documentRepository;

    @Autowired
    protected ICollectionRepository collectionRepository;

    @Autowired
    protected IModelRepository modelRepository;

    @Autowired
    protected IRuntimeTenantResolver runtimetenantResolver;

    @After
    public void clear() {
        runtimetenantResolver.forceTenant(getDefaultTenant());
        collectionRepository.deleteAll();
        documentRepository.deleteAll();
        modelRepository.deleteAll();
        runtimetenantResolver.clearTenant();
    }

    @Before
    public void initRepos() {

        clear();
        runtimetenantResolver.forceTenant(getDefaultTenant());
        // Bootstrap default values
        model1 = Model.build("documentModelName1", "model desc", EntityType.COLLECTION);
        model2 = Model.build("documentModelName2", "model desc", EntityType.DOCUMENT);
        model2 = modelRepository.save(model2);
        model1 = modelRepository.save(model1);

        collection1 = new Collection(model1, "PROJECT", "COL1", "collection1");
        collection1.setProviderId("ProviderId1");
        collection1.setLabel("label");
        collection1.setCreationDate(OffsetDateTime.now());

        collection2 = new Collection(model1, "PROJECT", "COL2", "collection2");
        collection2.setProviderId("ProviderId1");
        collection2.setLabel("label");
        collection2.setCreationDate(OffsetDateTime.now());

        document1 = new Document(model2, "PROJECT", "DOC1", "document1");
        document1.setProviderId("ProviderId2");
        document1.setLabel("label");
        document1.setCreationDate(OffsetDateTime.now());

        document2 = new Document(model2, "PROJECT", "DOC2", "document2");
        document2.setProviderId("ProviderId3");
        document2.setLabel("label");
        document2.setCreationDate(OffsetDateTime.now());
        final Set<String> doc2Tags = new HashSet<>();
        doc2Tags.add(collection1.getIpId().toString());
        document2.setTags(doc2Tags);

        collection1 = collectionRepository.save(collection1);
        collection2 = collectionRepository.save(collection2);
        document1 = documentRepository.save(document1);
        document2 = documentRepository.save(document2);
    }
}
