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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.cnes.regards.modules.indexer.domain.criterion.exception.InvalidGeometryException;

/**
 * Geometric bbox criterion
 * @author Sébastien Binda
 */
public class BoundaryBoxCriterion implements ICriterion {

    private static final Pattern p = Pattern
            .compile("^([0-9]+\\.[0-9]+),([0-9]+\\.[0-9]+),([0-9]+\\.[0-9]+),([0-9]+\\.[0-9]+)$");

    private final Double minX;

    private final Double minY;

    private final Double maxX;

    private final Double maxY;

    public BoundaryBoxCriterion(Double minX, Double minY, Double maxX, Double maxY) {
        super();
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
    }

    /**
     * Creates BoundaryBoxCriterion from string format <left,bottom,right,top> where each field is a {@link Double}.
     * @param bbox
     * @throws InvalidGeometryException
     */
    public BoundaryBoxCriterion(String bbox) throws InvalidGeometryException {
        Matcher m = p.matcher(bbox);
        if (m.matches()) {
            this.minX = Double.valueOf(m.group(1));
            this.minY = Double.valueOf(m.group(2));
            this.maxX = Double.valueOf(m.group(3));
            this.maxY = Double.valueOf(m.group(4));
        } else {
            throw new InvalidGeometryException(
                    String.format("Bbox %s is not a valid bbox format. Expected : minX,minY,maxX,maxY", bbox));
        }
    }

    @Override
    public ICriterion copy() {
        return new BoundaryBoxCriterion(this.maxY, this.minX, this.minY, this.maxX);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitBoundaryBoxCriterion(this);
    }

    public Double getMaxY() {
        return maxY;
    }

    public Double getMinX() {
        return minX;
    }

    public Double getMinY() {
        return minY;
    }

    public Double getMaxX() {
        return maxX;
    }

    @Override
    public int hashCode() {
        return maxY.hashCode() + minX.hashCode() + maxX.hashCode() + minY.hashCode();
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
        return crit.getMinX().equals(this.getMinX()) && crit.getMinY().equals(this.getMinY())
                && crit.getMaxX().equals(this.getMaxX()) && crit.getMaxY().equals(this.getMaxY());
    }

}
