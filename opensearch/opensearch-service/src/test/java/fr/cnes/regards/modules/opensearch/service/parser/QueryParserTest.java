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
package fr.cnes.regards.modules.opensearch.service.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.framework.hateoas.HateoasUtils;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.client.models.IAttributeModelClient;
import fr.cnes.regards.modules.dam.domain.entities.StaticProperties;
import fr.cnes.regards.modules.indexer.domain.criterion.AndCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.BooleanMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ComparisonOperator;
import fr.cnes.regards.modules.indexer.domain.criterion.DateMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.DateRangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.MatchType;
import fr.cnes.regards.modules.indexer.domain.criterion.NotCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.OrCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ValueComparison;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.AttributeFinder;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;
import fr.cnes.regards.modules.opensearch.service.utils.SampleDataUtils;

/**
 * Unit test for {@link QueryParser}.
 * @author Marc Sordi
 * @author Xavier-Alexandre Brochard
 */
public class QueryParserTest {

    /**
     * The tenant
     */
    private static final String TENANT = "tenant";

    /**
     * Class under test
     */
    private static QueryParser parser;

    /**
     * Prefix for OpenSearch queries
     */
    private static String QUERY_PREFIX = "q=";

    @BeforeClass
    public static void init() throws EntityNotFoundException {
        ISubscriber subscriber = Mockito.mock(ISubscriber.class);
        IRuntimeTenantResolver runtimeTenantResolver = Mockito.mock(IRuntimeTenantResolver.class);
        Mockito.when(runtimeTenantResolver.getTenant()).thenReturn(TENANT);
        IAttributeModelClient attributeModelClient = Mockito.mock(IAttributeModelClient.class);
        Mockito.when(attributeModelClient.getAttributes(null, null))
                .thenReturn(new ResponseEntity<>(HateoasUtils.wrapList(SampleDataUtils.LIST), HttpStatus.OK));
        IAttributeFinder finder = new AttributeFinder(attributeModelClient, subscriber, runtimeTenantResolver);

        parser = new QueryParser(finder);
    }

    @Test
    @Purpose("Tests empty q")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void emptyQTest() throws OpenSearchParseException {
        parser.parse(QUERY_PREFIX);
    }

    @Test
    @Purpose("Tests queries like isTrue:false")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void booleanMatchTest() throws OpenSearchParseException {
        String field = SampleDataUtils.BOOLEAN_ATTRIBUTE_MODEL.getJsonPath();
        Boolean value = true;
        String term = field + ":" + value;
        ICriterion criterion = parser.parse(QUERY_PREFIX + term);

        Assert.assertTrue(criterion instanceof BooleanMatchCriterion);
    }

