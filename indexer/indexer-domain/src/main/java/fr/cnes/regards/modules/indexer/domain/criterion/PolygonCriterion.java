package fr.cnes.regards.modules.indexer.domain.criterion;

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

}
