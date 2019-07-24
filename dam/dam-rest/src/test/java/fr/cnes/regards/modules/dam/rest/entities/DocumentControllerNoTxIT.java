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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.rest.DamRestConfiguration;

/**
 * @author lmieulet
 */
@TestPropertySource(locations = "classpath:test.properties",
        properties = { "spring.jpa.properties.hibernate.default_schema=documentationitnotx" })
@ContextConfiguration(classes = { DamRestConfiguration.class })
public class DocumentControllerNoTxIT extends AbstractDocumentControllerIT {

    @Requirement("REGARDS_DSL_DAM_DOC_230")
    @Purpose("Shall associate or dissociate tag from the document")
    @Test
    public void testDissociateDocuments() {
        List<UniformResourceName> toDissociate = new ArrayList<>();
        toDissociate.add(collection1.getIpId());

        RequestBuilderCustomizer customizer = customizer().expectStatusNoContent();
        performDefaultPut(DocumentController.TYPE_MAPPING + DocumentController.DOCUMENT_DISSOCIATE_MAPPING,
                          toDissociate, customizer, "Failed to dissociate collections from one document using its id",
                          document2.getId());

        List<UniformResourceName> toAssociate = new ArrayList<>();
        toAssociate.add(collection2.getIpId());

        performDefaultPut(DocumentController.TYPE_MAPPING + DocumentController.DOCUMENT_ASSOCIATE_MAPPING, toAssociate,
                          customizer, "Failed to associate collections from one document using its id",
                          document1.getId());
    }
}
