package fr.cnes.regards.modules.entities.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

public interface IDatasetService extends IEntityService {

    /**
     * @param pDataSetIpId
     * @return
     * @throws EntityNotFoundException
     */
    DataSet retrieveDataSet(UniformResourceName pDataSetIpId) throws EntityNotFoundException;

    /**
     * @param pDataSetId
     * @return
     * @throws EntityNotFoundException
     */
    DataSet retrieveDataSet(Long pDataSetId) throws EntityNotFoundException;

    /**
     * @param pPageable
     * @return
     */
    Page<DataSet> retrieveDataSets(Pageable pPageable);

    /**
     * @param pDataSetId
     * @return
     * @throws EntityNotFoundException
     */
    // TODO: return only IService not IConverter or IFilter or IProcessingService(not implemented yet anyway)
    List<Long> retrieveDataSetServices(Long pDataSetId) throws EntityNotFoundException;

}