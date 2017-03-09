/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

public interface IDatasetService extends IEntityService {

    /**
     * @param pDatasetIpId
     * @return
     * @throws EntityNotFoundException
     */
    Dataset retrieveDataset(UniformResourceName pDatasetIpId) throws EntityNotFoundException;

    /**
     * @param pDatasetId
     * @return
     * @throws EntityNotFoundException
     */
    Dataset retrieveDataset(Long pDatasetId) throws EntityNotFoundException;

    /**
     * @param pPageable
     * @return
     */
    Page<Dataset> retrieveDatasets(Pageable pPageable);

}