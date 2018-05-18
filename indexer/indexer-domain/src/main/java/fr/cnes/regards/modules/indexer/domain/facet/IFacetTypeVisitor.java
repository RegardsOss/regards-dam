package fr.cnes.regards.modules.indexer.domain.facet;

/**
 * FaceType visitor
 * @param <T> visitor return type
 */
public interface IFacetTypeVisitor<T> {

    T visitStringFacet(Object... args);

    T visitBooleanFacet(Object... args);

    T visitDateFacet(Object... args);

    T visitNumericFacet(Object... args);

    T visitRangeDateFacet(Object... args);

    T visitRangeDoubleFacet(Object... args);

    T visitMinFacet(Object... args);

    T visitMaxFacet(Object... args);
}
