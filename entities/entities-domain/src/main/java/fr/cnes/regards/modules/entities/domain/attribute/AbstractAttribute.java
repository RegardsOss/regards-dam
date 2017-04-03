/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * @param <T> attribute type
 * @author Marc Sordi
 */
public abstract class AbstractAttribute<T> implements IAttribute<T> {

    /**
     * Attribute name
     */
    @NotNull
    private String name;

    /**
     * Attribute value
     */
    @NotNull
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

    @Override
    public String toString() {
        return name + " : " + value.toString();
    }

    public abstract boolean represents(AttributeType pAttributeType);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (pObj == null) {
            return false;
        }
        if (getClass() != pObj.getClass()) {
            return false;
        }
        AbstractAttribute other = (AbstractAttribute) pObj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else
            if (!name.equals(other.name)) {
                return false;
            }
        return true;
    }

}
