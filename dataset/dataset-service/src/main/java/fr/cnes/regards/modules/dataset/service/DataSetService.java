/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataset.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.dataset.dao.IDataSetRepository;
import fr.cnes.regards.modules.dataset.domain.DataSet;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Service
public class DataSetService {

    private final IDataSetRepository repository;

    public DataSetService(IDataSetRepository pRepository) {
        repository = pRepository;
    }

    /**
     * @param pDataSetIpId
     * @return
     * @throws EntityNotFoundException
     */
    public DataSet retrieveDataSet(UniformResourceName pDataSetIpId) throws EntityNotFoundException {
        DataSet result = (DataSet) repository.findOneByIpId(pDataSetIpId);
        if (result == null) {
            throw new EntityNotFoundException(pDataSetIpId.toString(), DataSet.class);
        }
        return result;
    }

    /**
     * @param pDataSetId
     * @return
     */
    public DataSet retrieveDataSet(Long pDataSetId) {
        return repository.findOne(pDataSetId);
    }

    public void associateDataSet(Long pDataSetId, List<AbstractEntity> pToBeAssociatedWith) {
        DataSet source = repository.findOne(pDataSetId);
        for (AbstractEntity target : pToBeAssociatedWith) {

        }
    }

}
