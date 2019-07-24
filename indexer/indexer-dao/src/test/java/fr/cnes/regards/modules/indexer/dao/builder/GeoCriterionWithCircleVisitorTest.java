/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.indexer.dao.builder;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.util.Pair;

import fr.cnes.regards.modules.indexer.dao.EsHelper;
import fr.cnes.regards.modules.indexer.domain.criterion.NotCriterion;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;
import fr.cnes.regards.modules.indexer.domain.criterion.AndCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.CircleCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.OrCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchAnyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.exception.InvalidGeometryException;

/**
 * @author oroussel
 */
public class GeoCriterionWithCircleVisitorTest {

    @Test
    public void onlyCircleCriterionTest() {
        // Geo circle visitor on Mars
        GeoCriterionWithCircleVisitor visitor = new GeoCriterionWithCircleVisitor(Crs.MARS_49900);
        ICriterion criterion;
        Pair<ICriterion, ICriterion> resultPairCrit;
        ICriterion resultFirstCrit;
        ICriterion resultSecondCrit;

        // Criterion with center point on equator => min and max distance are equal
        criterion = ICriterion.intersectsCircle(new double[] { 0.0, 0.0 }, "100km");
        resultPairCrit = criterion.accept(visitor);

        resultFirstCrit = resultPairCrit.getFirst();
        resultSecondCrit = resultPairCrit.getSecond();
        Assert.assertEquals(resultFirstCrit, resultSecondCrit);

        // Criterion with center on north pole => min and max distance are equal
        criterion = ICriterion.intersectsCircle(new double[] { 0.0, 90.0 }, "100km");
        resultPairCrit = criterion.accept(visitor);

        resultFirstCrit = resultPairCrit.getFirst();
        resultSecondCrit = resultPairCrit.getSecond();
        Assert.assertEquals(resultFirstCrit, resultSecondCrit);

        // Criterion with center on south pole => min and max distance are equal
        criterion = ICriterion.intersectsCircle(new double[] { 0.0, -90.0 }, "100km");
        resultPairCrit = criterion.accept(visitor);

        resultFirstCrit = resultPairCrit.getFirst();
        resultSecondCrit = resultPairCrit.getSecond();
        Assert.assertEquals(resultFirstCrit, resultSecondCrit);

        // Criterion with center at 45, 45 => min and maxu must be different
        criterion = ICriterion.intersectsCircle(new double[] { 45.0, 45.0 }, "100km");
        resultPairCrit = criterion.accept(visitor);

        resultFirstCrit = resultPairCrit.getFirst();
        resultSecondCrit = resultPairCrit.getSecond();
        Assert.assertNotEquals(resultFirstCrit, resultSecondCrit);
        // Let's find second circle criterion (exterior one)
        CircleCriterion secondCircleCriterion = (CircleCriterion) ((AndCriterion) resultSecondCrit).getCriterions()
                .get(1);
        Assert.assertTrue(Double.valueOf(((CircleCriterion) resultFirstCrit).getRadius()) < Double
                .valueOf(secondCircleCriterion.getRadius()));
    }

