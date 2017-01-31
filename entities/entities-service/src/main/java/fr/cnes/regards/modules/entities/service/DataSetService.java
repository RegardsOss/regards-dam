/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.datasources.service.DataSourceService;
import fr.cnes.regards.modules.entities.dao.IDataSetRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.service.visitor.SubsettingCoherenceVisitor;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttributeService;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Service
public class DataSetService {

    private final IAttributeModelService attributeService;

    private final IModelAttributeService modelAttributeService;

    private final IDataSetRepository repository;

    private final IEntityService entityService;

    private final DataSourceService dataSourceService;

    public DataSetService(IDataSetRepository pRepository, IAttributeModelService pAttributeService,
            IModelAttributeService pModelAttributeService, IEntityService pEntityService,
            DataSourceService pDataSourceService) {
        repository = pRepository;
        attributeService = pAttributeService;
        modelAttributeService = pModelAttributeService;
        entityService = pEntityService;
        dataSourceService = pDataSourceService;
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
     * @throws EntityNotFoundException
     */
    public DataSet retrieveDataSet(Long pDataSetId) throws EntityNotFoundException {
        DataSet dataset = repository.findOne(pDataSetId);
        if (dataset == null) {
            throw new EntityNotFoundException(pDataSetId, DataSet.class);
        }
        return dataset;
    }

    public DataSet createDataSet(DataSet pDataSet) throws EntityInvalidException, EntityNotFoundException {
        // check for other jpa entities
        entityService.checkLinkedEntity(pDataSet);
        // FIXME: should i consider that DataSource from the dataSet has an ID? this cannot be assured by the @Valid
        // from the REST request because Id cannot be set as NotNull
        dataSourceService.getDataSource(pDataSet.getDataSource().getId());
        // check coherence of the subsettingcriterion
        ICriterion subsettingCriterion = pDataSet.getSubsettingClause();
        if (subsettingCriterion != null) {
            SubsettingCoherenceVisitor criterionVisitor = new SubsettingCoherenceVisitor(
                    pDataSet.getDataSource().getModelOfData(), attributeService, modelAttributeService);
            if (!subsettingCriterion.accept(criterionVisitor)) {
                throw new EntityInvalidException(
                        "given subsettingCriterion cannot be accepted for the DataSet : " + pDataSet.getLabel());
            }
        }
        // everything is fine
        return repository.save(pDataSet);
    }

    public void associateDataSet(Long pDataSetId, List<AbstractEntity> pToBeAssociatedWith) {
        DataSet source = repository.findOne(pDataSetId);
        for (AbstractEntity target : pToBeAssociatedWith) {

        }
    }

}
