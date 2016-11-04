/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.adapters.gson;

import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.DateISO8601Restriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.FloatRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.GeometryRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionType;
import fr.cnes.regards.modules.models.domain.attributes.restriction.UrlRestriction;

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
        registerSubtype(DateISO8601Restriction.class, RestrictionType.DATE_ISO8601);
        registerSubtype(FloatRangeRestriction.class, RestrictionType.FLOAT_RANGE);
        registerSubtype(IntegerRangeRestriction.class, RestrictionType.INTEGER_RANGE);
        registerSubtype(UrlRestriction.class, RestrictionType.URL);
        registerSubtype(GeometryRestriction.class, RestrictionType.GEOMETRY);
    }
}
