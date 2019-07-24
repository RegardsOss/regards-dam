/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.indexer.domain.facet;

/**
 * Facet types.</br>
 * All enumerated values can be visited (providing a String argument)
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
    BOOLEAN {

        @Override
        public <T> T accept(IFacetTypeVisitor<T> visitor, Object... args) {
            return visitor.visitBooleanFacet(args);
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
