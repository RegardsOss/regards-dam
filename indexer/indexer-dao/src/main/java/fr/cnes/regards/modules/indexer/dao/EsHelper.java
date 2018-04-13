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
}
