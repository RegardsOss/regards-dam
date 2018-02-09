package fr.cnes.regards.modules.indexer.dao;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Helper class used by indexer operations
 * @author oroussel
 */
public class EsHelper {
    private static final int PRECISION = 3;

    private static final MathContext mathContext = new MathContext(PRECISION);

    private EsHelper() {
    }

    /**
     * 3 decimal scaled double operation
     * @param n value to scale
     * @return 3 decimal digits scaled value
     */
    public static final double scaled(double n) {
        if (!Double.isFinite(n)) {
            return n;
        }
        return BigDecimal.valueOf(n).round(mathContext).doubleValue();
    }
}
