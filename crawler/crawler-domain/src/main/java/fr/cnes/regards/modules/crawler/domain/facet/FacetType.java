package fr.cnes.regards.modules.crawler.domain.facet;

/**
 * Facet types.</br>
 * All enumerated values can be visited (providing a String argument)
 * @author oroussel
 */
public enum FacetType {
    // CHECKSTYLE:OFF
    DATE {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> pVisitor, Object... pArgs) {
            return pVisitor.visitDateFacet(pArgs);
        }
    },
    NUMERIC {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> pVisitor, Object... pArgs) {
            return pVisitor.visitNumericFacet(pArgs);
        }
    },
    RANGE {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> pVisitor, Object... pArgs) {
            return pVisitor.visitRangeFacet(pArgs);
        }
    },
    STRING {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> pVisitor, Object... pArgs) {
            return pVisitor.visitStringFacet(pArgs);
        }
    };
    // CHECKSTYLE:OFF

    abstract public <T> T accept(IFacetTypeVisitor<T> visitor, Object... pArgs);
}
