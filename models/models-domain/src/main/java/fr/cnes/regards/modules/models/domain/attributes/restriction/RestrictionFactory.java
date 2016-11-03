/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

/**
 *
 * Restriction factory
 *
 * @author msordi
 *
 */
public final class RestrictionFactory {

    private RestrictionFactory() {
    }

    public static DateISO8601Restriction buildDateISO8601Restriction() {
        return new DateISO8601Restriction();
    }

    public static EnumerationRestriction buildEnumerationRestriction(String... pAcceptableValues) {
        final EnumerationRestriction er = new EnumerationRestriction();
        if (pAcceptableValues != null) {
            for (String val : pAcceptableValues) {
                er.addAcceptableValues(val);
            }
        }
        return er;
    }

    public static FloatRangeRestriction buildFloatRangeRestriction(Float pMinInclusive, Float pMaxInclusive,
            Float pMinExclusive, Float pMaxExclusive) {
        final FloatRangeRestriction frr = new FloatRangeRestriction();
        frr.setMinInclusive(pMinInclusive);
        frr.setMaxInclusive(pMaxInclusive);
        frr.setMinExclusive(pMinExclusive);
        frr.setMaxExclusive(pMaxExclusive);
        return frr;
    }

    public static GeometryRestriction buildGeometryRestriction() {
        return new GeometryRestriction();
    }

    public static IntegerRangeRestriction buildIntegerRangeRestriction(Integer pMinInclusive, Integer pMaxInclusive,
            Integer pMinExclusive, Integer pMaxExclusive) {
        final IntegerRangeRestriction irr = new IntegerRangeRestriction();
        irr.setMinInclusive(pMinInclusive);
        irr.setMaxInclusive(pMaxInclusive);
        irr.setMinExclusive(pMinExclusive);
        irr.setMaxExclusive(pMaxExclusive);
        return irr;
    }

    public static NoRestriction buildNoRestriction() {
        return new NoRestriction();
    }

    public static PatternRestriction buildPatternRestriction(String pPattern) {
        final PatternRestriction pr = new PatternRestriction();
        pr.setPattern(pPattern);
        return pr;
    }

    public static UrlRestriction buildUrlRestriction() {
        return new UrlRestriction();
    }
}
