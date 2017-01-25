package fr.cnes.regards.modules.crawler.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Resources;

import fr.cnes.regards.modules.crawler.domain.criterion.AbstractMultiCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.DateRangeCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterionVisitor;
import fr.cnes.regards.modules.crawler.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.NotCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.StringMatchAnyCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.StringMatchCriterion;

// CHECKSTYLE:OFF
public class CriterionTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws IOException {
        // textAtt contains "testContains"
        ICriterion containsCrit = ICriterion.contains("attributes.text", "testContains");
        // textAtt ends with "testEndsWith"
        ICriterion endsWithCrit = ICriterion.endsWith("attributes.text", "testEndsWith");
        // textAtt startsWith "testStartsWith"
        ICriterion startsWithCrit = ICriterion.startsWith("attributes.text", "testStartsWith");
        // textAtt strictly equals "testEquals"
        ICriterion equalsCrit = ICriterion.equals("attributes.text", "testEquals");
        ICriterionVisitor<String> visitor = new EsQueryDslVisitor();

        List<ICriterion> numericCritList = new ArrayList<>();
        numericCritList.add(ICriterion.gt("attributes.number1", 10));
        numericCritList.add(ICriterion.lt("attributes.number1", 20));
        numericCritList.add(ICriterion.between("attributes.number3", 0, 100));

        numericCritList.add(ICriterion.ge("attributes.number2", 10.));
        numericCritList.add(ICriterion.le("attributes.number2", 20.));
        numericCritList.add(ICriterion.between("attributes.number4", 0., 100.));

        numericCritList.add(ICriterion.ne("attributes.number1", 500));
        numericCritList.add(ICriterion.ne("attributes.number4", 1000., 1.e0));

        numericCritList.add(ICriterion.eq("attributes.number3", Math.PI, 1.e-5));
        ICriterion numericAndCriterion = ICriterion.and(numericCritList);

        // All theses criterions (AND)
        ICriterion rootCrit = ICriterion.and(containsCrit, endsWithCrit, startsWithCrit, equalsCrit,
                                             numericAndCriterion);

        String text = Resources.toString(Resources.getResource("test1.txt"), Charsets.UTF_8);
        Assert.assertEquals(text, rootCrit.accept(visitor));
    }

    /**
     * Visitor to generate Elasticsearch Query DSL syntax from criterons
     */
    private static class EsQueryDslVisitor implements ICriterionVisitor<String> {

        @Override
        public String visitAndCriterion(AbstractMultiCriterion pCriterion) {
            return "{\n  \"bool\": {\n    \"must\": [\n"
                    + pCriterion.getCriterions().stream().map(c -> c.accept(this)).collect(Collectors.joining(",\n"))
                    + "\n    ]\n  }\n}";
        }

        @Override
        public String visitOrCriterion(AbstractMultiCriterion pCriterion) {
            return "{\n  \"bool\": {\n    \"should\": [\n"
                    + pCriterion.getCriterions().stream().map(c -> c.accept(this)).collect(Collectors.joining(",\n"))
                    + "\n    ]\n  }\n}";
        }

        @Override
        public String visitNotCriterion(NotCriterion pCriterion) {
            return "{\n\"bool\": {\n  \"must_not\": [\n" + pCriterion.getCriterion().accept(this) + "\n  ]\n}\n}";
        }

        @Override
        public String visitIntMatchCriterion(IntMatchCriterion pCriterion) {
            return "{\"match\": { \"" + pCriterion.getName() + "\": " + pCriterion.getValue().toString() + "} }";
        }

        @Override
        public String visitStringMatchCriterion(StringMatchCriterion pCriterion) {
            String searchType = "match";
            String searchValue = pCriterion.getValue();
            switch (pCriterion.getType()) {
                case EQUALS:
                    searchType = "match_phrase";
                    break;
                case STARTS_WITH:
                    searchType = "match_phrase_prefix";
                    break;
                case ENDS_WITH:
                    searchType = "regexp";
                    searchValue = ".*" + pCriterion.getValue();
                    break;
                case CONTAINS:
                default:
            }
            return "{\"" + searchType + "\": {\"" + pCriterion.getName() + "\": \"" + searchValue + "\" }}";
        }

        @Override
        public String visitStringMatchAnyCriterion(StringMatchAnyCriterion pCriterion) {
            return "{\"" + "match" + "\": {\"" + pCriterion.getName() + "\": \""
                    + Joiner.on(" ").join(pCriterion.getValue()) + "\" }}";
        }

        @Override
        public <T> String visitRangeCriterion(RangeCriterion<T> pCriterion) {
            StringBuilder buf = new StringBuilder("{\n\"range\": {\n\"").append(pCriterion.getName()).append("\": {\n");
            // for all comparisons
            String ranges = pCriterion.getValueComparisons().stream().map(valueComp -> {
                String op;
                switch (valueComp.getOperator()) {
                    case GREATER:
                        op = "\"gt\"";
                        break;
                    case GREATER_OR_EQUAL:
                        op = "\"gte\"";
                        break;
                    case LESS:
                        op = "\"lt\"";
                        break;
                    case LESS_OR_EQUAL:
                        op = "\"lte\"";
                        break;
                    default:
                        op = "";
                }
                return op + ": " + valueComp.getValue();
            }).collect(Collectors.joining(",\n"));
            buf.append(ranges).append("\n}\n}\n}");
            return buf.toString();
        }

        @Override
        public String visitDateRangeCriterion(DateRangeCriterion pCriterion) {
            return null;
        }

    }
}
// CHECKSTYLE:ON
