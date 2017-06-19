/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.base.Objects;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.datasources.service.DataSourceService;
import fr.cnes.regards.modules.datasources.service.IDataSourceService;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.dao.IDescriptionFileRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.domain.StaticProperties;
import fr.cnes.regards.modules.entities.service.visitor.SubsettingCoherenceVisitor;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * Specific EntityService for Datasets
 *
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Service
@MultitenantTransactional
public class DatasetService extends AbstractEntityService<Dataset> implements IDatasetService {

    private final IAttributeModelService attributeService;

    private final IDataSourceService dataSourceService;

    private final IDescriptionFileRepository descriptionFileRepository;

    public DatasetService(IDatasetRepository pRepository, IAttributeModelService pAttributeService,
            IModelAttrAssocService pModelAttributeService, IDataSourceService pDataSourceService,
            IAbstractEntityRepository<AbstractEntity> pEntityRepository, IModelService pModelService,
            IDeletedEntityRepository deletedEntityRepository, ICollectionRepository pCollectionRepository,
            EntityManager pEm, IPublisher pPublisher, IRuntimeTenantResolver runtimeTenantResolver,
            IDescriptionFileRepository descriptionFileRepository) {
        super(pModelAttributeService, pEntityRepository, pModelService, deletedEntityRepository, pCollectionRepository,
              pRepository, pRepository, pEm, pPublisher, runtimeTenantResolver);
        attributeService = pAttributeService;
        dataSourceService = pDataSourceService;
        this.descriptionFileRepository = descriptionFileRepository;
    }

    /**
     * Control the DataSource associated to the {@link Dataset} in parameter if needed.</br>
     * If any DataSource is associated, sets the default DataSource.
     *
     * @param pDataset
     * @throws EntityNotFoundException
     */
    private Dataset checkDataSource(final Dataset pDataset) throws EntityNotFoundException {
        if (pDataset.getDataSource() == null) {
            // If any DataSource, set the default DataSource
            pDataset.setDataSource(dataSourceService.getInternalDataSource());
        } else {
            // Verify the existence of the DataSource associated to the Dataset
            final DataSource src = dataSourceService.getDataSource(pDataset.getDataSource().getId());
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
    private Dataset checkSubsettingCriterion(final Dataset pDataset) throws ModuleException {
        // getSubsettingClausePartToCheck() cannot be null
        final ICriterion subsettingCriterion = pDataset.getSubsettingClausePartToCheck();
        // To avoid loading models when not necessary
        if (!subsettingCriterion.equals(ICriterion.all())) {
            final SubsettingCoherenceVisitor criterionVisitor = getSubsettingCoherenceVisitor(pDataset.getDataModel());
            if (!subsettingCriterion.accept(criterionVisitor)) {
                throw new EntityInvalidException(
                        "Given subsettingCriterion cannot be accepted for the Dataset : " + pDataset.getLabel());
            }
        }
        return pDataset;
    }

    @Override
    public SubsettingCoherenceVisitor getSubsettingCoherenceVisitor(Long dataModelId) throws ModuleException {
        return new SubsettingCoherenceVisitor(modelService.getModel(dataModelId), attributeService,
                                              modelAttributeService);
    }

    @Override
    protected void doCheck(final Dataset pEntity, final Dataset entityInDB) throws ModuleException {
        Dataset ds = checkDataSource(pEntity);
        checkSubsettingCriterion(ds);
        // check for updates on data model or datasource
        // if entityInDB is null then it is a creation so we cannot be modifying the data model or the datasource
        if (entityInDB != null) {
            if (!Objects.equal(pEntity.getDataSource(), entityInDB.getDataSource())) {
                throw new EntityOperationForbiddenException("Datasources of datasets cannot be updated");
            }
            if (!Objects.equal(pEntity.getDataModel(), entityInDB.getDataModel())) {
                throw new EntityOperationForbiddenException("Data models of datasets cannot be updated");
            }
        }
    }

    @Override
    public Page<AttributeModel> getDataAttributeModels(final Set<UniformResourceName> urns, final Set<Long> modelIds,
            final Pageable pPageable) throws ModuleException {
        if (((modelIds == null) || modelIds.isEmpty()) && ((urns == null) || urns.isEmpty())) {
            final List<Dataset> datasets = datasetRepository.findAll();
            return getDataAttributeModelsFromDatasets(datasets, pPageable);
        } else {
            if ((modelIds == null) || modelIds.isEmpty()) {
                final List<Dataset> datasets = datasetRepository.findByIpIdIn(urns);
                return getDataAttributeModelsFromDatasets(datasets, pPageable);
            } else {
                final Set<Dataset> datasets = datasetRepository.findAllByModelId(modelIds);
                return getDataAttributeModelsFromDatasets(datasets, pPageable);
            }
        }
    }

    @Override
    public DescriptionFile retrieveDescription(Long datasetId) throws EntityNotFoundException {
        Dataset ds = datasetRepository.findOneDescriptionFile(datasetId);
        if (ds == null) {
            throw new EntityNotFoundException(datasetId, Dataset.class);
        }
        return ds.getDescriptionFile();
    }

    @Override
    public void removeDescription(Long datasetId) throws EntityNotFoundException {
        Dataset ds = datasetRepository.findOneDescriptionFile(datasetId);
        if (ds == null) {
            throw new EntityNotFoundException(datasetId, Dataset.class);
        }
        DescriptionFile desc = ds.getDescriptionFile();
        ds.setDescriptionFile(null);
        descriptionFileRepository.delete(desc);
    }

    /**
     * extract all the AttributeModel of {@link DataObject} that can be contained into the datasets
     */
    private Page<AttributeModel> getDataAttributeModelsFromDatasets(final Collection<Dataset> datasets,
            final Pageable pPageable) throws ModuleException {
        final List<Long> modelIds = datasets.stream().map(ds -> ds.getDataModel()).collect(Collectors.toList());
        Page<AttributeModel> attModelPage = modelAttributeService.getAttributeModels(modelIds, pPageable);
        // Build JSON path
        attModelPage.forEach(attModel -> attModel.buildJsonPath(StaticProperties.PROPERTIES));
        return attModelPage;
    }

}
