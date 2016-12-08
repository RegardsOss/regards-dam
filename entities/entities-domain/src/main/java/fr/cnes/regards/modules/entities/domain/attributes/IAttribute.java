/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attributes;

/**
 *
 * @param <T>
 *            type of Attribute
 *
 * @author lmieulet
 * @author Marc Sordi
 *
 */
public interface IAttribute<T> {

    /**
     * Get fragment name. Equivalent to name space.
     *
     * @return fragment name
     */
    String getFragmentName();

    /**
     * Get attribute name
     *
     * @return attribute name
     */
    String getName();

    /**
     * @return the attribute value
     */
    public T getValue();

}
