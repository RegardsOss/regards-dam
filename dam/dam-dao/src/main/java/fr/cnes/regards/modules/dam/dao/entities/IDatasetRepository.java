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
package fr.cnes.regards.modules.dam.dao.entities;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.dam.domain.entities.Dataset;

/**
 * Specific requests on Dataset
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Repository
public interface IDatasetRepository extends IAbstractEntityRepository<Dataset> {

    List<Dataset> findByGroups(String group);

    long countByDataModelIn(Set<String> dataModelNames);

    long countByPlgConfDataSourceId(Long pluginConfigurationId);

    /**
     * Check if at least one model is already linked to at least one dataset as a data source model
     * @param dataModelNames model list
     * @return true if no dataset exists linked with at least one model
     */
    default boolean isLinkedToDatasetsAsDataModel(Set<String> dataModelNames) {
        return countByDataModelIn(dataModelNames) > 0;
    }
}
