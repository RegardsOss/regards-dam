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
package fr.cnes.regards.modules.dam.service.dataaccess;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessRight;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.dto.DatasetWithAccessRight;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.service.entities.IDatasetService;

/**
 * Service to search for {@link Dataset}s associated with their {@link AccessRight}
 * @author Sébastien Binda
 */
@Service
@MultitenantTransactional
public class DatasetWithAccessRightService implements IDatasetWithAccessRightService {

    @Autowired
    private IDatasetService datasetService;

    @Autowired
    private IAccessRightService accessRightService;

    @Override
    public Page<DatasetWithAccessRight> search(String datasetLabelFilter, String accessGroupName, Pageable pageRequest)
            throws ModuleException {

        // Initialize set to keep datasets order by label
        LinkedHashSet<DatasetWithAccessRight> datasetsWithAR = Sets.newLinkedHashSet();

        // 1. Search for datasets
        // NOTE : New pageRequest to avoid Sort. Sort is forced in the JPA specification to sort by label
        Page<Dataset> datasets = datasetService
                .search(datasetLabelFilter, PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize()));

        // 2. For each dataset of the result page, retrieve the associated AccessRight
        for (Dataset ds : datasets.getContent()) {
            DatasetWithAccessRight datasetWithAR = new DatasetWithAccessRight(ds, null);
            try {
                // There is only one possible AccessRight for a given dataset and a given accessGroup.
                // We set the pagination to 1 element max
                Page<AccessRight> accessRights = accessRightService
                        .retrieveAccessRights(accessGroupName, ds.getIpId(),
                                              PageRequest.of(0, 1, Sort.by(Direction.ASC, "id")));
                if (accessRights.hasContent()) {
                    datasetWithAR.setAccessRight(accessRights.getContent().get(0));
                }
            } catch (EntityNotFoundException e) {
                // Nothing to do.
            }
            datasetsWithAR.add(datasetWithAR);
        }

        return new PageImpl<>(datasetsWithAR.stream().collect(Collectors.toList()), pageRequest,
                datasets.getTotalElements());
    }

}
