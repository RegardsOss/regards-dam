/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attributes;

/**
 * @param <T>
 *            attribute type
 * @author Marc Sordi
 *
 */
public abstract class AbstractAttribute<T> implements IAttribute<T> {

    /**
     * Fragment name (may be null)
     */
    private String fragmentName;

    /**
     * Attribute name
     */
    private String name;

    @Override
    public String getFragmentName() {
        return fragmentName;
    }

    public void setFragmentName(String pFragmentName) {
        fragmentName = pFragmentName;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

}
