/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.PolygonCriterion;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * Unit test for {@link GeometryParser}
 * @author Xavier-Alexandre Brochard
 */
public class GeometryParserTest {

    /**
     * The tested parser
     */
    private static final GeometryParser PARSER = new GeometryParser();

    @Test
    @Requirement("REGARDS_DSL_DAM_PLG_250")
    @Purpose("Test queries like g=POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2))")
    public final void testParse_shouldParseGeometry() throws OpenSearchParseException, UnsupportedEncodingException {
        String request = URLEncoder.encode("POLYGON((1 2,3 4,5 6,7 8,1 2),(9 8,7 6,5 4,3 2,9 8))", "UTF-8");
        ICriterion criterion = PARSER.parse("g=" + request);
        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof PolygonCriterion);

        final PolygonCriterion crit = (PolygonCriterion) criterion;
        // External ring
        Assert.assertThat(crit.getCoordinates()[0][0][0], Matchers.equalTo(1d));
        Assert.assertThat(crit.getCoordinates()[0][0][1], Matchers.equalTo(2d));

        Assert.assertThat(crit.getCoordinates()[0][1][0], Matchers.equalTo(3d));
        Assert.assertThat(crit.getCoordinates()[0][1][1], Matchers.equalTo(4d));

        Assert.assertThat(crit.getCoordinates()[0][2][0], Matchers.equalTo(5d));
        Assert.assertThat(crit.getCoordinates()[0][2][1], Matchers.equalTo(6d));

        Assert.assertThat(crit.getCoordinates()[0][3][0], Matchers.equalTo(7d));
        Assert.assertThat(crit.getCoordinates()[0][3][1], Matchers.equalTo(8d));

        Assert.assertThat(crit.getCoordinates()[0][4][0], Matchers.equalTo(1d));
        Assert.assertThat(crit.getCoordinates()[0][4][1], Matchers.equalTo(2d));

        // Internal ring
        Assert.assertThat(crit.getCoordinates()[1][0][0], Matchers.equalTo(9d));
        Assert.assertThat(crit.getCoordinates()[1][0][1], Matchers.equalTo(8d));

        Assert.assertThat(crit.getCoordinates()[1][1][0], Matchers.equalTo(7d));
        Assert.assertThat(crit.getCoordinates()[1][1][1], Matchers.equalTo(6d));

        Assert.assertThat(crit.getCoordinates()[1][2][0], Matchers.equalTo(5d));
        Assert.assertThat(crit.getCoordinates()[1][2][1], Matchers.equalTo(4d));

        Assert.assertThat(crit.getCoordinates()[1][3][0], Matchers.equalTo(3d));
        Assert.assertThat(crit.getCoordinates()[1][3][1], Matchers.equalTo(2d));

        Assert.assertThat(crit.getCoordinates()[1][4][0], Matchers.equalTo(9d));
        Assert.assertThat(crit.getCoordinates()[1][4][1], Matchers.equalTo(8d));
    }

    @Test(expected = OpenSearchParseException.class)
    @Requirement("REGARDS_DSL_DAM_PLG_250")
    @Purpose("Test queries like g=MULTILINESTRING((3 4,10 50,20 25),(-5 -8,-10 -8,-15 -4))")
    public final void testParse_shouldFailIfNotPolygon() throws OpenSearchParseException, UnsupportedEncodingException {
        String request = URLEncoder.encode("MULTILINESTRING((3 4,10 50,20 25),(-5 -8,-10 -8,-15 -4))", "UTF-8");
        PARSER.parse("g=" + request);
    }

}
