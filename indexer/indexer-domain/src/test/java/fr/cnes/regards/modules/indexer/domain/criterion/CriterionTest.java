package fr.cnes.regards.modules.indexer.domain.criterion;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import fr.cnes.regards.framework.gson.adapters.LocalDateTimeAdapter;
import fr.cnes.regards.modules.indexer.domain.criterion.AbstractMultiCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.BooleanMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.DateRangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.EmptyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterionVisitor;
import fr.cnes.regards.modules.indexer.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.LongMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.NotCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchAnyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchCriterion;

// CHECKSTYLE:OFF
public class CriterionTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test1() throws IOException {
        final String RESULT = "(attributes.text CONTAINS \"testContains\") AND (attributes.text ENDS_WITH "
                + "\"testEndsWith\") AND (attributes.text STARTS_WITH \"testStartsWith\") AND (attributes.text "
                + "EQUALS \"testEquals\") AND ((attributes.number1 ∈ { x / x > 10 }) AND (attributes.number1 ∈ "
                + "{ x / x < 20 }) AND (attributes.number3 ∈ { x / x ≥ 0, x ≤ 100 }) AND (attributes.number2 ∈ "
                + "{ x / x ≥ 10.0 }) AND (attributes.number2 ∈ { x / x ≤ 20.0 }) AND (attributes.number4 ∈ "
                + "{ x / x ≥ 0.0, x ≤ 100.0 }) AND (NOT (attributes.number1 == 500)) AND "
                + "(NOT (attributes.number4 ∈ { x / x ≥ 999.0, x ≤ 1001.0 })) AND "
                + "(attributes.number3 ∈ { x / x ≥ 3.141582653589793, x ≤ 3.141602653589793 }))";
        // textAtt contains "testContains"
        ICriterion containsCrit = ICriterion.contains("attributes.text", "testContains");
        // textAtt ends with "testEndsWith"
        ICriterion endsWithCrit = ICriterion.endsWith("attributes.text", "testEndsWith");
        // textAtt startsWith "testStartsWith"
        ICriterion startsWithCrit = ICriterion.startsWith("attributes.text", "testStartsWith");
        // textAtt strictly equals "testEquals"
        ICriterion equalsCrit = ICriterion.equals("attributes.text", "testEquals");
        ICriterionVisitor<String> visitor = new TestCriterionVisitor();

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

