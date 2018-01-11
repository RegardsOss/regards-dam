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
    private Double[] coordinates;

    /**
     * Radius length. Format : a number eventually followed by unit (m, km, ...). In meter by default
     */
    private String radius;

    public CircleCriterion(Double[] coordinates, String radius) {
        this.coordinates = coordinates;
        this.radius = radius;
    }

    public Double[] getCoordinates() {
        return coordinates;
    }

    public String getRadius() {
        return radius;
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
        if (o == null || getClass() != o.getClass()) {
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
        result = 31 * result + ((radius != null) ? radius.hashCode() : 0);
        return result;
    }
}
