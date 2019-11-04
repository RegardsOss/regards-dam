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
package fr.cnes.regards.modules.dam.service.entities.visitor;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.StaticProperties;
import fr.cnes.regards.modules.indexer.domain.criterion.AbstractMultiCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.AbstractPropertyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.BooleanMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.BoundaryBoxCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.CircleCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.DateMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.DateRangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.EmptyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.FieldExistsCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterionVisitor;
import fr.cnes.regards.modules.indexer.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.LongMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.NotCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.PolygonCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchAnyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMultiMatchCriterion;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;

/**
 * Visitor to check if a {@link ICriterion} can be accepted as a subsetting filter in {@link Dataset}. <b>The aim is not
 * to execute the filter but to check if the filter is coherent.</b> For example, the visit of
 * NotCriterion(subCriterion) leads to the visit of subcriterion (because the NotCriterion is coherent)
 * @author Sylvain Vissiere-Guerinet
 */
public class SubsettingCoherenceVisitor implements ICriterionVisitor<Boolean> {

    private static final Logger LOG = LoggerFactory.getLogger(SubsettingCoherenceVisitor.class);

    private final IAttributeFinder attributeFinder;

    public SubsettingCoherenceVisitor(IAttributeFinder attributeFinder) {
        this.attributeFinder = attributeFinder;
    }

    @Override
    public Boolean visitAndCriterion(AbstractMultiCriterion criterion) {
        boolean result = true;
        Iterator<ICriterion> criterionIterator = criterion.getCriterions().iterator();
        while (result && criterionIterator.hasNext()) {
            result &= criterionIterator.next().accept(this);
        }
        return result;
    }

    @Override
    public Boolean visitOrCriterion(AbstractMultiCriterion criterion) {
        boolean result = true;
        Iterator<ICriterion> criterionIterator = criterion.getCriterions().iterator();
        while (result && criterionIterator.hasNext()) {
            result &= criterionIterator.next().accept(this);
        }
        return result;
    }

    @Override
    public Boolean visitNotCriterion(NotCriterion criterion) {
        return criterion.getCriterion().accept(this);
    }

    @Override
    public Boolean visitStringMatchCriterion(StringMatchCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return attribute != null && (attribute.getType().equals(PropertyType.STRING)
                || attribute.getType().equals(PropertyType.STRING_ARRAY));
    }

    @Override
    public Boolean visitStringMultiMatchCriterion(StringMultiMatchCriterion criterion) {
        return false;
    }

    @Override
    public Boolean visitStringMatchAnyCriterion(StringMatchAnyCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return attribute != null && (attribute.getType().equals(PropertyType.STRING)
                || attribute.getType().equals(PropertyType.STRING_ARRAY));
    }

    @Override
    public Boolean visitIntMatchCriterion(IntMatchCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return attribute != null && attribute.getType().equals(PropertyType.INTEGER);
    }

    @Override
    public Boolean visitLongMatchCriterion(LongMatchCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return attribute != null && attribute.getType().equals(PropertyType.LONG);
    }

    @Override
    public Boolean visitDateMatchCriterion(DateMatchCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return attribute != null && attribute.getType().equals(PropertyType.DATE_ISO8601);
    }

    @Override
    public <U extends Comparable<? super U>> Boolean visitRangeCriterion(RangeCriterion<U> criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        if (attribute == null) {
            return false;
        }
        switch (attribute.getType()) {
            case DOUBLE:
            case INTEGER:
            case LONG:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Boolean visitDateRangeCriterion(DateRangeCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return attribute != null && attribute.getType().equals(PropertyType.DATE_ISO8601);
    }

    @Override
    public Boolean visitBooleanMatchCriterion(BooleanMatchCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return attribute != null && attribute.getType().equals(PropertyType.BOOLEAN);
    }

    /**
     * extract the {@link AttributeModel} from the criterion if it is possible and check if it is a attribute from the
     * right model
     * @param criterion {@link AbstractPropertyCriterion} from which extract the attribute
     * @return extracted {@link AttributeModel} or null
     */
    private AttributeModel extractAttribute(AbstractPropertyCriterion criterion) {
        try {
            // At this time, query has been already parsed so criterion is fully qualified.
            // Remove feature namespace to allow finder to match attributes
            return attributeFinder.findByName(criterion.getName().substring(StaticProperties.FEATURE_NS.length()));
        } catch (OpenSearchUnknownParameter e) {
            LOG.error("Inconsistent property {} in subsetting clause", criterion.getName());
            return null;
        }
    }

    @Override
    public Boolean visitEmptyCriterion(EmptyCriterion criterion) {
        return true;
    }

    @Override
    public Boolean visitPolygonCriterion(PolygonCriterion criterion) {
        return true;
    }

    @Override
    public Boolean visitBoundaryBoxCriterion(BoundaryBoxCriterion criterion) {
        return true;
    }

    @Override
    public Boolean visitCircleCriterion(CircleCriterion criterion) {
        return true;
    }

    /**
     * Into context of subsetting dataset filter criterion, only model attributes should be concerned, not static
     * entities properties so criterion attribute should be a model one
     */
    @Override
    public Boolean visitFieldExistsCriterion(FieldExistsCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return attribute != null;
    }

}
