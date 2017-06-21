package fr.cnes.regards.modules.indexer.dao;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by oroussel on 21/06/17.
 */
public class TestDouble {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        double d = -15.100000000000009e-12;
        System.out.println(d);
        BigDecimal bd = BigDecimal.valueOf(d);
//        BigDecimal bd = new BigDecimal(d);
        System.out.println(bd);
        System.out.println(bd.scale());
        System.out.println(bd.setScale(12, BigDecimal.ROUND_HALF_UP).doubleValue());
        BigDecimal rounded = bd.round(new MathContext(12));
        System.out.println(rounded.doubleValue());
        System.out.println(System.currentTimeMillis() - start + " ms");
    }



}