    @Test
    @Purpose("Tests queries like altitude:8848")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void intMatchTest() throws OpenSearchParseException {
        final String field = SampleDataUtils.INTEGER_ATTRIBUTE_MODEL.getJsonPath();
        final Integer value = 8848;
        final String term = field + ":" + value;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + term);

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof IntMatchCriterion);

        final IntMatchCriterion crit = (IntMatchCriterion) criterion;
        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        Assert.assertEquals(MatchType.EQUALS, crit.getType());
        Assert.assertEquals(value, crit.getValue());
    }

    @Test
    @Purpose("Tests queries like altitude:-8848")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void negativeIntMatchTest() throws OpenSearchParseException {
        final String field = SampleDataUtils.INTEGER_ATTRIBUTE_MODEL.getJsonPath();
        final Integer value = -8848;
        final String term = field + ":" + value;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + term);

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof IntMatchCriterion);

        final IntMatchCriterion crit = (IntMatchCriterion) criterion;
        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        Assert.assertEquals(MatchType.EQUALS, crit.getType());
        Assert.assertEquals(value, crit.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    @Purpose("Tests queries like bpm:128.0")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void doubleMatchTest() throws OpenSearchParseException {
        final String field = SampleDataUtils.DOUBLE_ATTRIBUTE_MODEL.getJsonPath();
        final Double value = 145.6;
        final String term = field + ":" + value;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + term);

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<Double> crit = (RangeCriterion<Double>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<Double>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons
                .add(new ValueComparison<Double>(ComparisonOperator.GREATER_OR_EQUAL, Math.nextDown(value)));
        expectedValueComparisons.add(new ValueComparison<Double>(ComparisonOperator.LESS_OR_EQUAL, Math.nextUp(value)));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @Test
    @Purpose("Tests queries like title:\"harrypotter\"")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void stringMatchExactTest() throws OpenSearchParseException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        //do not forget that the parser is supposed to parse URI encoded values so \"=>%22
        final String val = "harrypotter";
        final String term = key + ":" + "%22" + val + "%22";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + term);

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof StringMatchCriterion);

        final StringMatchCriterion crit = (StringMatchCriterion) criterion;
        Assert.assertEquals(key, getShortCriterionName(crit.getName()));
        Assert.assertEquals(MatchType.EQUALS, crit.getType());
        Assert.assertEquals(val, crit.getValue());
    }

    @Test(expected = OpenSearchParseException.class)
    @Purpose("Verify that parenthesis inside a regexp cannot be interpreted")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void stringRegexpInvalid() throws OpenSearchParseException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        final String val = "[1-9](.*)";
        final String term = key + ":" + val;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + term);
    }

    @Test
    public void stringRegexpFull() throws OpenSearchParseException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        // value is: ^[1-9]{4}a?b+c~3\:?\\.*$
        final String val = "^[1-9]{4}a?b+c~3\\:?\\\\.*$";
        String urlEncodedVal = "(%5E%5B1-9%5D%7B4%7Da%3Fb%2Bc~3%5C%3A%3F%5C%5C.%2A%24)";
        final StringMatchCriterion crit = testParseString(key, urlEncodedVal);
        Assert.assertEquals(val.replaceAll("\\\\:", ":"), crit.getValue());
    }

    @Test
    public void stringRegexpWithSpace() throws OpenSearchParseException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        // %3A is : url encoded
        final String val = "[1-9] \\\\.*";
        String urlEncodedVal = "(%5B1-9%5D%20%5C%5C.*)";
        final StringMatchCriterion crit = testParseString(key, urlEncodedVal);
        Assert.assertEquals(val, crit.getValue());
    }

    @Test
    public void stringRegexpWithParenthesisChar() throws OpenSearchParseException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        // %3A is : url encoded
        final String val = "[1-9]\\\\( \\\\.*";
        String urlEncodedVal = "(%5B1-9%5D%5C%5C(%20%5C%5C.*)";
        final StringMatchCriterion crit = testParseString(key, urlEncodedVal);
        Assert.assertEquals(val.replaceAll("\\\\[(]", "\\("), crit.getValue());
    }

    @Test
    public void stringRegexpWithParenthesisRegexp() throws OpenSearchParseException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        // %3A is : url encoded
        final String val = "[1-9]\\( \\\\.*";
        String urlEncodedVal = "(%5B1-9%5D%5C(%20%5C%5C.*)";
        final StringMatchCriterion crit = testParseString(key, urlEncodedVal);
        Assert.assertEquals(val.replaceAll("\\\\[(]", "("), crit.getValue());
    }

    @Test
    public void multipleProperties() throws OpenSearchParseException {
        final String key1 = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        // %3A is : url encoded
        final String val = "[1-9] \\\\.*";
        String urlEncodedVal = "(%5B1-9%5D%20%5C%5C.*)";
        String key2 = SampleDataUtils.STRING_ATTRIBUTE_MODEL_1.getJsonPath();
        String key3 = SampleDataUtils.STRING_ARRAY_ATTRIBUTE_MODEL.getJsonPath();
        String term = "((" + key1 + ":" + urlEncodedVal + ")%20AND%20(" + key2 + ":" + urlEncodedVal + "))%20OR%20(" + key3
                + ":" + urlEncodedVal + ")";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + term);

        Assert.assertNotNull(criterion);

        OrCriterion orCrit = (OrCriterion) criterion;

        AndCriterion andCriterion = (AndCriterion) orCrit.getCriterions().get(0);

        StringMatchCriterion key1Crit = (StringMatchCriterion) andCriterion.getCriterions().get(0);
        Assert.assertEquals(key1, getShortCriterionName(key1Crit.getName()));
        Assert.assertEquals(MatchType.REGEXP, key1Crit.getType());
        Assert.assertEquals(val, key1Crit.getValue());

        StringMatchCriterion key2Crit = (StringMatchCriterion) andCriterion.getCriterions().get(1);
        Assert.assertEquals(key2, getShortCriterionName(key2Crit.getName()));
        Assert.assertEquals(MatchType.REGEXP, key2Crit.getType());
        Assert.assertEquals(val, key2Crit.getValue());

        StringMatchCriterion key3Crit = (StringMatchCriterion) orCrit.getCriterions().get(1);
        Assert.assertEquals(key3, getShortCriterionName(key3Crit.getName()));
        Assert.assertEquals(MatchType.REGEXP, key3Crit.getType());
        Assert.assertEquals(val, key3Crit.getValue());
    }

    /**
     * Do not test the value as it could differ. For example if there is parenthesis around val
     */
    private StringMatchCriterion testParseString(String key, String val) throws OpenSearchParseException {
        final String term = key + ":" + val;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + term);

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof StringMatchCriterion);

        final StringMatchCriterion crit = (StringMatchCriterion) criterion;
        Assert.assertEquals(key, getShortCriterionName(crit.getName()));
        Assert.assertEquals(MatchType.REGEXP, crit.getType());
        return crit;
    }

    @Test
    @Purpose("Tests queries like title:.*harrypotter.*")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void stringMatchTest() throws OpenSearchParseException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        final String val = ".*harrypotter.*";
        final StringMatchCriterion crit = testParseString(key, val);
        Assert.assertEquals(val, crit.getValue());
    }

    @Test
    @Purpose("Tests queries like title:\"harry potter\"")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void stringPhraseMatchTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        final String val = "\"a phrase query\"";
        final String expectedAfterParse = "a phrase query";
        final String term = key + ":" + val;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof StringMatchCriterion);

        final StringMatchCriterion crit = (StringMatchCriterion) criterion;
        Assert.assertEquals(key, getShortCriterionName(crit.getName()));
        Assert.assertEquals(MatchType.EQUALS, crit.getType());
        Assert.assertEquals(expectedAfterParse, crit.getValue());
    }

    @Test
    @Purpose("Tests queries like title:*potter")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void wildcardLeading() throws OpenSearchParseException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        final String val = "*potter";
        final StringMatchCriterion crit = testParseString(key, val);
        Assert.assertEquals("*potter", crit.getValue());
    }

    @Test
    @Purpose("Tests queries like title:harry*")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void wildcardTrailing() throws OpenSearchParseException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        final String val = "harry*";
        final StringMatchCriterion crit = testParseString(key, val);
        Assert.assertEquals(val, crit.getValue());
    }

    @Test
    @Purpose("Tests queries like tag:<ip id containing :>")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void ipIdParsing() throws OpenSearchParseException, UnsupportedEncodingException {
        String key = StaticProperties.FEATURE_TAGS;
        String val = "\"URN\\:PROJECT\\:DATA\\:patati\\:V1\"";
        String term = key + ":" + val;
        ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof StringMatchCriterion);

        final StringMatchCriterion crit = (StringMatchCriterion) criterion;
        Assert.assertEquals(key, getShortCriterionName(crit.getName()));
    }

    @Test
    @Purpose("Tests queries like title:*rry*")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void wildcardsAround() throws OpenSearchParseException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        final String val = "*RRY*";
        final StringMatchCriterion crit = testParseString(key, val);
        Assert.assertEquals(val, crit.getValue());
    }

    @Test
    @Purpose("Tests queries like title:har*ter")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void wildcardMiddleTest() throws OpenSearchParseException {
        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        final String val = "har*ter";
        final StringMatchCriterion crit = testParseString(key, val);
        Assert.assertEquals(val, crit.getValue());
    }

    @Test
    @Purpose("Tests queries like title:harrypotter AND author:jkrowling")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void andMatchTest() throws OpenSearchParseException, UnsupportedEncodingException {
        String key0 = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        String key1 = SampleDataUtils.STRING_ATTRIBUTE_MODEL_1.getJsonPath();
        String term = key0 + ":harrypotter AND " + key1 + ":jkrowling";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof AndCriterion);

        final AndCriterion crit = (AndCriterion) criterion;
        Assert.assertEquals(2, crit.getCriterions().size());
        Assert.assertTrue(crit.getCriterions().get(0) instanceof StringMatchCriterion);
        Assert.assertTrue(crit.getCriterions().get(1) instanceof StringMatchCriterion);
    }

    @Test
    @Purpose("Tests queries like title:harrypotter OR author:jkrowling")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void orMatchTest() throws OpenSearchParseException, UnsupportedEncodingException {
        String key0 = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        String key1 = SampleDataUtils.STRING_ATTRIBUTE_MODEL_1.getJsonPath();
        String term = key0 + ":val1 OR " + key1 + ":val2";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof OrCriterion);

        final OrCriterion crit = (OrCriterion) criterion;
        Assert.assertEquals(2, crit.getCriterions().size());
        Assert.assertTrue(crit.getCriterions().get(0) instanceof StringMatchCriterion);
        Assert.assertTrue(crit.getCriterions().get(1) instanceof StringMatchCriterion);
    }

    @Test(expected = OpenSearchParseException.class)
    @Purpose("Tests queries like title:harry~")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void proximityTest() throws OpenSearchParseException {
        parser.parse(QUERY_PREFIX + "field:value~");
    }

    @Test(expected = OpenSearchParseException.class)
    @Purpose("Tests queries like title:{harrypotter TO starwars}")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void stringRangeTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.STRING_RANGE_FIELD;
        final String lowerInclusion = "{";
        final String lowerValue = "harrypotter";
        final String upperValue = "starwars";
        final String upperInclusion = "}";
        final String term = field + ":" + lowerInclusion + lowerValue + " TO " + upperValue + upperInclusion;
        parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like altitude:{90 TO 120}")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void integerRangeExclusiveTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.INTEGER_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final String lowerInclusion = "{";
        final Integer lowerValue = 90;
        final Integer upperValue = 120;
        final String upperInclusion = "}";
        final String term = field + ":" + lowerInclusion + lowerValue + " TO " + upperValue + upperInclusion;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<Integer> crit = (RangeCriterion<Integer>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<Integer>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<Integer>(ComparisonOperator.GREATER_OR_EQUAL, lowerValue));
        expectedValueComparisons.add(new ValueComparison<Integer>(ComparisonOperator.LESS_OR_EQUAL, lowerValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like altitude:[90 TO 120]")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void integerRangeInclusiveTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.INTEGER_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final String lowerInclusion = "[";
        final Integer lowerValue = 0;
        final Integer upperValue = 2;
        final String upperInclusion = "]";
        final String term = field + ":" + lowerInclusion + lowerValue + " TO " + upperValue + upperInclusion;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<Integer> crit = (RangeCriterion<Integer>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<Integer>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<Integer>(ComparisonOperator.GREATER_OR_EQUAL, lowerValue));
        expectedValueComparisons.add(new ValueComparison<Integer>(ComparisonOperator.LESS_OR_EQUAL, lowerValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like bpm:{128.0 TO 145]")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void doubleRangeSemiInclusiveTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.DOUBLE_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final String lowerInclusion = "{";
        final Double lowerValue = 128d;
        final Double upperValue = 145d;
        final String upperInclusion = "]";
        final String term = field + ":" + lowerInclusion + lowerValue + " TO " + upperValue + upperInclusion;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<Double> crit = (RangeCriterion<Double>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<Double>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<Double>(ComparisonOperator.GREATER_OR_EQUAL, lowerValue));
        expectedValueComparisons.add(new ValueComparison<Double>(ComparisonOperator.LESS_OR_EQUAL, lowerValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like distance:{0 TO 88]")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void longRangeTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.LONG_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final String lowerInclusion = "{";
        final Long lowerValue = 0L;
        final Long upperValue = 88L;
        final String upperInclusion = "]";
        final String term = field + ":" + lowerInclusion + lowerValue + " TO " + upperValue + upperInclusion;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<Long> crit = (RangeCriterion<Long>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<Long>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<Long>(ComparisonOperator.GREATER_OR_EQUAL, lowerValue));
        expectedValueComparisons.add(new ValueComparison<Long>(ComparisonOperator.LESS_OR_EQUAL, lowerValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @Test
    @Purpose("Tests queries like date:[2007-12-03T10:15:30 TO 2007-12-03T11:15:30]")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void OffsetDateTimeRangeTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.LOCAL_DATE_TIME_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final String lowerInclusion = "{";
        final OffsetDateTime lowerValue = OffsetDateTime.now().minusHours(1);
        final OffsetDateTime upperValue = OffsetDateTime.now();
        final String upperInclusion = "]";
        final String term = field + ":" + lowerInclusion + lowerValue + " TO " + upperValue + upperInclusion;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof DateRangeCriterion);
        final DateRangeCriterion crit = (DateRangeCriterion) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<OffsetDateTime>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, lowerValue));
        expectedValueComparisons.add(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, lowerValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like date:{* TO 2007-12-03T10:15:30}")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void OffsetDateTimeLtTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.LOCAL_DATE_TIME_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final OffsetDateTime upperValue = OffsetDateTime.now();
        final String term = field + ":{ * TO " + upperValue + "}";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<OffsetDateTime> crit = (RangeCriterion<OffsetDateTime>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<OffsetDateTime>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<OffsetDateTime>(ComparisonOperator.LESS, upperValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like date:{* TO 2007-12-03T10:15:30]")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void OffsetDateTimeLeTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.LOCAL_DATE_TIME_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final OffsetDateTime upperValue = OffsetDateTime.now();
        final String term = field + ":{ * TO " + upperValue + "]";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<OffsetDateTime> crit = (RangeCriterion<OffsetDateTime>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<OffsetDateTime>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<OffsetDateTime>(ComparisonOperator.LESS_OR_EQUAL, upperValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like date:{2007-12-03T10:15:30 TO *}")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void OffsetDateTimeGtTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.LOCAL_DATE_TIME_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final OffsetDateTime lowerValue = OffsetDateTime.now();
        final String term = field + ":{" + lowerValue + " TO *}";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<OffsetDateTime> crit = (RangeCriterion<OffsetDateTime>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<OffsetDateTime>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<OffsetDateTime>(ComparisonOperator.GREATER, lowerValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like date:[2007-12-03T10:15:30 TO *}")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void OffsetDateTimeGeTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.LOCAL_DATE_TIME_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final OffsetDateTime lowerValue = OffsetDateTime.now();
        final String term = field + ":[ " + lowerValue + " TO *}";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<OffsetDateTime> crit = (RangeCriterion<OffsetDateTime>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<OffsetDateTime>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons
                .add(new ValueComparison<OffsetDateTime>(ComparisonOperator.GREATER_OR_EQUAL, lowerValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @Test
    @Purpose("Tests queries like date:2007-12-03T10:15:30.166Z")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void OffsetDateTimeEqTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.LOCAL_DATE_TIME_ATTRIBUTE_MODEL.getJsonPath();
        final OffsetDateTime lowerValue = OffsetDateTime.now();
        DateTimeFormatter ISO_DATE_TIME_UTC = new DateTimeFormatterBuilder().parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME).optionalStart().appendOffset("+HH:MM", "Z")
                .toFormatter();
        final String term =
                field + ":\"" + ISO_DATE_TIME_UTC.format(lowerValue.withOffsetSameInstant(ZoneOffset.UTC)) + "\"";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof DateMatchCriterion);
        final DateMatchCriterion crit = (DateMatchCriterion) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like altitude:{* TO 1}")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void integerLtTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.INTEGER_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final Integer upperValue = 1;
        final String term = field + ":{ * TO " + upperValue + "}";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<Integer> crit = (RangeCriterion<Integer>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<Integer>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<Integer>(ComparisonOperator.LESS, upperValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like altitude:{* TO 1]")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void integerLeTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.INTEGER_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final Integer upperValue = 1;
        final String term = field + ":{ * TO " + upperValue + "]";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<Integer> crit = (RangeCriterion<Integer>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<Integer>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<Integer>(ComparisonOperator.LESS_OR_EQUAL, upperValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like altitude:{1 TO *}")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void integerGtTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.INTEGER_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final Integer lowerValue = 1;
        final String term = field + ":{" + lowerValue + " TO *}";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<Integer> crit = (RangeCriterion<Integer>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<Integer>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<Integer>(ComparisonOperator.GREATER, lowerValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like altitude:[1 TO *}")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void integerGeTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.INTEGER_RANGE_ATTRIBUTE_MODEL.getJsonPath();
        final Integer lowerValue = 1;
        final String term = field + ":[ " + lowerValue + " TO *}";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<Integer> crit = (RangeCriterion<Integer>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<Integer>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<Integer>(ComparisonOperator.GREATER_OR_EQUAL, lowerValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @Test
    @Purpose("Tests queries like (title:.*harrypotter.*)")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void parenthesisAroundAllTest() throws OpenSearchParseException {
        final String field = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        final String value = ".*harrypotter.*";
        final String term = "(" + field + ":" + value + ")";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + term);

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof StringMatchCriterion);
        final StringMatchCriterion crit = (StringMatchCriterion) criterion;
        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        Assert.assertEquals(MatchType.REGEXP, crit.getType());
        Assert.assertEquals(value, crit.getValue());
    }

    @Test
    @Purpose("Tests queries like title:(harrypotter OR starwars)")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void parenthesisAroundOrTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        final String term = field + ":(harrypotter OR starwars)";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof OrCriterion);
    }

    @Test
    @Purpose("Tests queries like cast:.*danielradcliffe.*")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void stringArrayTest() throws OpenSearchParseException {
        final String field = SampleDataUtils.STRING_ARRAY_ATTRIBUTE_MODEL.getJsonPath();
        final String value = ".*danielradcliffe.*";
        final StringMatchCriterion crit = testParseString(field, value);
        Assert.assertEquals(value, crit.getValue());
    }

    @Test
    @Purpose("Tests queries like years:2001")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void integerArrayTest() throws OpenSearchParseException {
        final String field = SampleDataUtils.INTEGER_ARRAY_ATTRIBUTE_MODEL.getJsonPath();
        final Integer value = 2001;
        final String term = field + ":" + value;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + term);

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof IntMatchCriterion);
        final IntMatchCriterion crit = (IntMatchCriterion) criterion;
        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        Assert.assertEquals(MatchType.EQUALS, crit.getType());
        Assert.assertEquals(value, crit.getValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Purpose("Tests queries like duration:159")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void doubleArrayTest() throws OpenSearchParseException {
        final String field = SampleDataUtils.DOUBLE_ARRAY_ATTRIBUTE_MODEL.getJsonPath();
        final Double value = 159d;
        final String term = field + ":" + value;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + term);

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof RangeCriterion);
        final RangeCriterion<Double> crit = (RangeCriterion<Double>) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<Double>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons
                .add(new ValueComparison<Double>(ComparisonOperator.GREATER_OR_EQUAL, Math.nextDown(value)));
        expectedValueComparisons.add(new ValueComparison<Double>(ComparisonOperator.LESS_OR_EQUAL, Math.nextUp(value)));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @Test
    @Purpose("Tests queries like releases:[2001-11-04T00:00:00 TO 2001-11-16T23:59:59]")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    @Ignore
    public void containsDateBetweenTest() throws OpenSearchParseException, UnsupportedEncodingException {
        final String field = SampleDataUtils.LOCAL_DATE_TIME_ARRAY_ATTRIBUTE_MODEL.getJsonPath();
        final OffsetDateTime lowerValue = OffsetDateTimeAdapter.parse("2001-11-04T00:00:00");
        final OffsetDateTime upperValue = OffsetDateTimeAdapter.parse("2001-11-16T23:59:59");
        final String lowerInclusion = "[";
        final String upperInclusion = "]";
        final String term = field + ":" + lowerInclusion + lowerValue + " TO " + upperValue + upperInclusion;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof DateRangeCriterion);
        final DateRangeCriterion crit = (DateRangeCriterion) criterion;

        Assert.assertEquals(field, getShortCriterionName(crit.getName()));
        final Set<ValueComparison<OffsetDateTime>> expectedValueComparisons = new HashSet<>();
        expectedValueComparisons.add(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, lowerValue));
        expectedValueComparisons.add(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, lowerValue));
        Assert.assertEquals(expectedValueComparisons, crit.getValueComparisons());
    }

    @Test
    @Purpose("Tests queries like tags:plop AND tags:(A\\:A OR B\\:B OR C\\:C)")
    @Requirement("REGARDS_DSL_DAM_ARC_810")
    public void handlesSmallRealLifeQuery() throws OpenSearchParseException, UnsupportedEncodingException {
        final String term = SampleDataUtils.SMALL_REAL_LIFE_QUERY;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof AndCriterion);

        final AndCriterion crit = (AndCriterion) criterion;
        Assert.assertEquals(2, crit.getCriterions().size());
        Assert.assertTrue(crit.getCriterions().get(0) instanceof StringMatchCriterion);
        Assert.assertTrue(crit.getCriterions().get(1) instanceof OrCriterion);
    }

    @Test
    @Purpose("Checks that escaping special characters is not needed when using double quotes")
    public void escapingNotNeededWhenDoubleQuotes() throws OpenSearchParseException, UnsupportedEncodingException {
        final String term = SampleDataUtils.UNESCAPED_QUERY_WITH_DOUBLE_QUOTES_AND_CHARS_TO_ESCAPE;
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder.encode(term, "UTF-8"));

        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof StringMatchCriterion);

        final StringMatchCriterion crit = (StringMatchCriterion) criterion;
        Assert.assertEquals(MatchType.EQUALS, crit.getType());
        Assert.assertEquals("texte avec:des caractères+spéciaux", crit.getValue());
    }

    @Test
    public void notMatchTest() throws OpenSearchParseException, UnsupportedEncodingException {

        final String key = SampleDataUtils.STRING_ATTRIBUTE_MODEL.getJsonPath();
        final String val = "harrypotter";
        final ICriterion criterion = parser.parse(QUERY_PREFIX + URLEncoder
                .encode("!(" + key + ":" + val + " OR " + key + ":" + val + ")", "UTF-8"));
        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof NotCriterion);
    }

    private String getShortCriterionName(String criterionName) {
        return criterionName.substring(StaticProperties.FEATURE_NS.length());
    }
}
