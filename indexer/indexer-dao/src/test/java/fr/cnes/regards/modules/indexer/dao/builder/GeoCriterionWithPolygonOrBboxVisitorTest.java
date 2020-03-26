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
package fr.cnes.regards.modules.indexer.dao.builder;

import org.junit.Assert;
import org.junit.Test;

import fr.cnes.regards.modules.indexer.domain.criterion.BoundaryBoxCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.PolygonCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.exception.InvalidGeometryException;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;

/**
 * @author oroussel
 */
public class GeoCriterionWithPolygonOrBboxVisitorTest {

    @Test
    public void onlyBboxCriterionTest() throws InvalidGeometryException {
        // Geo visitor on Mars
        GeoCriterionWithPolygonOrBboxVisitor visitor = new GeoCriterionWithPolygonOrBboxVisitor(Crs.MARS_49900);
        BoundaryBoxCriterion criterion;
        BoundaryBoxCriterion resultCrit;

        // Simple Bbox centered on equator
        criterion = (BoundaryBoxCriterion) ICriterion.intersectsBbox("0.0, -10.0, 90.0, 10.0");
        resultCrit = (BoundaryBoxCriterion) criterion.accept(visitor);

        // 0 stay 0
        Assert.assertEquals(criterion.getMinX(), resultCrit.getMinX(), 0.000001);
        // 90 stay 90
        Assert.assertEquals(criterion.getMaxX(), resultCrit.getMaxX(), 0.000001);
        // Both latitudes have higher amplitude
        Assert.assertTrue(resultCrit.getMinY() < criterion.getMinY());
        Assert.assertTrue(resultCrit.getMaxY() > criterion.getMaxY());

        // Simple Bbox centered on 45° parallel
        criterion = (BoundaryBoxCriterion) ICriterion.intersectsBbox("0.0, 10.0, 90.0, 80.0");
        resultCrit = (BoundaryBoxCriterion) criterion.accept(visitor);
        // Both latitudes are > (because of flattening of Mars which is higher than Earth one
        Assert.assertTrue(resultCrit.getMinY() > criterion.getMinY());
        Assert.assertTrue(resultCrit.getMaxY() > criterion.getMaxY());
    }

    @Test
    public void onlyPolygonCriteriontest() {
        // Geo visitor on Mars
        GeoCriterionWithPolygonOrBboxVisitor visitor = new GeoCriterionWithPolygonOrBboxVisitor(Crs.MARS_49900);
        PolygonCriterion criterion;
        PolygonCriterion resultCrit;

        criterion = (PolygonCriterion) ICriterion.intersectsPolygon(
                new double[][][] { { { 0.0, 0.0 }, { 90.0, 0.0 }, { 180.0, 45.0 }, { 90.0, 60.0 } }, {} });
        resultCrit = (PolygonCriterion) criterion.accept(visitor);
        Assert.assertEquals(2, resultCrit.getCoordinates().length);
        Assert.assertEquals(0, resultCrit.getCoordinates()[1].length);
        Assert.assertEquals(4, resultCrit.getCoordinates()[0].length);
        Assert.assertArrayEquals(new double[] { 0.0, 0.0 }, resultCrit.getCoordinates()[0][0], 0.00001);
        Assert.assertArrayEquals(new double[] { 90.0, 0.0 }, resultCrit.getCoordinates()[0][1], 0.00001);
        Assert.assertArrayEquals(new double[] { -180.0, 45.15141819898613 }, resultCrit.getCoordinates()[0][2], 0.00001);
        Assert.assertArrayEquals(new double[] { 90.0, 60.13055834426036 }, resultCrit.getCoordinates()[0][3], 0.00001);
    }

}
