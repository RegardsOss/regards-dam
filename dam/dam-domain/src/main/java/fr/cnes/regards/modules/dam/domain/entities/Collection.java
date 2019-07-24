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
package fr.cnes.regards.modules.dam.domain.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import fr.cnes.regards.modules.dam.domain.entities.feature.CollectionFeature;
import fr.cnes.regards.modules.dam.domain.models.Model;

/**
 * Collection feature decorator
 *
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 * @author Marc Sordi
 */
@Entity
@DiscriminatorValue("COLLECTION")
public class Collection extends AbstractEntity<CollectionFeature> {

    public Collection(Model model, String tenant, String providerId, String label) {
        super(model, new CollectionFeature(tenant, providerId, label));
    }

    public Collection() {
        // we use super and not this because at deserialization we need a ipId null at the object creation which is then
        // replaced by the attribute if present or added by creation method
        super(null, null);
    }
}
