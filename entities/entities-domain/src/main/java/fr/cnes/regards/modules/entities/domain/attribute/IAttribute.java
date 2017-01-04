/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

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
