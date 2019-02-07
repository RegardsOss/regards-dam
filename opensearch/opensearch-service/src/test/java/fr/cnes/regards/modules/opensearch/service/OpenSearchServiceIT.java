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
package fr.cnes.regards.modules.opensearch.service;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.search.schema.OpenSearchDescription;
import fr.cnes.regards.modules.search.schema.UrlType;

/**
 * @author sbinda
 *
 */
@TestPropertySource(locations = "classpath:test.properties")
@MultitenantTransactional
public class OpenSearchServiceIT extends AbstractRegardsTransactionalIT {

    private static final Logger LOG = LoggerFactory.getLogger(OpenSearchServiceIT.class);

    @Autowired
    IOpenSearchService opensearchService;

    @Test
    public void test() throws Exception {
        OpenSearchDescription desc = opensearchService
                .readDescriptor(new URL("https://theia.cnes.fr/atdistrib/resto2/api/collections/describe.xml"));
        LOG.info(desc.getDescription());
        UrlType url = opensearchService.getSearchRequestURL(desc, MediaType.APPLICATION_JSON);
        Assert.assertNotNull("JSON Opensearch request should not be null from THEIA descriptor", url);
        Assert.assertFalse("There sould be parameters for the search request", url.getParameter().isEmpty());
        url.getParameter().forEach(p -> {
            LOG.info(String.format("Available parameter %s - %s", p.getName(), p.getTitle()));
        });
    }

}
