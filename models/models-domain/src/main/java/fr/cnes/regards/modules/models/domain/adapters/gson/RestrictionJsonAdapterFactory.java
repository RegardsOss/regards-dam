/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.adapters.gson;

import fr.cnes.regards.framework.gson.adapters.PolymorphicTypeAdapterFactory;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.DoubleRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionType;

/**
 *
 * Restriction adapter
 *
 * @author Marc Sordi
 *
 */
public class RestrictionJsonAdapterFactory extends PolymorphicTypeAdapterFactory<AbstractRestriction> {

    protected RestrictionJsonAdapterFactory() {
        super(AbstractRestriction.class, "type");
        registerSubtype(EnumerationRestriction.class, RestrictionType.ENUMERATION);
        registerSubtype(PatternRestriction.class, RestrictionType.PATTERN);
        registerSubtype(DoubleRangeRestriction.class, RestrictionType.DOUBLE_RANGE);
        registerSubtype(IntegerRangeRestriction.class, RestrictionType.INTEGER_RANGE);
    }
}
