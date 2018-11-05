/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.opensearch.service.builder;

import java.time.OffsetDateTime;

import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;

import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;

/**
 * Facade class wrapping a {@link TermRangeQueryNode} for simplifiying access to usefull parameters.
 * @author Xavier-Alexandre Brochard
 * @author Marc Sordi
 */
class TermRangeQueryNodeFacade {

    private final String field;

    private final AttributeModel attModel;

    private final boolean isLowerInclusive;

    private final boolean isUpperInclusive;

    private final String lowerBound;

    private final String upperBound;

    /**
     * Constructor
     */
    public TermRangeQueryNodeFacade(TermRangeQueryNode termRangeQueryNode, AttributeModel attModel) {
        super();
        field = StringUtils.toString(termRangeQueryNode.getField());
        lowerBound = termRangeQueryNode.getLowerBound().getTextAsString();
        upperBound = termRangeQueryNode.getUpperBound().getTextAsString();
        isLowerInclusive = termRangeQueryNode.isLowerInclusive();
        isUpperInclusive = termRangeQueryNode.isUpperInclusive();
        this.attModel = attModel;
    }

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
     * @return the lower bound parsed as {@link OffsetDateTime}
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
     * @return the upper bound parsed as {@link OffsetDateTime}
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

    public AttributeModel getAttModel() {
        return attModel;
    }

}