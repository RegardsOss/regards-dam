package fr.cnes.regards.modules.indexer.domain.criterion;

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

    public CircleCriterion(Double[] pCoordinates, String pRadius) {
        coordinates = pCoordinates;
        radius = pRadius;
    }

    public Double[] getCoordinates() {
        return coordinates;
    }

    public String getRadius() {
        return radius;
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitCircleCriterion(this);
    }

}
