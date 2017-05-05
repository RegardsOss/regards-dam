/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.parser;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import fr.cnes.regards.framework.test.report.annotation.Purpose;
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
