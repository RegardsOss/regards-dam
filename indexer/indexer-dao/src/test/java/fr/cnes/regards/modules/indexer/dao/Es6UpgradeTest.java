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
package fr.cnes.regards.modules.indexer.dao;

import java.io.IOException;
import java.util.Collection;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.cnes.regards.modules.indexer.dao.builder.AggregationBuilderFacetTypeVisitor;

/**
 * Tests for testing Elasticsearch 6 upgrade (=> single-type)
 * <b>ONLY TO BE LAUNCHED LOCALLY</b>
 * @author Olivier Rousselot
 */
@Ignore
@RunWith(SpringRunner.class)
public class Es6UpgradeTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(Es6UpgradeTest.class);
    /**
     * Class to test
     */
    private static IEsRepository repository;

    /**
     * JSON mapper
     */
    private static Gson gson;

    /**
     * Befor class setting up method
     * @throws Exception exception
     */
    @Before
    public void setUp() throws Exception {
        boolean repositoryOK = true;
        // we get the properties into target/test-classes because this is where maven will put the filtered file(with real values and not placeholder)
        try {
            gson = new GsonBuilder().create();
            repository = new EsRepository(gson, null, "localhost", 9200,
                                          new AggregationBuilderFacetTypeVisitor(10, 1));
        } catch (NoNodeAvailableException e) {
            repositoryOK = false;
        }
        // Do not launch tests is Elasticsearch is not available
        Assume.assumeTrue(repositoryOK);

    }

    @Test
    public void test() {
        long start = System.currentTimeMillis();
        Collection<String> newIndices = repository.upgradeAllIndices4SingleType();
        if (!newIndices.isEmpty()) {
            LOGGER.info("Upgrade  {} indices in " + (System.currentTimeMillis() - start) + " ms", newIndices.size());
        }
    }

    @Test
    public void testOne() throws IOException {
        String idx = "default";
        String newIdx = repository.reindex(idx);
        Assert.assertTrue(repository.indexExists(newIdx));
        Assert.assertTrue(repository.deleteIndex(idx));
        Assert.assertTrue(repository.createAlias(newIdx, idx));

    }

}
