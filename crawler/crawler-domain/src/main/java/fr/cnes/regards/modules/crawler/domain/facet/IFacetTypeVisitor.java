package fr.cnes.regards.modules.crawler.domain.facet;

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
