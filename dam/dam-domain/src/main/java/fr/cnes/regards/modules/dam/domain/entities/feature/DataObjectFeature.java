/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.domain.entities.feature;

import java.util.UUID;

import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;

/**
 * Specific feature properties for data objects
 * @author Marc Sordi
 *
 */
public class DataObjectFeature extends EntityFeature {

    /**
     * Deserialization constructor
     */
    protected DataObjectFeature() {
        super(null, null, EntityType.DATA, null);
    }

    public DataObjectFeature(UniformResourceName id, String providerId, String label) {
        super(id, providerId, EntityType.DATA, label);
    }

    public DataObjectFeature(String tenant, String providerId, String label) {
        super(new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, tenant, UUID.randomUUID(), 1), providerId,
              EntityType.DATA, label);
    }

}