    @Test
    public void criterionWithCircleTest() {
        // Geo cricle visitor on Mars
        GeoCriterionWithCircleVisitor visitor = new GeoCriterionWithCircleVisitor(Crs.MARS_49900);
        ICriterion criterion = ICriterion.and(ICriterion.in("toto", "text1", "text2"), ICriterion.eq("count", 25),
                                              ICriterion.or(ICriterion.ge("altitude", 2552.36), ICriterion
                                                      .intersectsCircle(new double[] { 45, 45 }, "50m")));

        Pair<ICriterion, ICriterion> resultPairCrit = criterion.accept(visitor);

        ICriterion resultFirstCrit = resultPairCrit.getFirst();
        ICriterion resultSecondCrit = resultPairCrit.getSecond();

        Assert.assertTrue(resultFirstCrit instanceof AndCriterion);
        AndCriterion firstAndCriterion = (AndCriterion) resultFirstCrit;
        Assert.assertTrue(firstAndCriterion.getCriterions().get(0) instanceof StringMatchAnyCriterion);
        Assert.assertTrue(firstAndCriterion.getCriterions().get(1) instanceof IntMatchCriterion);
        Assert.assertTrue(firstAndCriterion.getCriterions().get(2) instanceof OrCriterion);
        OrCriterion firstOrCriterion = (OrCriterion) firstAndCriterion.getCriterions().get(2);
        Assert.assertTrue(firstOrCriterion.getCriterions().get(0) instanceof RangeCriterion);
        Assert.assertTrue(firstOrCriterion.getCriterions().get(1) instanceof CircleCriterion);
        CircleCriterion firstCircleCriterion = (CircleCriterion) firstOrCriterion.getCriterions().get(1);
        System.out.println(Arrays.toString(firstCircleCriterion.getCoordinates()));
        Assert.assertArrayEquals(new double[] { 45.0, 45.15141819898613 }, firstCircleCriterion.getCoordinates(),
                                 0.000001);

        Assert.assertTrue(resultSecondCrit instanceof AndCriterion);
        AndCriterion secondAndCriterion = (AndCriterion) resultSecondCrit;
        Assert.assertTrue(secondAndCriterion.getCriterions().get(0) instanceof StringMatchAnyCriterion);
        Assert.assertTrue(secondAndCriterion.getCriterions().get(1) instanceof IntMatchCriterion);
        Assert.assertTrue(secondAndCriterion.getCriterions().get(2) instanceof OrCriterion);
        OrCriterion secondOrCriterion = (OrCriterion) secondAndCriterion.getCriterions().get(2);
        Assert.assertTrue(secondOrCriterion.getCriterions().get(0) instanceof RangeCriterion);
        Assert.assertTrue(secondOrCriterion.getCriterions().get(1) instanceof AndCriterion);
        AndCriterion secondAndCirclesCriterion = (AndCriterion) secondOrCriterion.getCriterions().get(1);

        Assert.assertTrue(secondAndCirclesCriterion.getCriterions().get(0) instanceof NotCriterion);
        NotCriterion secondNotCircleCriterion = (NotCriterion) secondAndCirclesCriterion.getCriterions().get(0);
        Assert.assertTrue(secondNotCircleCriterion.getCriterion() instanceof CircleCriterion);
        Assert.assertEquals(firstCircleCriterion, secondNotCircleCriterion.getCriterion());

        Assert.assertTrue(secondAndCirclesCriterion.getCriterions().get(1) instanceof CircleCriterion);

        CircleCriterion secondCircleCriterion = (CircleCriterion) secondAndCirclesCriterion.getCriterions().get(1);
        System.out.println(Arrays.toString(secondCircleCriterion.getCoordinates()));
        Assert.assertArrayEquals(new double[] { 45.0, 45.15141819898613 }, secondCircleCriterion.getCoordinates(),
                                 0.000001);

        Assert.assertTrue(EsHelper.toMeters(firstCircleCriterion.getRadius()) < EsHelper
                .toMeters(secondCircleCriterion.getRadius()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void visitorForbiddenCrsTest() {
        GeoCriterionWithCircleVisitor visitor = new GeoCriterionWithCircleVisitor(Crs.WGS_84);
    }

    @Test(expected = IllegalArgumentException.class)
    public void visitorForbiddenPolygonCriterionTest() {
        GeoCriterionWithCircleVisitor visitor = new GeoCriterionWithCircleVisitor(Crs.WGS_84);
        ICriterion criterion = ICriterion.intersectsPolygon(new double[][][] {});
        criterion.accept(visitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void visitorForbiddenBoundingBoxCriterionTest() throws InvalidGeometryException {
        GeoCriterionWithCircleVisitor visitor = new GeoCriterionWithCircleVisitor(Crs.WGS_84);
        ICriterion criterion = ICriterion.intersectsBbox("0.0, -10, 10., 1e+1");
        criterion.accept(visitor);
    }
}
