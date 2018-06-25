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
package fr.cnes.regards.modules.indexer.dao;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Helper class used by indexer operations
 * @author oroussel
 * @author Christophe Mertz
 */
public class EsHelper {

    private static final int PRECISION = 3;

    private static final MathContext mathContext = new MathContext(PRECISION);

    private static final MathContext mathContextUp = new MathContext(PRECISION, RoundingMode.CEILING);

    private static final MathContext mathContextDown = new MathContext(PRECISION, RoundingMode.FLOOR);

    private EsHelper() {
    }

    /**
     * 2 decimal scaled double operation
     * @param n value to scale
     * @return 2 decimal digits scaled value
     */
    public static final double scaled(double n) {
        if (!Double.isFinite(n)) {
            return n;
        }
        return BigDecimal.valueOf(n).round(mathContext).doubleValue();
    }

    /**
     * 2 decimal scaled and always increments to the next digit if the parameter value is positive
     * otherwise decreases to the previous digit if the parameter value is negative
     * 3.4902 --> 3.50
     * 3.001  --> 3.01
     * @param n value to scale
     * @return 2 decimal scaled value
     */
    public static final double scaledUp(double n) {
        if (!Double.isFinite(n)) {
            return n;
        }
        return BigDecimal.valueOf(n).round(mathContextUp).doubleValue();
    }

    /**
     * 2 decimal scaled and always decrements to the previous digit if the parameter value is positive
     * otherwise increments to the next digit if the parameter value is negative
     * 3.4902 --> 3.50
     * 3.001  --> 3.01
     * @param n value to scale
     * @return 2 decimal scaled value
     */
    public static final double scaledDown(double n) {
        if (!Double.isFinite(n)) {
            return n;
        }
        return BigDecimal.valueOf(n).round(mathContextDown).doubleValue();
    }

}
