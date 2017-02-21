/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.visitor;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.modules.crawler.domain.criterion.AbstractMultiCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.AbstractPropertyCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.BooleanMatchCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.DateRangeCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.EmptyCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterionVisitor;
import fr.cnes.regards.modules.crawler.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.NotCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.StringMatchAnyCriterion;
import fr.cnes.regards.modules.crawler.domain.criterion.StringMatchCriterion;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttributeService;

/**
 * Visitor to check if a {@link ICriterion} can be accepted as a subsetting filter in {@link Dataset}
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class SubsettingCoherenceVisitor implements ICriterionVisitor<Boolean> {

    private static final Logger LOG = LoggerFactory.getLogger(SubsettingCoherenceVisitor.class);

    private static final String ATTRIBUTE_DOES_NOT_EXISTS = "Attribute of name : %s could not be found in the database";

    private static final String ATTRIBUTE_IS_NOT_COHERENT = "Attribute of name : %s is not an attribute of the model : %s";

    private static final String ATTRIBUTE_IS_NOT_QUERYABLE = "Attribute of name : %s of the model : %s is not queryable";

    private final Model referenceModel;

    private final IAttributeModelService attributeService;

    private final IModelAttributeService modelAttributeService;

    /**
     *
     */
    public SubsettingCoherenceVisitor(Model pModel, IAttributeModelService pAttributeService,
            IModelAttributeService pModelAttributeService) {
        referenceModel = pModel;
        attributeService = pAttributeService;
        modelAttributeService = pModelAttributeService;
    }

    @Override
    public Boolean visitAndCriterion(AbstractMultiCriterion pCriterion) {
        boolean result = true;
        Iterator<ICriterion> criterionIterator = pCriterion.getCriterions().iterator();
        while (result && criterionIterator.hasNext()) {
            result &= criterionIterator.next().accept(this);
        }
        return result;
    }

    @Override
    public Boolean visitOrCriterion(AbstractMultiCriterion pCriterion) {
        boolean result = true;
        Iterator<ICriterion> criterionIterator = pCriterion.getCriterions().iterator();
        while (result && criterionIterator.hasNext()) {
            result &= criterionIterator.next().accept(this);
        }
        return result;
    }

    @Override
    public Boolean visitNotCriterion(NotCriterion pCriterion) {
        return pCriterion.accept(this);
    }

    @Override
    public Boolean visitStringMatchCriterion(StringMatchCriterion pCriterion) {
        AttributeModel attribute = extractAttribute(pCriterion);
        // FIXME: log at this level?
        // LOG.info(String.format(CRITERION_IS_COHERENT, result));
        return (attribute != null) && (attribute.getType().equals(AttributeType.STRING)
                || attribute.getType().equals(AttributeType.STRING_ARRAY));
    }

    @Override
    public Boolean visitStringMatchAnyCriterion(StringMatchAnyCriterion pCriterion) {
        AttributeModel attribute = extractAttribute(pCriterion);
        // FIXME: log at this level?
        // LOG.info(String.format(CRITERION_IS_COHERENT, result));
        return (attribute != null) && (attribute.getType().equals(AttributeType.STRING)
                || attribute.getType().equals(AttributeType.STRING_ARRAY));
    }

    @Override
    public Boolean visitIntMatchCriterion(IntMatchCriterion pCriterion) {
        AttributeModel attribute = extractAttribute(pCriterion);
        // FIXME: log at this level?
        // LOG.info(String.format(CRITERION_IS_COHERENT, result));
        return (attribute != null) && (attribute.getType().equals(AttributeType.INTEGER)
                || attribute.getType().equals(AttributeType.INTEGER_ARRAY)
                || attribute.getType().equals(AttributeType.INTEGER_INTERVAL));
    }

    @Override
    public <U> Boolean visitRangeCriterion(RangeCriterion<U> pCriterion) {
        AttributeModel attribute = extractAttribute(pCriterion);
        return null;
    }

    @Override
    public Boolean visitDateRangeCriterion(DateRangeCriterion pCriterion) {
        AttributeModel attribute = extractAttribute(pCriterion);
        return (attribute != null) && (attribute.getType().equals(AttributeType.DATE_ARRAY)
                || attribute.getType().equals(AttributeType.DATE_INTERVAL)
                || attribute.getType().equals(AttributeType.DATE_ISO8601));
    }

    @Override
    public Boolean visitBooleanMatchCriterion(BooleanMatchCriterion pCriterion) {
        AttributeModel attribute = extractAttribute(pCriterion);
        // FIXME: log at this level?
        // LOG.info(String.format(CRITERION_IS_COHERENT, result));
        return (attribute != null) && attribute.getType().equals(AttributeType.BOOLEAN);
    }

    /**
     * extract the {@link AttributeModel} from the criterion if it is possible and check if it is a attribute from the
     * right model and it is queryable
     *
     * @param pCriterion
     *            {@link AbstractPropertyCriterion} from which extract the attribute
     * @return extracted {@link AttributeModel} or null
     */
    private AttributeModel extractAttribute(AbstractPropertyCriterion pCriterion) {
        // contains attributes.attributeFullname
        String attributeFullName = pCriterion.getName();
        // remove the "attributes."
        int indexOfPoint = attributeFullName.indexOf('.');
        attributeFullName = attributeFullName.substring(indexOfPoint + 1);
        indexOfPoint = attributeFullName.indexOf('.');
        AttributeModel attribute;
        if (indexOfPoint == -1) {
            // represented attribute does not belong to a fragment, so attributeFullName is the attributeName
            attribute = attributeService.findByNameAndFragmentName(attributeFullName, null);
        } else {
            // represented attribute does belong to a fragment, so lets extract the attributeName and fragmentName
            String attributeName = attributeFullName.substring(indexOfPoint + 1);
            String fragmentName = attributeFullName.substring(0, indexOfPoint);
            attribute = attributeService.findByNameAndFragmentName(attributeName, fragmentName);
        }
        if (attribute == null) {
            // attributeName is unknown
            LOG.error(String.format(ATTRIBUTE_DOES_NOT_EXISTS, attributeFullName));
            return null;
        }
        if (!attribute.isQueryable()) {
            LOG.error(String.format(ATTRIBUTE_IS_NOT_QUERYABLE, attributeFullName, referenceModel.getName()));
            return null;
        }
        ModelAttribute modelAttribute = modelAttributeService.getModelAttribute(referenceModel.getId(), attribute);
        if (modelAttribute == null) {
            // attribute is not one of the model
            LOG.info(String.format(ATTRIBUTE_IS_NOT_COHERENT, attributeFullName, referenceModel.getName()));
            return null;
        }
        return attribute;
    }

    @Override
    public Boolean visitEmptyCriterion(EmptyCriterion pCriterion) {
        return true;
    }
}
