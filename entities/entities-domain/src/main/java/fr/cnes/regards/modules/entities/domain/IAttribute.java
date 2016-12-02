/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

/**
 *
 * @param <T>
 *            type of Attribute
 *
 * @author lmieulet
 *
 */
public interface IAttribute<T> {

    /**
     *
     * @return the attribute name
     */
    public String getFullName();

    /**
     *
     * @return the attribute value
     */
    public T getValue();

}
