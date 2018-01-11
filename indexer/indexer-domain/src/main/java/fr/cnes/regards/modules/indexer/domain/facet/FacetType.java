package fr.cnes.regards.modules.indexer.domain.facet;

/**
 * Facet types.</br>
 * All enumerated values can be visited (providing a String argument)
 *
 * @author oroussel
 */
public enum FacetType {
    DATE {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> visitor, Object... args) {
            return visitor.visitDateFacet(args);
        }
    },
    NUMERIC {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> visitor, Object... args) {
            return visitor.visitNumericFacet(args);
        }
    },
    RANGE_DATE {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> visitor, Object... args) {
            return visitor.visitRangeDateFacet(args);
        }
    },
    RANGE_DOUBLE {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> visitor, Object... args) {
            return visitor.visitRangeDoubleFacet(args);
        }
    },
    STRING {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> visitor, Object... args) {
            return visitor.visitStringFacet(args);
        }
    },
    MIN {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> visitor, Object... args) {
            return visitor.visitMinFacet(args);
        }
    },
    MAX {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> visitor, Object... args) {
            return visitor.visitMaxFacet(args);
        }
    };

    public abstract <T> T accept(IFacetTypeVisitor<T> visitor, Object... args);
}

