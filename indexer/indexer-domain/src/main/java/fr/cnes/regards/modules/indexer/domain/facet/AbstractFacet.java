package fr.cnes.regards.modules.indexer.domain.facet;

/**
 * IFacet facility to manage attribute name
 * @param <T> facet type
 * @author oroussel
 */
public abstract class AbstractFacet<T> implements IFacet<T> {

    /**
     * Concerned attribute name
     */
    private String attributeName;

    public AbstractFacet(String pAttributeName) {
        this.attributeName = pAttributeName;
    }

    @Override
    public String getAttributeName() {
        return this.attributeName;
    }

}