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
package fr.cnes.regards.modules.entities.rest;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;

/**
 * Test entity attachments processing
 *
 * @author Marc Sordi
 *
 */
// @TestPropertySource
@MultitenantTransactional
public class AttachmentControllerIT extends AbstractRegardsTransactionalIT {

    @Test
    public void attachDescription() {
        Path filePath = Paths.get("src", "test", "resources", "attachments", "description.pdf");

        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isOk());
        performDefaultFileUpload(AttachmentController.TYPE_MAPPING + AttachmentController.ATTACHMENTS_MAPPING, filePath,
                                 customizer, "Attachment error",
                                 "URN:AIP:DATASET:legacy:9de29e82-e580-45f4-aeea-448defc4d5cb:V1",
                                 DataType.DESCRIPTION);
    }

}
