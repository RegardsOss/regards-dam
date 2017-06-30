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
    private Double[][][] coordinates;

    protected PolygonCriterion(Double[][][] pCoordinates) {
        coordinates = pCoordinates;
    }

    public Double[][][] getCoordinates() {
        return coordinates;
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitPolygonCriterion(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
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
