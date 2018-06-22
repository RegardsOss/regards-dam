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
package fr.cnes.regards.modules.entities.service.visitor;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.indexer.domain.criterion.AbstractMultiCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.AbstractPropertyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.BooleanMatchCriterion;
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
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;

/**
 * Visitor to check if a {@link ICriterion} can be accepted as a subsetting filter in {@link Dataset}. <b>The aim is not
 * to execute the filter but to check if the filter is coherent.</b> For example, the visit of
 * NotCriterion(subCriterion) leads to the visit of subcriterion (because the NotCriterion is coherent)
 * @author Sylvain Vissiere-Guerinet
 */
public class SubsettingCoherenceVisitor implements ICriterionVisitor<Boolean> {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubsettingCoherenceVisitor.class);

    private static final String NOT_HANDLED_STATIC_PROPERTY = "Static attribute %s cannot be used as subsetting clause";

    /**
     * Attribute does not exist message format
     */
    private static final String ATTRIBUTE_DOES_NOT_EXIST = "Attribute of name : %s could not be found in the database";

    /**
     * Attribute is not coherent message format
     */
    private static final String ATTRIBUTE_IS_NOT_COHERENT = "Attribute of name : %s is not an attribute of the model : %s";

    /**
     * Reference model
     */
    private final Model referenceModel;

    /**
     * {@link IAttributeModelService} instance
     */
    private final IAttributeModelService attributeService;

    /**
     * {@link IModelAttrAssocService} instance
     */
    private final IModelAttrAssocService modelAttributeService;

    private final IAttributeFinder attributeFinder;

    /**
     * Constructor
     * @param pModel the model
     * @param pAttributeService the attribute service
     * @param pModelAttributeService the model attribute service
     */
    public SubsettingCoherenceVisitor(Model pModel, IAttributeModelService pAttributeService,
            IModelAttrAssocService pModelAttributeService, IAttributeFinder attributeFinder) {
        referenceModel = pModel;
        attributeService = pAttributeService;
        modelAttributeService = pModelAttributeService;
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
        return (attribute != null) && (attribute.getType().equals(AttributeType.STRING)
                || attribute.getType().equals(AttributeType.STRING_ARRAY));
    }

    @Override
    public Boolean visitStringMatchAnyCriterion(StringMatchAnyCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return (attribute != null) && (attribute.getType().equals(AttributeType.STRING)
                || attribute.getType().equals(AttributeType.STRING_ARRAY));
    }

    @Override
    public Boolean visitIntMatchCriterion(IntMatchCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return (attribute != null) && (attribute.getType().equals(AttributeType.INTEGER));
    }

    @Override
    public Boolean visitLongMatchCriterion(LongMatchCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return (attribute != null) && (attribute.getType().equals(AttributeType.LONG));
    }

    @Override
    public Boolean visitDateMatchCriterion(DateMatchCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return (attribute != null) && (attribute.getType().equals(AttributeType.DATE_ISO8601));
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
        return (attribute != null) && (attribute.getType().equals(AttributeType.DATE_ISO8601));
    }

    @Override
    public Boolean visitBooleanMatchCriterion(BooleanMatchCriterion criterion) {
        AttributeModel attribute = extractAttribute(criterion);
        return (attribute != null) && attribute.getType().equals(AttributeType.BOOLEAN);
    }

    /**
     * extract the {@link AttributeModel} from the criterion if it is possible and check if it is a attribute from the
     * right model
     * @param criterion {@link AbstractPropertyCriterion} from which extract the attribute
     * @return extracted {@link AttributeModel} or null
     */
    private AttributeModel extractAttribute(AbstractPropertyCriterion criterion) {
        try {
            return attributeFinder.findByName(criterion.getName());
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
        return (attribute != null);
    }
}
