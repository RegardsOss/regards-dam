/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.service.entities;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@PluginInterface(
        description = "Plugin interface which are responsible for storing entities (Collection, Dataset, Document)")
public interface IStorageService {

    /**
     * method to call whenever an {@link AbstractEntity} has changed or is created and modifications has to be
     * communicated to the storage unit
     *
     * @param <T>
     *            one of {@link AbstractEntity} sub class
     * @param toPersist
     *            {@link AbstractEntity} to be persisted
     * @return persisted {@link AbstractEntity}
     */
    <T extends AbstractEntity<?>> T storeAIP(T toPersist);

    /**
     * Delete the aip associated to the given entity
     * @param toDelete
     */
    void deleteAIP(AbstractEntity<?> toDelete);

    /**
     * Update the aip associated to the given entity
     * @param toUpdate
     * @param <T>
     * @return updated aip
     */
    <T extends AbstractEntity<?>> T updateAIP(T toUpdate);

}
