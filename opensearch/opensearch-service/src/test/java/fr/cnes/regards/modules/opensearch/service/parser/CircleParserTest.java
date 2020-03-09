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
package fr.cnes.regards.modules.opensearch.service.parser;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.indexer.domain.criterion.CircleCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * Unit test for {@link CircleParser}
 * @author Xavier-Alexandre Brochard
 */
public class CircleParserTest {

    /**
     * The tested parser
     */
    private static final CircleParser PARSER = new CircleParser();

    @Test
    @Requirement("REGARDS_DSL_DAM_PLG_250")
    @Purpose("Test queries like lat=11&lon=22&r=33")
    public final void testParse_shouldParseLatLonRadius() throws OpenSearchParseException {
        ICriterion criterion = PARSER.parse("lat=11&lon=22&r=33");

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof CircleCriterion);

        final CircleCriterion crit = (CircleCriterion) criterion;
        Assert.assertThat(crit.getCoordinates()[0], Matchers.equalTo(22d));
        Assert.assertThat(crit.getCoordinates()[1], Matchers.equalTo(11d));
        Assert.assertThat(crit.getRadius(), Matchers.equalTo("33"));
    }

}
