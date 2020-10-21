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
package fr.cnes.regards.modules.model.domain.attributes;

/**
 *
 * Namespace helper
 *
 * @author Marc Sordi
 *
 * FIXME duplicated from StaticProperties!!!!!
 *
 */
public final class PropertyNamespaces {

    // ##########-EntityFeature-##########

    public static final String FEATURE = "feature";

    /**
     * Feature namespace
     */
    public static final String FEATURE_NS = FEATURE + ".";

    // Wrappped dynamic properties
    public static final String FEATURE_PROPERTIES = "properties";

    public static final String FEATURE_PROPERTIES_PATH = FEATURE_NS + FEATURE_PROPERTIES;

    private PropertyNamespaces() {
    }

}
