/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.model.domain.attributes.restriction;

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

    public static DoubleRangeRestriction buildFloatRangeRestriction(Double pMin, Double pMax, boolean pMinExcluded,
            boolean pMaxExcluded) {
        final DoubleRangeRestriction frr = new DoubleRangeRestriction();
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

    public static LongRangeRestriction buildLongRangeRestriction(Long pMin, Long pMax, boolean pMinExcluded,
            boolean pMaxExcluded) {
        final LongRangeRestriction irr = new LongRangeRestriction();
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
