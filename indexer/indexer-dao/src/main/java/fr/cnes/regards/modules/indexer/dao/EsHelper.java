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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

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

    /**
     * Transform Distance with units value to meters
     * @param value distance value with units (valid units are in, inch, yd, yard, mi, miles, km, kilometers, m,meters,
     * cm,centimeters, mm, millimeters)
     * @return distance in meters
     */
    public static double toMeters(String value) {
        Pattern p = Pattern.compile("^([\\d]*\\.?[\\d]*\\d(?:e-?\\d+)?)\\s*([iymkc]\\S*)?$");
        Matcher m = p.matcher(value.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Bad value with unit (" + value + ")");
        }
        String unit = Strings.nullToEmpty(m.group(2));
        String numeric = m.group(1);
        double factor;
        switch (unit) {
            case "in":
            case "inch":
                factor = 0.0254;
                break;
            case "yd":
            case "yard":
                factor = 0.9144;
                break;
            case "mi":
            case "miles":
                factor = 1609.34;
                break;
            case "km":
            case "kilometers":
                factor = 1000;
                break;
            case "":
            case "m":
            case "meters":
                factor = 1;
                break;
            case "cm":
            case "centimeters":
                factor = 0.01;
                break;
            case "mm":
            case "millimeters":
                factor = 0.001;
                break;
            default:
                throw new IllegalArgumentException("Bad unit ! (" + unit + ")");
        }
        return Double.parseDouble(numeric) * factor;
    }
}
