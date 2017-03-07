/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.rest.utils.HttpUtils;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.security.utils.endpoint.RoleAuthority;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.framework.security.utils.jwt.JwtTokenUtils;
import fr.cnes.regards.microservices.catalog.plugin.client.ICatalogPluginClient;
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
import fr.cnes.regards.modules.search.service.IConverter;
import fr.cnes.regards.modules.search.service.IFilter;
import fr.cnes.regards.modules.search.service.IService;

/**
 * Specific EntityService for Datasets
 *
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Service
@EnableFeignClients(clients = ICatalogPluginClient.class)
public class DatasetService extends EntityService implements IDatasetService {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetService.class);

    private final IAttributeModelService attributeService;

    private final IModelAttributeService modelAttributeService;

    private final DataSourceService dataSourceService;

    private final ICatalogPluginClient catalogPluginClient;

    private final JWTService jwtService;

    @Value("${spring.application.name}")
    private String microserviceName;

    public DatasetService(IDatasetRepository pRepository, IAttributeModelService pAttributeService,
            IModelAttributeService pModelAttributeService, DataSourceService pDataSourceService,
            IAbstractEntityRepository<AbstractEntity> pEntitiesRepository, IModelService pModelService,
            IDeletedEntityRepository deletedEntityRepository, ICollectionRepository pCollectionRepository,
            EntityManager pEm, ICatalogPluginClient pPluginClient, JWTService pJwtService, IPublisher pPublisher) {
        super(pModelAttributeService, pEntitiesRepository, pModelService, deletedEntityRepository,
              pCollectionRepository, pRepository, pEm, pPublisher);
        attributeService = pAttributeService;
        modelAttributeService = pModelAttributeService;
        dataSourceService = pDataSourceService;
        catalogPluginClient = pPluginClient;
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
     * Check that the given set of configuration Id exist in rs-catalog and that they are of the right type(IService,
     * IFilter, IConverter)
     *
     * @param pDataset
     *            Dataset to check
     * @return checked Dataset
     * @throws EntityNotFoundException
     * @throws EntityInvalidException
     */
    private Dataset checkPluginConfigurations(Dataset pDataset) throws EntityNotFoundException, EntityInvalidException {
        // TODO: IProcessingService configuration!
        List<Long> configurationIds = pDataset.getPluginConfigurationIds();
        List<String> possiblePluginTypeNames = new ArrayList<>();
        possiblePluginTypeNames.add(IFilter.class.getName());
        possiblePluginTypeNames.add(IConverter.class.getName());
        possiblePluginTypeNames.add(IService.class.getName());
        for (Long configId : configurationIds) {
            PluginConfiguration pluginConf = JwtTokenUtils
                    .asSafeCallableOnRole(this::getPluginConfiguration, configId, jwtService, null)
                    .apply(RoleAuthority.getSysRole(microserviceName));
            if (pluginConf == null) {
                throw new EntityNotFoundException(configId, PluginConfiguration.class);
            }
            String pluginType = pluginConf.getInterfaceName();
            if (!possiblePluginTypeNames.contains(pluginType)) {
                throw new EntityInvalidException(
                        "The Datasets can only contains configuration of Service, convert or Filter, not "
                                + pluginType);
            }
        }
        return pDataset;
    }

    /**
     *
     * Handle the use of RestClient(Client Feign) for plugins of rs-catalog
     *
     * @param pConfigurationId
     * @return the PluginConfiguration if it exist or null
     */
    private PluginConfiguration getPluginConfiguration(Long pConfigurationId) {// NOSONAR: sonar does not detect that
                                                                                   // the method is used by
                                                                               // DataSetServive#checkPluginConfigurations
                                                                               // with the
                                                                               // "this::getPluginConfiguration"
        final ResponseEntity<Resource<PluginConfiguration>> response = catalogPluginClient
                .getPluginConfigurationDirectAccess(pConfigurationId);
        final HttpStatus responseStatus = response.getStatusCode();
        if (!HttpUtils.isSuccess(responseStatus)) {
            // if it gets here it's mainly because of 404 so it means entity not found
            return null;
        }
        return response.getBody().getContent();
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
     * extract the identifier of {@link IService} to be applied to the DataSet
     *
     * @param pDatasetId
     * @return List of Ids of IService
     * @throws EntityNotFoundException
     *             thrown if the DataSet does not exist
     */
    @Override
    public List<Long> retrieveDatasetServices(Long pDatasetId) throws EntityNotFoundException {
        Dataset dataSetWithConfs = datasetRepository.findOneWithPluginConfigurations(pDatasetId);
        if (dataSetWithConfs == null) {
            throw new EntityNotFoundException(pDatasetId, Dataset.class);
        }
        List<Long> pluginConfIds = dataSetWithConfs.getPluginConfigurationIds();
        return pluginConfIds.stream().filter(confId -> isServiceId(confId)).collect(Collectors.toList());
    }

    /**
     * check either the pluginConfiguration of id pConfigId is a configuration for a {@link IService} or not
     *
     * @param pConfigId
     * @return either it is a PluginConfiguration Id of a service or not
     */
    private boolean isServiceId(Long pConfigId) {
        PluginConfiguration pluginConf = JwtTokenUtils
                .asSafeCallableOnRole(this::getPluginConfiguration, pConfigId, jwtService, null)
                .apply(RoleAuthority.getSysRole(microserviceName));
        // Dataset creation assured that pluginConf cannot be null here because it is called after the DataSet has been created
        return pluginConf.getInterfaceName().equals(IService.class.getName());
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
