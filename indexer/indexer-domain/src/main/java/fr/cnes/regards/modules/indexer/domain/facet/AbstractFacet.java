package fr.cnes.regards.modules.indexer.domain.facet;

/**
 * IFacet facility to manage attribute name
 *
 * @param <T> facet type
 * @author oroussel
 */
public abstract class AbstractFacet<T> implements IFacet<T> {

    /**
     * Concerned attribute name
     */
    private final String attributeName;

    /**
     * Number of values not covered by facet
     */
    private final long others;

    public AbstractFacet(String attributeName) {
        this(attributeName, 0);
    }

    public AbstractFacet(String attName, long others) {
        this.attributeName = attName;
        this.others = others;
    }

    @Override
    public String getAttributeName() {
        return this.attributeName;
    }

    /**
     * Number of values not covered by facet (0 by default, most of facets cover all values)
     */
    long getOthers() {
        return this.others;
    }
}