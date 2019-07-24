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
package fr.cnes.regards.modules.indexer.domain.criterion;

import java.util.Arrays;

/**
 * Geometric polygon criterion.
 * @author oroussel
 */
public class PolygonCriterion implements ICriterion {

    /**
     * Polygon coordinates
     */
    private final double[][][] coordinates;

    protected PolygonCriterion(double[][][] coordinates) {
        this.coordinates = coordinates;
    }

    public double[][][] getCoordinates() {
        return coordinates;
    }

    @Override
    public ICriterion copy() {
        return new PolygonCriterion(this.coordinates.clone());
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitPolygonCriterion(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        PolygonCriterion that = (PolygonCriterion) o;
        return Arrays.deepEquals(coordinates, that.coordinates);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(coordinates);
    }
}
