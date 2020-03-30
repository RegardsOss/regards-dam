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
package fr.cnes.regards.modules.opensearch.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.urn.DataType;
import fr.cnes.regards.modules.dam.domain.entities.StaticProperties;
import fr.cnes.regards.modules.indexer.domain.criterion.AndCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.FieldExistsCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.OrCriterion;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;
import fr.cnes.regards.modules.opensearch.service.parser.ImageOnlyParser;
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

    @Test(expected = ModuleException.class)
    public void testInvalidUrl() throws MalformedURLException, ModuleException {
        opensearchService
                .readDescriptor(new URL("https://theia.cnes.fr/atdistrib/resto2/api/collections/describe.xmlx"));
    }

    @Test(expected = ModuleException.class)
    public void testInvalidUrl2() throws MalformedURLException, ModuleException {
        opensearchService.readDescriptor(new URL("https://plop.com/describe.xml"));
    }

    @Test(expected = ModuleException.class)
    public void testInvalidUrl3() throws MalformedURLException, ModuleException {
        opensearchService.readDescriptor(new URL("https://google.com"));
    }

    @Test
    public void testParseImageOnly() throws OpenSearchParseException {

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add(ImageOnlyParser.IMAGE_ONLY_PARAM, "true");
        ICriterion crit = opensearchService.parse(param);
        // in reality we do not have only an OR crit because other parsers passed and did not add any restriction
        // so our request is interpreted as "nothing & imageOnly"
        // so AndCrit(EmptyCrit, OrCrit(FieldExistsCrits))
        crit = ((AndCriterion) crit).getCriterions().get(1);
        Assert.assertTrue("When parsing a query with " + ImageOnlyParser.IMAGE_ONLY_PARAM
                + " GET parameter, we should have a criterion that is an OR", crit instanceof OrCriterion);
        OrCriterion orCrit = (OrCriterion) crit;
        Assert.assertEquals("There should be 4 elements to this or criterion(one for each image type", 4L,
                            orCrit.getCriterions().size());
        List<FieldExistsCriterion> fieldsToLookFor = orCrit.getCriterions().stream().map(c -> (FieldExistsCriterion) c)
                .collect(Collectors.toList());
        Assert.assertTrue(fieldsToLookFor.get(0).getName()
                .equals(StaticProperties.FEATURE_FILES_PATH + "." + DataType.THUMBNAIL));
        Assert.assertTrue(fieldsToLookFor.get(1).getName()
                .equals(StaticProperties.FEATURE_FILES_PATH + "." + DataType.QUICKLOOK_HD));
        Assert.assertTrue(fieldsToLookFor.get(2).getName()
                .equals(StaticProperties.FEATURE_FILES_PATH + "." + DataType.QUICKLOOK_MD));
        Assert.assertTrue(fieldsToLookFor.get(3).getName()
                .equals(StaticProperties.FEATURE_FILES_PATH + "." + DataType.QUICKLOOK_SD));
    }

}
