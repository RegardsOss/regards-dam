package fr.cnes.regards.modules.indexer.domain.facet;

/**
 * IFacet facility to manage attribute name
 * 
 * @param <T>
 *            facet type
 * @author oroussel
 */
public abstract class AbstractFacet<T> implements IFacet<T> {

    /**
     * Serial
     */
    private static final long serialVersionUID = -5338989406262386574L;

    /**
     * Concerned attribute name
     */
    private final String attributeName;

    public AbstractFacet(String pAttributeName) {
        this.attributeName = pAttributeName;
    }

    @Override
    public String getAttributeName() {
        return this.attributeName;
    }

}