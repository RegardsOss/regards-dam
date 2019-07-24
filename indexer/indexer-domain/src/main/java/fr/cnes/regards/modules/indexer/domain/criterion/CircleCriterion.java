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
 * Geometric circle criterion
 * @author oroussel
 */
public class CircleCriterion implements ICriterion {

    /**
     * Center point coordinates
     */
    private final double[] coordinates;

    /**
     * Radius length. Format : a number eventually followed by unit (m, km, ...). In meter by default
     */
    private String radius;

    public CircleCriterion(double[] coordinates, String radius) {
        this.coordinates = coordinates;
        this.radius = radius;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = Double.toString(radius);
    }

    @Override
    public CircleCriterion copy() {
        return new CircleCriterion(this.coordinates.clone(), this.radius);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitCircleCriterion(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        CircleCriterion that = (CircleCriterion) o;

        if (!Arrays.equals(coordinates, that.coordinates)) {
            return false;
        }
        return (radius != null) ? radius.equals(that.radius) : (that.radius == null);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(coordinates);
        result = (31 * result) + ((radius != null) ? radius.hashCode() : 0);
        return result;
    }
}
