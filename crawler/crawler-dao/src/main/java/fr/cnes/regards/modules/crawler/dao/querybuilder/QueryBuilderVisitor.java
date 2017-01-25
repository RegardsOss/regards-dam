package fr.cnes.regards.modules.crawler.dao.querybuilder;

import java.time.LocalDateTime;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import com.google.common.base.Joiner;

import fr.cnes.regards.framework.gson.adapters.LocalDateTimeAdapter;
import fr.cnes.regards.modules.crawler.domain.criterion.AbstractMultiCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.DateRangeCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterionVisitor;
import fr.cnes.regards.modules.crawler.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.NotCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.StringMatchAnyCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.StringMatchCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.ValueComparison;

/**
 * Criterion visitor implementation to generate Elasticsearch QueryBuilder from
 * a search criterion
 * @author oroussel
 */
public class QueryBuilderVisitor implements ICriterionVisitor<QueryBuilder> {

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
                return QueryBuilders.regexpQuery(attName, ".*" + searchValue);
            case CONTAINS:
                return QueryBuilders.matchQuery(attName, searchValue);
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
        return QueryBuilders.matchQuery(pCriterion.getName(), pCriterion.getValue());
    }

    @Override
    public <U> QueryBuilder visitRangeCriterion(RangeCriterion<U> pCriterion) {
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

        for (ValueComparison<LocalDateTime> valueComp : pCriterion.getValueComparisons()) {
            LocalDateTime date = valueComp.getValue();
            switch (valueComp.getOperator()) {
                case GREATER:
                    rangeQueryBuilder.gt(LocalDateTimeAdapter.ISO_DATE_TIME_OPTIONAL_OFFSET.format(date));
                    break;
                case GREATER_OR_EQUAL:
                    rangeQueryBuilder.gte(LocalDateTimeAdapter.ISO_DATE_TIME_OPTIONAL_OFFSET.format(date));
                    break;
                case LESS:
                    rangeQueryBuilder.lt(LocalDateTimeAdapter.ISO_DATE_TIME_OPTIONAL_OFFSET.format(date));
                    break;
                case LESS_OR_EQUAL:
                    rangeQueryBuilder.lte(LocalDateTimeAdapter.ISO_DATE_TIME_OPTIONAL_OFFSET.format(date));
                    break;
                default:
            }
        }
        return rangeQueryBuilder;
    }
}
