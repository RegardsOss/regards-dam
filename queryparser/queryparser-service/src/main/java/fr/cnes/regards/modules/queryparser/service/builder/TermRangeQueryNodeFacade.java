/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.queryparser.service.builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;

import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;

/**
 * Facade class wrapping a {@link TermRangeQueryNode} for simplifiying access to usefull parameters.
 *
 * @author Xavier-Alexandre Brochard
 */
class TermRangeQueryNodeFacade {

    private final String field;

    private final boolean isLowerInclusive;

    private final boolean isUpperInclusive;

    private final String lowerBound;

    private final String upperBound;

    /**
     * Constructor
     */
    public TermRangeQueryNodeFacade(TermRangeQueryNode pTermRangeQueryNode) {
        super();
        field = StringUtils.toString(pTermRangeQueryNode.getField());
        lowerBound = pTermRangeQueryNode.getLowerBound().getTextAsString();
        upperBound = pTermRangeQueryNode.getUpperBound().getTextAsString();
        isLowerInclusive = pTermRangeQueryNode.isLowerInclusive();
        isUpperInclusive = pTermRangeQueryNode.isUpperInclusive();
    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * @return the lowerBound
     */
    public String getLowerBound() {
        return lowerBound;
    }

    /**
     * @return the lower bound parsed as {@link Double}
     */
    public Double getLowerBoundAsDouble() {
        return Double.parseDouble(lowerBound);
    }

    /**
     * @return the lower bound parsed as {@link Integer}
     */
    public Integer getLowerBoundAsInteger() {
        return Integer.parseInt(lowerBound);
    }

    /**
     * @return the lower bound parsed as {@link LocalDateTime}
     */
    public OffsetDateTime getLowerBoundAsDateTime() {
        return OffsetDateTimeAdapter.parse(lowerBound);
    }

    /**
     * @return the lower bound parsed as {@link Long}
     */
    public Long getLowerBoundAsLong() {
        return Long.parseLong(lowerBound);
    }

    /**
     * @return the upperBound
     */
    public String getUpperBound() {
        return upperBound;
    }

    /**
     * @return the upper bound parsed as {@link Double}
     */
    public Double getUpperBoundAsDouble() {
        return Double.parseDouble(upperBound);
    }

    /**
     * @return the upper bound parsed as {@link Integer}
     */
    public Integer getUpperBoundAsInteger() {
        return Integer.parseInt(upperBound);
    }

    /**
     * @return the upper bound parsed as {@link LocalDateTime}
     */
    public OffsetDateTime getUpperBoundAsDateTime() {
        return OffsetDateTimeAdapter.parse(upperBound);
    }

    /**
     * @return the upper bound parsed as {@link Long}
     */
    public Long getUpperBoundAsLong() {
        return Long.parseLong(upperBound);
    }

    /**
     * @return the isLowerInclusive
     */
    public boolean isLowerInclusive() {
        return isLowerInclusive;
    }

    /**
     * @return the isUpperInclusive
     */
    public boolean isUpperInclusive() {
        return isUpperInclusive;
    }

}