/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
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
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
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
public class DatasetService extends EntityService implements IDatasetService {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetService.class);

    private final IAttributeModelService attributeService;

    private final IModelAttributeService modelAttributeService;

    private final DataSourceService dataSourceService;

    private final JWTService jwtService;

    @Value("${spring.application.name}")
    private String microserviceName;

    public DatasetService(IDatasetRepository pRepository, IAttributeModelService pAttributeService,
            IModelAttributeService pModelAttributeService, DataSourceService pDataSourceService,
            IAbstractEntityRepository<AbstractEntity> pEntitiesRepository, IModelService pModelService,
            IDeletedEntityRepository deletedEntityRepository, ICollectionRepository pCollectionRepository,
            EntityManager pEm, JWTService pJwtService, IPublisher pPublisher) {
        super(pModelAttributeService, pEntitiesRepository, pModelService, deletedEntityRepository,
              pCollectionRepository, pRepository, pEm, pPublisher);
        attributeService = pAttributeService;
        modelAttributeService = pModelAttributeService;
        dataSourceService = pDataSourceService;
        jwtService = pJwtService;
    }

    /**
     *
     * retrieve a Dataset by its IpId
     *
     * @param pDatasetIpId
     * @return the Dataset of IpId pDatasetIpId
     * @throws EntityNotFoundException
     *             when the entity does not exist
     */
    @Override
    public Dataset retrieveDataset(UniformResourceName pDatasetIpId) throws EntityNotFoundException {
        Dataset result = datasetRepository.findOneByIpId(pDatasetIpId);
        if (result == null) {
            throw new EntityNotFoundException(pDatasetIpId.toString(), Dataset.class);
        }
        return result;
    }

    /**
     *
     * retrieve a Dataset by its id
     *
     * @param pDatasetId
     * @return the Dataset of Id pDatasetId
     * @throws EntityNotFoundException
     *             when entity does not exist
     */
    @Override
    public Dataset retrieveDataset(Long pDatasetId) throws EntityNotFoundException {
        Dataset dataset = datasetRepository.findOne(pDatasetId);
        if (dataset == null) {
            throw new EntityNotFoundException(pDatasetId, Dataset.class);
        }
        return dataset;
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
     *
     * @param pDataset
     *            the {@link Dataset} to check
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

    /**
     * @param pPageable
     * @return
     */
    @Override
    public Page<Dataset> retrieveDatasets(Pageable pPageable) {
        return datasetRepository.findAll(pPageable);
    }

    protected static Logger getLogger() {
        return LOGGER;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends AbstractEntity> T doCheck(T pEntity) throws ModuleException {
        Dataset ds = checkDataSource((Dataset) pEntity);
        ds = checkSubsettingCriterion(ds);
        return (T) ds;
    }

    /**
     *
     * retrieve the descriptionFile of a DataSet.
     *
     *
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
