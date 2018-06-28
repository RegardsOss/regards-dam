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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.dto.DatasetWithAccessRight;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.service.IDatasetService;

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

        List<DatasetWithAccessRight> datasetsWithAR = new ArrayList<>();

        // 1. Search for datasets
        Page<Dataset> datasets = datasetService.search(datasetLabelFilter, pageRequest);

        // 2. For each dataset of the result page, retrieve the associated AccessRight
        for (Dataset ds : datasets.getContent()) {
            DatasetWithAccessRight datasetWithAR = new DatasetWithAccessRight(ds, null);
            try {
                Page<AccessRight> accessRights = accessRightService.retrieveAccessRights(accessGroupName, ds.getIpId(),
                                                                                         pageRequest);
                if (accessRights.hasContent()) {
                    datasetWithAR.setAccessRight(accessRights.getContent().get(0));
                }
            } catch (EntityNotFoundException e) {
                // Nothing to do.
            }
            datasetsWithAR.add(datasetWithAR);
        }

        return new PageImpl<>(datasetsWithAR, pageRequest, datasets.getTotalElements());
    }

}
