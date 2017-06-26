package fr.cnes.regards.modules.indexer.dao.builder;

import java.io.IOException;
import java.time.OffsetDateTime;

import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilders;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.Coordinate;
import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.modules.indexer.domain.IMapping;
import fr.cnes.regards.modules.indexer.domain.criterion.AbstractMultiCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.BooleanMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.CircleCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.DateRangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.EmptyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterionVisitor;
import fr.cnes.regards.modules.indexer.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.LongMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.NotCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.PolygonCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchAnyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ValueComparison;

/**
 * Criterion visitor implementation to generate Elasticsearch QueryBuilder from
 * a search criterion
 * @author oroussel
 */
public class QueryBuilderCriterionVisitor implements ICriterionVisitor<QueryBuilder> {

    /**
     * Text subfield mapping used for string search criterions
     */
    private static final String KEYWORD = ".keyword";

    @Override
    public QueryBuilder visitEmptyCriterion(EmptyCriterion pCriterion) {
        return QueryBuilders.matchAllQuery();
    }

    @Override
    public QueryBuilder visitAndCriterion(AbstractMultiCriterion pCriterion) {
        BoolQueryBuilder andQueryBuilder = QueryBuilders.boolQuery();
        for (ICriterion criterion : pCriterion.getCriterions()) {
            andQueryBuilder.must(criterion.accept(this));
        }
        return andQueryBuilder;
    }

    @Override
    public QueryBuilder visitOrCriterion(AbstractMultiCriterion pCriterion) {
        BoolQueryBuilder orQueryBuilder = QueryBuilders.boolQuery();
        for (ICriterion criterion : pCriterion.getCriterions()) {
            orQueryBuilder.should(criterion.accept(this));
        }
        return orQueryBuilder;
    }

    @Override
    public QueryBuilder visitNotCriterion(NotCriterion pCriterion) {
        return QueryBuilders.boolQuery().mustNot(pCriterion.getCriterion().accept(this));
    }

    @Override
    public QueryBuilder visitStringMatchCriterion(StringMatchCriterion pCriterion) {
        String searchValue = pCriterion.getValue();
        String attName = pCriterion.getName();
        switch (pCriterion.getType()) {
            case EQUALS:
                return QueryBuilders.matchPhraseQuery(attName, searchValue);
            case STARTS_WITH:
                return QueryBuilders.matchPhrasePrefixQuery(attName, searchValue);
            case ENDS_WITH:
                return QueryBuilders.regexpQuery(attName + KEYWORD, ".*" + searchValue);
            case CONTAINS:
                return QueryBuilders.regexpQuery(attName + KEYWORD, ".*" + searchValue + ".*");
            default:
                return null;
        }
    }

    @Override
    public QueryBuilder visitStringMatchAnyCriterion(StringMatchAnyCriterion pCriterion) {
        return QueryBuilders.matchQuery(pCriterion.getName(), Joiner.on(" ").join(pCriterion.getValue()));
    }

    @Override
    public QueryBuilder visitIntMatchCriterion(IntMatchCriterion pCriterion) {
        return QueryBuilders.termQuery(pCriterion.getName(), pCriterion.getValue());
    }

    @Override
    public QueryBuilder visitLongMatchCriterion(LongMatchCriterion pCriterion) {
        return QueryBuilders.termQuery(pCriterion.getName(), pCriterion.getValue());
    }

    @Override
    public <U extends Comparable<? super U>> QueryBuilder visitRangeCriterion(RangeCriterion<U> pCriterion) {
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(pCriterion.getName());

        for (ValueComparison<U> valueComp : pCriterion.getValueComparisons()) {
            U value = valueComp.getValue();
            switch (valueComp.getOperator()) {
                case GREATER:
                    rangeQueryBuilder.gt(value);
                    break;
                case GREATER_OR_EQUAL:
                    rangeQueryBuilder.gte(value);
                    break;
                case LESS:
                    rangeQueryBuilder.lt(value);
                    break;
                case LESS_OR_EQUAL:
                    rangeQueryBuilder.lte(value);
                    break;
                default:
            }
        }
        return rangeQueryBuilder;
    }

    @Override
    public QueryBuilder visitDateRangeCriterion(DateRangeCriterion pCriterion) {
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(pCriterion.getName());

        for (ValueComparison<OffsetDateTime> valueComp : pCriterion.getValueComparisons()) {
            OffsetDateTime date = valueComp.getValue();
            switch (valueComp.getOperator()) {
                case GREATER:
                    rangeQueryBuilder.gt(OffsetDateTimeAdapter.format(date));
                    break;
                case GREATER_OR_EQUAL:
                    rangeQueryBuilder.gte(OffsetDateTimeAdapter.format(date));
                    break;
                case LESS:
                    rangeQueryBuilder.lt(OffsetDateTimeAdapter.format(date));
                    break;
                case LESS_OR_EQUAL:
                    rangeQueryBuilder.lte(OffsetDateTimeAdapter.format(date));
                    break;
                default:
            }
        }
        return rangeQueryBuilder;
    }

    @Override
    public QueryBuilder visitBooleanMatchCriterion(BooleanMatchCriterion pCriterion) {
        return QueryBuilders.boolQuery().must(QueryBuilders.termQuery(pCriterion.getName(), pCriterion.getValue()));
    }

    @Override
    public QueryBuilder visitCircleCriterion(CircleCriterion criterion) {
        Double[] center = criterion.getCoordinates();
        try {
            return QueryBuilders.geoIntersectionQuery(IMapping.GEOMETRY, ShapeBuilders.newCircleBuilder()
                    .center(new Coordinate(center[0], center[1])).radius(criterion.getRadius()));
        } catch (IOException ioe) { // Never occurs
            throw new RuntimeException(ioe); // NOSONAR
        }
    }

    @Override
    public QueryBuilder visitPolygonCriterion(PolygonCriterion criterion) {
        Double[][][] coordinates = criterion.getCoordinates();
        // Only shell can be taken into account
        Double[][] shell = coordinates[0];
        CoordinatesBuilder coordBuilder = new CoordinatesBuilder();
        for (Double[] point : shell) {
            coordBuilder.coordinate(new Coordinate(point[0], point[1]));
        }
        try {
            return QueryBuilders.geoIntersectionQuery(IMapping.GEOMETRY, ShapeBuilders.newPolygon(coordBuilder));
        } catch (IOException ioe) { // Never occurs
            throw new RuntimeException(ioe); // NOSONAR
        }

    }
}
