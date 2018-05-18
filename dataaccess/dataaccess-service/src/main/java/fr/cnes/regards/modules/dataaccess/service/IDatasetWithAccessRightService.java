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
package fr.cnes.regards.modules.dataaccess.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.dto.DatasetWithAccessRight;
import fr.cnes.regards.modules.entities.domain.Dataset;

/**
 * Service to handle association between {@link Dataset} and {@link AccessRight} entities.
 * @author Sébastien Binda
 */
public interface IDatasetWithAccessRightService {

    /**
     * Search for {@link AccessRight}s of all {@link Dataset}s matching the filters and the given access group name.
     * @param datasetLabelFilter Filter on dataset label.
     * @param accessGroupName search {@link AccessRight}s of the given access group.
     * @param pageRequest
     * @return {@link DatasetWithAccessRight}
     */
    Page<DatasetWithAccessRight> search(String datasetLabelFilter, String accessGroupName, Pageable pageRequest);

}
