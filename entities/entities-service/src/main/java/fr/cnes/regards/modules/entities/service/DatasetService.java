/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
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
public class DatasetService extends AbstractEntityService implements IDatasetService {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetService.class);

    private final IAttributeModelService attributeService;

    private final IModelAttributeService modelAttributeService;

    private final DataSourceService dataSourceService;

    public DatasetService(IDatasetRepository pRepository, IAttributeModelService pAttributeService,
            IModelAttributeService pModelAttributeService, DataSourceService pDataSourceService,
            IAbstractEntityRepository<AbstractEntity> pEntitiesRepository, IModelService pModelService,
            IDeletedEntityRepository deletedEntityRepository, ICollectionRepository pCollectionRepository,
            EntityManager pEm, IPublisher pPublisher) {
        super(pModelAttributeService, pEntitiesRepository, pModelService, deletedEntityRepository,
              pCollectionRepository, pRepository, pEm, pPublisher);
        attributeService = pAttributeService;
        modelAttributeService = pModelAttributeService;
        dataSourceService = pDataSourceService;
    }

    /**
     * @param pDatasetIpId
     * @return
     * @throws EntityNotFoundException
     */
    @Override
    public Dataset retrieveDataset(UniformResourceName pDatasetIpId) throws EntityNotFoundException {
        Dataset result = (Dataset) datasetRepository.findOneByIpId(pDatasetIpId);
        if (result == null) {
            throw new EntityNotFoundException(pDatasetIpId.toString(), Dataset.class);
        }
        return result;
    }

    /**
     * @param pDatasetId
     * @return
     * @throws EntityNotFoundException
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
            pDataset.setDataSource(dataSourceService.getDefaultDataSource());
        } else {
            // Verify the existence of the DataSource associated to the Dataset
            dataSourceService.getDataSource(pDataset.getDataSource().getId());
        }
        return pDataset;
    }

    /**
     * Check that the sub-setting criterion setting on a Dataset are coherent with the {@link Model} associated to the
     * {@link DataSource}.
     *
     * @param pDataset
     *            the {@link Dataset} to check
     * @return the modified {@link Dataset}
     * @throws EntityInvalidException
     *             the subsetting criterion are not coherent with the Dataset
     */
    private Dataset checkSubsettingCriterion(Dataset pDataset) throws EntityInvalidException {
        ICriterion subsettingCriterion = pDataset.getSubsettingClause();
        if (subsettingCriterion != null) {

            SubsettingCoherenceVisitor criterionVisitor = new SubsettingCoherenceVisitor(pDataset.getModelOfData(),
                    attributeService, modelAttributeService);
            if (!subsettingCriterion.accept(criterionVisitor)) {
                throw new EntityInvalidException(
                        "given subsettingCriterion cannot be accepted for the Dataset : " + pDataset.getLabel());
            }
        }
        return pDataset;
    }

    /**
     * @param pDataset
     */
    private Dataset checkPluginConfigurations(Dataset pDataset) {
        // TODO see how to get if the plugin configuration exist on catalog feign client in catalog for plugins
        // idea: create plugin-client sub module and create an interface without the @RestClient and then create a
        // catalog-plugin-client which extends the interface from plugin-client and add the @RestClient
        // this will allow us to have a plugin-client per microservice identified by the microservice name
        // TODO: also check if it is a IService or IConverter or IFilter or IProcessingService configuration!
        return pDataset;
    }

    /**
     * @param pPageable
     * @return
     */
    // FIXME: return deleted too?
    @Override
    public Page<Dataset> retrieveDatasets(Pageable pPageable) {
        return datasetRepository.findAll(pPageable);
    }

    /**
     * @param pDatasetId
     * @return
     * @throws EntityNotFoundException
     */
    // TODO: return only IService not IConverter or IFilter or IProcessingService(not implemented yet anyway)
    @Override
    public List<Long> retrieveDatasetServices(Long pDatasetId) throws EntityNotFoundException {
        Dataset dataSetWithConfs = datasetRepository.findOneWithPluginConfigurations(pDatasetId);
        if (dataSetWithConfs == null) {
            throw new EntityNotFoundException(pDatasetId, Dataset.class);
        }
        List<Long> pluginConfIds = dataSetWithConfs.getPluginConfigurationIds();
        if (pluginConfIds == null) {
            pluginConfIds = new ArrayList<>();
        }
        return pluginConfIds;
    }

    protected static Logger getLogger() {
        return LOGGER;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends AbstractEntity> T doCheck(T pEntity) throws ModuleException {
        Dataset ds = checkDataSource((Dataset) pEntity);
        ds = checkPluginConfigurations(ds);
        ds = checkSubsettingCriterion(ds);
        return (T) ds;
    }

    /**
     * @param pDatasetId
     * @return
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
