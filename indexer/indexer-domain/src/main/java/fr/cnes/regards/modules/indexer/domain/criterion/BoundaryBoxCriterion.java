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
package fr.cnes.regards.modules.indexer.domain.criterion;

import java.util.Objects;

import fr.cnes.regards.modules.indexer.domain.criterion.exception.InvalidGeometryException;

/**
 * Geometric bbox criterion
 * @author Sébastien Binda
 */
public class BoundaryBoxCriterion implements ICriterion {

    private final double minX;

    private final double minY;

    private double maxX;

    private final double maxY;

    public BoundaryBoxCriterion(double minX, double minY, double maxX, double maxY) {
        super();
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
    }

    /**
     * Creates BoundaryBoxCriterion from string format "left,bottom,right,top" where each field is a double.
     * @param bbox
     * @throws InvalidGeometryException
     */
    public BoundaryBoxCriterion(String bbox) throws InvalidGeometryException {
        String[] values = bbox.split(",");
        try {
            this.minX = Double.parseDouble(values[0].trim());
            this.minY = Double.parseDouble(values[1].trim());
            this.maxX = Double.parseDouble(values[2].trim());
            this.maxY = Double.parseDouble(values[3].trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidGeometryException(
                    String.format("Bbox %s is not a valid bbox format. Expected : minX,minY,maxX,maxY", bbox));
        }
    }

    @Override
    public ICriterion copy() {
        return new BoundaryBoxCriterion(this.minX, this.minY, this.maxX, this.maxY);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitBoundaryBoxCriterion(this);
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    /**
     * This method can be used by QueryBuilderCriterionVisitor to update a > 180 longitude
     * @param maxX
     */
    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, maxX, maxY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        BoundaryBoxCriterion crit = (BoundaryBoxCriterion) o;
        return (crit.getMinX() == this.getMinX()) && (crit.getMinY() == this.getMinY())
                && (crit.getMaxX() == this.getMaxX()) && (crit.getMaxY() == this.getMaxY());
    }

}
