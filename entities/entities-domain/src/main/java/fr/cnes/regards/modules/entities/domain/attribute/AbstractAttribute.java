/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

/**
 * @param <T>
 *            attribute type
 * @author Marc Sordi
 *
 */
public abstract class AbstractAttribute<T> implements IAttribute<T> {

    /**
     * Attribute name
     */
    private String name;

    /**
     * Attribute value
     */
    private T value;

    @Override
    public T getValue() {
        return value;
    }

    public void setValue(T pValue) {
        value = pValue;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

}
