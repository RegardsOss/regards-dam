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
package fr.cnes.regards.modules.indexer.domain.facet.adapters.gson;

import fr.cnes.regards.framework.gson.adapters.PolymorphicTypeAdapterFactory;
import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapterFactory;
import fr.cnes.regards.modules.indexer.domain.facet.BooleanFacet;
import fr.cnes.regards.modules.indexer.domain.facet.DateFacet;
import fr.cnes.regards.modules.indexer.domain.facet.FacetType;
import fr.cnes.regards.modules.indexer.domain.facet.IFacet;
import fr.cnes.regards.modules.indexer.domain.facet.NumericFacet;
import fr.cnes.regards.modules.indexer.domain.facet.StringFacet;

/**
 * Facet adapter factory
 *
 * @author Xavier-Alexandre Brochard
 */
@SuppressWarnings("rawtypes")
@GsonTypeAdapterFactory
public class FacetAdapterFactory extends PolymorphicTypeAdapterFactory<IFacet> {

    /**
     * Constructor
     */
    public FacetAdapterFactory() {
        super(IFacet.class, "type", true);
        registerSubtype(StringFacet.class, FacetType.STRING);
        registerSubtype(NumericFacet.class, FacetType.NUMERIC);
        registerSubtype(DateFacet.class, FacetType.DATE);
        registerSubtype(BooleanFacet.class, FacetType.BOOLEAN);
    }
}
