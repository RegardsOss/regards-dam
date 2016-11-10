/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionFactory;

/**
 * @author Marc Sordi
 *
 */
@Service
public class RestrictionService {

    /**
     * List of all available restriction<br/>
     * The list contains an empty instance of each restriction type.
     */
    private List<IRestriction> restrictions;

    @PostConstruct
    public void init() {
        restrictions = new ArrayList<>();
        restrictions.add(RestrictionFactory.buildDateISO8601Restriction());
        restrictions.add(RestrictionFactory.buildEnumerationRestriction());
        restrictions.add(RestrictionFactory.buildFloatRangeRestriction(null, null, null, null));
        restrictions.add(RestrictionFactory.buildGeometryRestriction());
        restrictions.add(RestrictionFactory.buildIntegerRangeRestriction(null, null, null, null));
        restrictions.add(RestrictionFactory.buildPatternRestriction(null));
        restrictions.add(RestrictionFactory.buildUrlRestriction());
    }

    /**
     * Regarding the list of available restriction, this method computes the list of applicable ones for a particular
     * type of attribute.
     *
     * @param pType
     *            attribute type
     * @return list of restriction supported by the attribute type
     */
    public List<String> getRestrictions(AttributeType pType) {
        final List<String> restrictionList = new ArrayList<>();
        for (IRestriction restriction : restrictions) {
            if (restriction.supports(pType) && restriction.isPublic()) {
                restrictionList.add(restriction.getType().name());
            }
        }
        return restrictionList;
    }
}