        Assert.assertEquals(RESULT, rootCrit.accept(visitor));
    }

    @Test
    public void test2() throws IOException {
        final String RESULT = "(ALL) OR (attributes.alwaysTrue IS TRUE) OR (attributes.alwaysFalse IS FALSE) OR "
                + "(attributes.creationDate ∈ { x / { x ≥ 2017-01-01T00:00:00, x ≤ 2017-12-31T23:59:59 })";

        ICriterion rootCrit = ICriterion.or(ICriterion.all(), ICriterion.isTrue("attributes.alwaysTrue"),
                                            ICriterion.isFalse("attributes.alwaysFalse"),
                                            ICriterion.between("attributes.creationDate",
                                                               LocalDateTime.of(2017, 1, 1, 0, 0), LocalDateTime
                                                                       .of(2017, 12, 31, 23, 59, 59)));
        ICriterionVisitor<String> visitor = new TestCriterionVisitor();
        Assert.assertEquals(RESULT, rootCrit.accept(visitor));
    }

    @Test
    public void test3() throws IOException {
        final String RESULT = "(att.creationDate ∈ { x / { x > 2010-01-01T00:00:00 }) OR (att.updateDate ∈ "
                + "{ x / { x ≥ 2010-01-01T00:00:00 }) OR (att.deleteDate ∈ { x / { x < 2018-01-01T00:00:00 })"
                + " OR (att.deleteDate ∈ { x / { x ≤ 2017-12-31T23:59:59 })";
        ICriterion rootCrit = ICriterion
                .or(ICriterion.gt("att.creationDate", LocalDateTime.of(2010, 1, 1, 0, 0)),
                    ICriterion.ge("att.updateDate", LocalDateTime.of(2010, 1, 1, 0, 0)),
                    ICriterion.lt("att.deleteDate", LocalDateTime.of(2018, 1, 1, 0, 0)),
                    ICriterion.le("att.deleteDate", LocalDateTime.of(2017, 12, 31, 23, 59, 59)));

        ICriterionVisitor<String> visitor = new TestCriterionVisitor();
        Assert.assertEquals(RESULT, rootCrit.accept(visitor));
    }

    @Test
    public void test4() throws IOException {
        final String RESULT = "((att.id == 1) OR (att.id == 2) OR (att.id == 3) OR (att.id == 4) OR (att.id == 5)) "
                + "OR (att.ints == 3) OR (att.doubles ∈ { x / x ≥ 3.141582653589793, x ≤ 3.141602653589793 }) OR "
                + "(att.dates ∈ { x / { x ≥ 2010-01-01T00:00:00, x ≤ 2020-01-01T00:00:00 })";
        ICriterion rootCrit = ICriterion
                .or(Lists.newArrayList(ICriterion.in("att.id", 1, 2, 3, 4, 5), ICriterion.contains("att.ints", 3),
                                       ICriterion.contains("att.doubles", Math.PI, 1e-5), ICriterion
                                               .containsDateBetween("att.dates", LocalDateTime.of(2010, 1, 1, 0, 0),
                                                                    LocalDateTime.of(2020, 1, 1, 0, 0))));

        ICriterionVisitor<String> visitor = new TestCriterionVisitor();
        Assert.assertEquals(RESULT, rootCrit.accept(visitor));
    }

    @Test
    public void test5() throws IOException {
        final String RESULT = "(att.text IN (\"toto\", \"titi\", \"tutu\")) OR "
                + "((att.text EQUALS \"toto tutu\") OR (att.text EQUALS \"titi tata\"))";
        ICriterion rootCrit = ICriterion.or(ICriterion.in("att.text", "toto", "titi", "tutu"),
                                            ICriterion.in("att.text", "toto tutu", "titi tata"));

        ICriterionVisitor<String> visitor = new TestCriterionVisitor();
        Assert.assertEquals(RESULT, rootCrit.accept(visitor));
    }

    @Test
    public void test6() throws IOException {
        final String RESULT = "((att.intRange.lowerBound ∈ { x / x ≤ 12 }) AND (att.intRange.upperBound ∈ "
                + "{ x / x ≥ 12 })) OR ((att.dateRange.lowerBound ∈ { x / { x ≤ 2020-01-01T00:00:00 }) AND "
                + "(att.dateRange.upperBound ∈ { x / { x ≥ 2010-01-01T00:00:00 }))";
        ICriterion rootCrit = ICriterion.or(ICriterion.into("att.intRange", 12), ICriterion
                .intersects("att.dateRange", LocalDateTime.of(2010, 1, 1, 0, 0), LocalDateTime.of(2020, 1, 1, 0, 0)));

        ICriterionVisitor<String> visitor = new TestCriterionVisitor();
        Assert.assertEquals(RESULT, rootCrit.accept(visitor));
    }

    /**
     * Visitor to generate Elasticsearch Query DSL syntax from criterons
     */
    private static class TestCriterionVisitor implements ICriterionVisitor<String> {

        @Override
        public String visitEmptyCriterion(EmptyCriterion pCriterion) {
            return "ALL";
        }

        @Override
        public String visitAndCriterion(AbstractMultiCriterion pCriterion) {
            return pCriterion.getCriterions().stream().map(c -> c.accept(this))
                    .collect(Collectors.joining(") AND (", "(", ")"));
        }

        @Override
        public String visitOrCriterion(AbstractMultiCriterion pCriterion) {
            return pCriterion.getCriterions().stream().map(c -> c.accept(this))
                    .collect(Collectors.joining(") OR (", "(", ")"));
        }

        @Override
        public String visitNotCriterion(NotCriterion pCriterion) {
            return "NOT (" + pCriterion.getCriterion().accept(this) + ")";
        }

        @Override
        public String visitIntMatchCriterion(IntMatchCriterion pCriterion) {
            return pCriterion.getName() + " == " + pCriterion.getValue().toString();
        }

        @Override
        public String visitStringMatchCriterion(StringMatchCriterion pCriterion) {
            return pCriterion.getName() + " " + pCriterion.getType().toString() + " \"" + pCriterion.getValue() + "\"";
        }

        @Override
        public String visitLongMatchCriterion(LongMatchCriterion pCriterion) {
            return pCriterion.getName() + " " + pCriterion.getType().toString() + " \"" + pCriterion.getValue() + "\"";
        }

        @Override
        public String visitStringMatchAnyCriterion(StringMatchAnyCriterion pCriterion) {
            return pCriterion.getName() + " IN "
                    + Arrays.stream(pCriterion.getValue()).collect(Collectors.joining("\", \"", "(\"", "\")"));
        }

        @Override
        public <U extends Comparable<? super U>> String visitRangeCriterion(RangeCriterion<U> pCriterion) {
            StringBuilder buf = new StringBuilder(pCriterion.getName());
            buf.append(" ∈ { x / ");
            // for all comparisons
            String ranges = pCriterion.getValueComparisons().stream().sorted().map(valueComp -> {
                String op;
                switch (valueComp.getOperator()) {
                    case GREATER:
                        op = "x > ";
                        break;
                    case GREATER_OR_EQUAL:
                        op = "x ≥ ";
                        break;
                    case LESS:
                        op = "x < ";
                        break;
                    case LESS_OR_EQUAL:
                        op = "x ≤ ";
                        break;
                    default:
                        op = "";
                }
                return op + valueComp.getValue();
            }).collect(Collectors.joining(", "));
            buf.append(ranges).append(" }");
            return buf.toString();
        }

        @Override
        public String visitDateRangeCriterion(DateRangeCriterion pCriterion) {
            StringBuilder buf = new StringBuilder(pCriterion.getName());
            buf.append(" ∈ { x / ");
            // for all comparisons
            String ranges = pCriterion.getValueComparisons().stream()
                    .sorted((a, b) -> a.getValue().compareTo(b.getValue())).map(valueComp -> {
                        String op;
                        switch (valueComp.getOperator()) {
                            case GREATER:
                                op = "x > ";
                                break;
                            case GREATER_OR_EQUAL:
                                op = "x ≥ ";
                                break;
                            case LESS:
                                op = "x < ";
                                break;
                            case LESS_OR_EQUAL:
                                op = "x ≤ ";
                                break;
                            default:
                                op = "";
                        }
                        return op + LocalDateTimeAdapter.format(valueComp.getValue());
                    }).collect(Collectors.joining(", ", "{ ", " }"));
            buf.append(ranges);
            return buf.toString();
        }

        @Override
        public String visitBooleanMatchCriterion(BooleanMatchCriterion pCriterion) {
            return pCriterion.getName() + " IS " + (pCriterion.getValue() ? "TRUE" : "FALSE");
        }
    }
}
//CHECKSTYLE:ON
