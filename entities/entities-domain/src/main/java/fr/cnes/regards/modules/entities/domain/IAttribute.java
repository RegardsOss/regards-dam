/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

/**
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
