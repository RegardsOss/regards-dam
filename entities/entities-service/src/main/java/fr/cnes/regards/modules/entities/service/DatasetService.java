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

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.datasources.service.DataSourceService;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDataSetRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.service.visitor.SubsettingCoherenceVisitor;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttributeService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * @author Sylvain Vissiere-Guerinet
 *
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

    public DatasetService(IDataSetRepository pRepository, IAttributeModelService pAttributeService,
            IModelAttributeService pModelAttributeService, DataSourceService pDataSourceService,
            IAbstractEntityRepository<AbstractEntity> pEntitiesRepository, IModelService pModelService,
            IStorageService pStorageService, IDeletedEntityRepository deletedEntityRepository,
            ICollectionRepository pCollectionRepository, EntityManager pEm) {
        super(pModelAttributeService, pEntitiesRepository, pModelService, pStorageService, deletedEntityRepository,
              pCollectionRepository, pRepository, pEm);
        attributeService = pAttributeService;
        modelAttributeService = pModelAttributeService;
        dataSourceService = pDataSourceService;
    }

    /**
     * @param pDataSetIpId
     * @return
     * @throws EntityNotFoundException
     */
    @Override
    public DataSet retrieveDataSet(UniformResourceName pDataSetIpId) throws EntityNotFoundException {
        DataSet result = (DataSet) datasetRepository.findOneByIpId(pDataSetIpId);
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
    @Override
    public DataSet retrieveDataSet(Long pDataSetId) throws EntityNotFoundException {
        DataSet dataset = datasetRepository.findOne(pDataSetId);
        if (dataset == null) {
            throw new EntityNotFoundException(pDataSetId, DataSet.class);
        }
        return dataset;
    }

    /**
     * modify the DataSet in parameter if needed
     *
     * @param pDataSet
     * @throws EntityNotFoundException
     */
    private DataSet checkDataSource(DataSet pDataSet) throws EntityNotFoundException {
        if (pDataSet.getDataSource() == null) {
            pDataSet.setDataSource(dataSourceService.getDefaultDataSource());
        } else {
            dataSourceService.getDataSource(pDataSet.getDataSource().getId());
        }
        return pDataSet;
    }

    private DataSet checkSubsettingCriterion(DataSet pDataSet) throws EntityInvalidException {
        ICriterion subsettingCriterion = pDataSet.getSubsettingClause();
        if (subsettingCriterion != null) {
            SubsettingCoherenceVisitor criterionVisitor = new SubsettingCoherenceVisitor(
                    pDataSet.getDataSource().getModelOfData(), attributeService, modelAttributeService);
            if (!subsettingCriterion.accept(criterionVisitor)) {
                throw new EntityInvalidException(
                        "given subsettingCriterion cannot be accepted for the DataSet : " + pDataSet.getLabel());
            }
        }
        return pDataSet;
    }

    /**
     * @param pDataSet
     */
    private DataSet checkPluginConfigurations(DataSet pDataSet) {
        // TODO see how to get if the plugin configuration exist on catalog feign client in catalog for plugins
        // idea: create plugin-client sub module and create an interface without the @RestClient and then create a
        // catalog-plugin-client which extends the interface from plugin-client and add the @RestClient
        // this will allow us to have a plugin-client per microservice identified by the microservice name
        // TODO: also check if it is a IService or IConverter or IFilter or IProcessingService configuration!
        return pDataSet;
    }

    /**
     * @param pPageable
     * @return
     */
    // FIXME: return deleted too?
    @Override
    public Page<DataSet> retrieveDataSets(Pageable pPageable) {
        return datasetRepository.findAll(pPageable);
    }

    /**
     * @param pDataSetId
     * @return
     * @throws EntityNotFoundException
     */
    // TODO: return only IService not IConverter or IFilter or IProcessingService(not implemented yet anyway)
    @Override
    public List<Long> retrieveDataSetServices(Long pDataSetId) throws EntityNotFoundException {
        DataSet dataSetWithConfs = datasetRepository.findOneWithPluginConfigurations(pDataSetId);
        if (dataSetWithConfs == null) {
            throw new EntityNotFoundException(pDataSetId, DataSet.class);
        }
        List<Long> pluginConfIds = dataSetWithConfs.getPluginConfigurationIds();
        if (pluginConfIds == null) {
            pluginConfIds = new ArrayList<>();
        }
        return pluginConfIds;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected <T extends AbstractEntity> T beforeCreate(T pNewEntity) throws ModuleException {
        // TODO: download the description
        return pNewEntity;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends AbstractEntity> T doCheck(T pEntity) throws ModuleException {
        DataSet ds = checkDataSource((DataSet) pEntity);
        ds = checkPluginConfigurations(ds);
        ds = checkSubsettingCriterion(ds);
        return (T) ds;
    }

    @Override
    protected <T extends AbstractEntity> T beforeUpdate(T pEntity) {
        // nothing to do for now
        return pEntity;
    }

}
