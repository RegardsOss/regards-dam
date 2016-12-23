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

    public static EnumerationRestriction buildEnumerationRestriction(String... pAcceptableValues) {
        final EnumerationRestriction er = new EnumerationRestriction();
        if (pAcceptableValues != null) {
            for (String val : pAcceptableValues) {
                er.addAcceptableValues(val);
            }
        }
        return er;
    }

    public static FloatRangeRestriction buildFloatRangeRestriction(Double pMin, Double pMax, boolean pMinExcluded,
            boolean pMaxExcluded) {
        final FloatRangeRestriction frr = new FloatRangeRestriction();
        frr.setMin(pMin);
        frr.setMax(pMax);
        frr.setMinExcluded(pMinExcluded);
        frr.setMaxExcluded(pMaxExcluded);
        return frr;
    }

    public static IntegerRangeRestriction buildIntegerRangeRestriction(Integer pMin, Integer pMax, boolean pMinExcluded,
            boolean pMaxExcluded) {
        final IntegerRangeRestriction irr = new IntegerRangeRestriction();
        irr.setMin(pMin);
        irr.setMax(pMax);
        irr.setMinExcluded(pMinExcluded);
        irr.setMaxExcluded(pMaxExcluded);
        return irr;
    }

    public static PatternRestriction buildPatternRestriction(String pPattern) {
        final PatternRestriction pr = new PatternRestriction();
        pr.setPattern(pPattern);
        return pr;
    }
}
