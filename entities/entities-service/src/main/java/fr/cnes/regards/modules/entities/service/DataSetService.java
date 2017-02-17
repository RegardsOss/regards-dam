/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.datasources.service.DataSourceService;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.IDataSetRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.service.identification.IdentificationService;
import fr.cnes.regards.modules.entities.service.visitor.SubsettingCoherenceVisitor;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttributeService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Service
public class DataSetService extends AbstractEntityService {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetService.class);

    private final IAttributeModelService attributeService;

    private final IModelAttributeService modelAttributeService;

    private final IDataSetRepository repository;

    private final DataSourceService dataSourceService;

    public DataSetService(IDataSetRepository pRepository, IAttributeModelService pAttributeService,
            IModelAttributeService pModelAttributeService, DataSourceService pDataSourceService,
            IdentificationService pIdService, IAbstractEntityRepository<AbstractEntity> pEntitiesRepository,
            IModelService pModelService, IStorageService pStorageSerivce) {
        super(pModelAttributeService, pEntitiesRepository, pModelService, pStorageSerivce, pIdService);
        repository = pRepository;
        attributeService = pAttributeService;
        modelAttributeService = pModelAttributeService;
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

    /**
     * Control the DataSource associated to the {@link DataSet} in parameter if needed.</br>
     * If any DataSource is associated, sets the default DataSource.
     *
     * @param pDataSet
     *            the {@link DataSet} to check
     * @return the modified {@link DataSet}
     * @throws EntityNotFoundException
     *             the DataSet to check does not exist
     */
    private DataSet checkDataSource(DataSet pDataSet) throws EntityNotFoundException {
        if (pDataSet.getDataSource() == null) {
            // If any DataSource, set the default DataSource
            pDataSet.setDataSource(dataSourceService.getDefaultDataSource());
        } else {
            // Verify the existence of the DataSource associated to the DataSet
            dataSourceService.getDataSource(pDataSet.getDataSource().getId());
        }
        return pDataSet;
    }

    /**
     * Check that the sub-setting criterion setting on a DataSet are coherent with the {@link Model} associated to the
     * {@link DataSource}.
     * 
     * @param pDataSet
     *            the {@link DataSet} to check
     * @return the modified {@link DataSet}
     * @throws EntityInvalidException
     *             the subsetting criterion are not coherent with the DataSet
     */
    private DataSet checkSubsettingCriterion(DataSet pDataSet) throws EntityInvalidException {
        ICriterion subsettingCriterion = pDataSet.getSubsettingClause();
        if (subsettingCriterion != null) {

            SubsettingCoherenceVisitor criterionVisitor = new SubsettingCoherenceVisitor(
                    pDataSet.getModelOfData(), attributeService, modelAttributeService);
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
    public Page<DataSet> retrieveDataSetList(Pageable pPageable) {
        return repository.findAll(pPageable);
    }

    /**
     * @param pDataSetId
     * @return
     * @throws EntityNotFoundException
     */
    // TODO: return only IService not IConverter or IFilter or IProcessingService(not implemented yet anyway)
    public List<Long> retrieveDataSetServices(Long pDataSetId) throws EntityNotFoundException {
        DataSet dataSetWithConfs = repository.findOneWithPluginConfigurations(pDataSetId);
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
    protected <T extends AbstractEntity> T doCreate(T pNewEntity) throws ModuleException {
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
    protected <T extends AbstractEntity> T doUpdate(T pEntity) {
        // nothing to do for now
        return pEntity;
    }

}
