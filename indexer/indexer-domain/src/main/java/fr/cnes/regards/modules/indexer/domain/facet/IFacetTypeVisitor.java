package fr.cnes.regards.modules.indexer.domain.facet;

/**
 * FaceType visitor
 * @param <T> visitor return type
 */
public interface IFacetTypeVisitor<T> {

    T visitStringFacet(Object... pArgs);

    T visitDateFacet(Object... pArgs);

    T visitNumericFacet(Object... pArgs);

    T visitRangeFacet(Object... pArgs);
}
