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
package fr.cnes.regards.modules.indexer.domain.aggregation;

import org.elasticsearch.search.aggregations.Aggregation;

/**
 * Information about queryable attribute from index with :<ul>
 * <li>attributeName : Name of the attribute in the index</li>
 * <li>aggregation : {@link Aggregation} statistic information about the attribute. Depends on the attribute type.</li>
 * <li>textAttribute : {@link Boolean} does the attribute is a text attribute ?</li>
 * </ul>
 *
 * @author Sébastien Binda
 */
public class QueryableAttribute {

    /**
     * Name of the attribute in the index
     */
    private String attributeName;

    /**
     * Statistic information about the attribute. Depends on the attribute type.
     */
    private Aggregation aggregation;

    /**
     * Does the attribute is a text attribute ?
     */
    private boolean textAttribute = false;

    /**
     * Number of terms to calculate in {@link Aggregation} if the attribute is a text attribute.
     */
    private int termsLimit = 0;

    public QueryableAttribute(String attributeName, Aggregation aggregation, boolean textAttribute, int termsLimit) {
        super();
        this.attributeName = attributeName;
        this.aggregation = aggregation;
        this.textAttribute = textAttribute;
        this.termsLimit = termsLimit;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Aggregation getAggregation() {
        return aggregation;
    }

    public void setAggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
    }

    public boolean isTextAttribute() {
        return textAttribute;
    }

    public void setTextAttribute(boolean textAttribute) {
        this.textAttribute = textAttribute;
    }

    public int getTermsLimit() {
        return termsLimit;
    }

    public void setTermsLimit(int termsLimit) {
        this.termsLimit = termsLimit;
    }

}
