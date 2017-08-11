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
package fr.cnes.regards.modules.entities.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.UUID;

import fr.cnes.regards.framework.urn.OAISIdentifier;
import fr.cnes.regards.framework.urn.UniformResourceName;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Entity
@DiscriminatorValue("COLLECTION")
public class Collection extends AbstractDescEntity { // NOSONAR

    public Collection(final Model pModel, final String pTenant, final String pLabel) {
        super(pModel, new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, pTenant, UUID.randomUUID(), 1),
              pLabel);
    }

    public Collection() {
        // we use super and not this because at deserialization we need a ipId null at the object creation which is then
        // replaced by the attribute if present or added by creation method
        super(null, null, null);
    }

    @Override
    public String getType() {
        return EntityType.COLLECTION.toString();
    }
}
