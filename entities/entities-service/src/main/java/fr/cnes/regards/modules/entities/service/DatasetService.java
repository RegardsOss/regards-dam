/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.datasources.service.DataSourceService;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.service.visitor.SubsettingCoherenceVisitor;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttributeService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * Specific EntityService for Datasets
 *
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Service
public class DatasetService extends AbstractEntityService<Dataset> implements IDatasetService {

    private final IAttributeModelService attributeService;

    private final IModelAttributeService modelAttributeService;

    private final DataSourceService dataSourceService;

    public DatasetService(IDatasetRepository pRepository, IAttributeModelService pAttributeService,
            IModelAttributeService pModelAttributeService, DataSourceService pDataSourceService,
            IAbstractEntityRepository<AbstractEntity> pEntityRepository, IModelService pModelService,
            IDeletedEntityRepository deletedEntityRepository, ICollectionRepository pCollectionRepository,
            EntityManager pEm, IPublisher pPublisher) {
        super(pModelAttributeService, pEntityRepository, pModelService, deletedEntityRepository, pCollectionRepository,
              pRepository, pRepository, pEm, pPublisher);
        attributeService = pAttributeService;
        modelAttributeService = pModelAttributeService;
        dataSourceService = pDataSourceService;
    }

    /**
     * Control the DataSource associated to the {@link Dataset} in parameter if needed.</br>
     * If any DataSource is associated, sets the default DataSource.
     *
     * @param pDataset
     * @throws EntityNotFoundException
     */
    private Dataset checkDataSource(Dataset pDataset) throws EntityNotFoundException {
        if (pDataset.getDataSource() == null) {
            // If any DataSource, set the default DataSource
            pDataset.setDataSource(dataSourceService.getInternalDataSource());
        } else {
            // Verify the existence of the DataSource associated to the Dataset
            DataSource src = dataSourceService.getDataSource(pDataset.getDataSource().getId());
            pDataset.setDataModel(src.getMapping().getModel());
        }
        return pDataset;
    }

    /**
     * Check that the sub-setting criterion setting on a Dataset are coherent with the {@link Model} associated to the
     * {@link DataSource}. Should always be closed after checkDataSource, so the dataModel is properly set.
     * @param pDataset the {@link Dataset} to check
     * @return the modified {@link Dataset}
     * @throws ModuleException
     */
    private Dataset checkSubsettingCriterion(Dataset pDataset) throws ModuleException {
        ICriterion subsettingCriterion = pDataset.getSubsettingClause();
        if (subsettingCriterion != null) {

            SubsettingCoherenceVisitor criterionVisitor = new SubsettingCoherenceVisitor(
                    modelService.getModel(pDataset.getDataModel()), attributeService, modelAttributeService);
            if (!subsettingCriterion.accept(criterionVisitor)) {
                throw new EntityInvalidException(
                        "given subsettingCriterion cannot be accepted for the Dataset : " + pDataset.getLabel());
            }
        }
        return pDataset;
    }

    @Override
    protected Dataset doCheck(Dataset pEntity) throws ModuleException {
        Dataset ds = checkDataSource(pEntity);
        ds = checkSubsettingCriterion(ds);
        return ds;
    }

    /**
     * Retrieve the descriptionFile of a DataSet.
     * @param pDatasetId
     * @return the DescriptionFile or null
     * @throws EntityNotFoundException
     */
    public DescriptionFile retrieveDatasetDescription(Long pDatasetId) throws EntityNotFoundException {
        Dataset ds = datasetRepository.findOneDescriptionFile(pDatasetId);
        if (ds == null) {
            throw new EntityNotFoundException(pDatasetId, Dataset.class);
        }
        return ds.getDescriptionFile();
    }

}
