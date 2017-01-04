/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute.value;

/**
 * Represent a value interval
 *
 * @author Marc Sordi
 *
 * @param <T>
 *            value type
 */
public class Interval<T> {

    /**
     * Lower bound value
     */
    private T lowerBound;

    /**
     * Upper bound value
     */
    private T upperBound;

    public T getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(T pLowerBound) {
        lowerBound = pLowerBound;
    }

    public T getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(T pUpperBound) {
        upperBound = pUpperBound;
    }
}
