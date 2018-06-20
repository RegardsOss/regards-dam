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
package fr.cnes.regards.modules.entities.dao.domain;

import java.util.UUID;

import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.entities.domain.feature.EntityFeature;

/**
 * Test feature
 * @author Marc Sordi
 *
 */
public class TestEntityFeature extends EntityFeature {

    /**
     * Deserialization constructor
     */
    public TestEntityFeature() {
        super(null, EntityType.COLLECTION, null);
    }

    public TestEntityFeature(String tenant, String label) {
        super(new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, tenant, UUID.randomUUID(), 1),
              EntityType.COLLECTION, label);
    }
}
