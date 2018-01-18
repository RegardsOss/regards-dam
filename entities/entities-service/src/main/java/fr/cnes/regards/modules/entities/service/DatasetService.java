/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.entities.service;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.ModelMappingAdapter;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IAipDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin;
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
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.modules.opensearch.service.IOpenSearchService;

/**
 * Specific EntityService for Datasets
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Service
@MultitenantTransactional
public class DatasetService extends AbstractEntityService<Dataset> implements IDatasetService {

    /**
     * {@link IAttributeModelService} instance
     */
    private final IAttributeModelService attributeService;

    /**
     * {@link IOpenSearchService} instance
     */
    private final IOpenSearchService openSearchService;

    private final IPluginService pluginService;

    public DatasetService(IDatasetRepository pRepository, IAttributeModelService attributeService,
            IModelAttrAssocService pModelAttributeService, IAbstractEntityRepository<AbstractEntity> pEntityRepository,
            IModelService pModelService, IDeletedEntityRepository deletedEntityRepository,
            ICollectionRepository pCollectionRepository, EntityManager pEm, IPublisher pPublisher,
            IRuntimeTenantResolver runtimeTenantResolver, IDescriptionFileRepository descriptionFileRepository,
            IOpenSearchService openSearchService, IPluginService pluginService) {
        super(pModelAttributeService, pEntityRepository, pModelService, deletedEntityRepository, pCollectionRepository,
              pRepository, pRepository, pEm, pPublisher, runtimeTenantResolver, descriptionFileRepository);
        this.attributeService = attributeService;
        this.openSearchService = openSearchService;
        this.pluginService = pluginService;
    }

    /**
     * Control the DataSource associated to the {@link Dataset} in parameter if needed.</br>
     * If any DataSource is associated, sets the default DataSource.
     */
    private Dataset checkDataSource(final Dataset dataset) throws EntityNotFoundException {
        if (dataset.getDataSource() != null) {
            // Retrieve DataModel from associated datasource
            // First : pluginConf id
            Long datasourceId = dataset.getDataSource().getId();
            // Then PluginConf...
            PluginConfiguration pluginConf = pluginService.getPluginConfiguration(datasourceId);
            // ...then retrieve data model id and set it onto dataset
            if (pluginConf.getInterfaceNames().contains(IDBDataSourcePlugin.class.getName())) {
                String jsonModelMapping = pluginConf.getParameterValue(IDBDataSourcePlugin.MODEL_PARAM);
                ModelMappingAdapter adapter = new ModelMappingAdapter();
                try {
                    DataSourceModelMapping modelMapping = adapter.fromJson(jsonModelMapping);
                    dataset.setDataModel(modelMapping.getModel());
                } catch (IOException e) {
                    throw new EntityNotFoundException(
                            "Unable to dejsonify model mapping parameter from " + "PluginConfiguration (" + e
                                    .getMessage() + ")", PluginConfiguration.class);
                }
            } else if (pluginConf.getInterfaceNames().contains(IAipDataSourcePlugin.class.getName())) {
                String modelName = pluginConf.getParameterValue(IAipDataSourcePlugin.MODEL_NAME_PARAM);
                Model dataModel = modelService.getModelByName(modelName);
                if (dataModel == null) {
                    throw new EntityNotFoundException(
                            "Datasource PluginConfiguration refers to an unknown model (name: " + modelName + ")",
                            PluginConfiguration.class);
                }
                dataset.setDataModel(dataModel.getId());
            }
        }
        return dataset;
    }

    /**
     * Check that the sub-setting criterion setting on a Dataset are coherent with the {@link Model} associated to the
     * data source. Should always be closed after checkDataSource, so the dataModel is properly set.
     * @param dataset the {@link Dataset} to check
     * @return the modified {@link Dataset}
     */
    private Dataset checkSubsettingCriterion(final Dataset dataset) throws ModuleException {
        // getUserSubsettingClause() cannot be null
        try {
            String stringClause = dataset.getOpenSearchSubsettingClause();
            if (Strings.isNullOrEmpty(stringClause)) {
                dataset.setSubsettingClause(ICriterion.all());
            } else {
                dataset.setSubsettingClause(openSearchService.parse("q=" + UriUtils.encode(stringClause, "UTF-8")));
            }
        } catch (UnsupportedEncodingException e) {
            //if this exception happens its really an issue as the whole system relys on the fact UTF-8 is handled
            throw new RuntimeException(e);
        }
        final ICriterion subsettingCriterion = dataset.getUserSubsettingClause();
        // To avoid loading models when not necessary
        if (!subsettingCriterion.equals(ICriterion.all())) {
            final SubsettingCoherenceVisitor criterionVisitor = getSubsettingCoherenceVisitor(dataset.getDataModel());
            if (!subsettingCriterion.accept(criterionVisitor)) {
                throw new EntityInvalidException(
                        "Given subsettingCriterion cannot be accepted for the Dataset : " + dataset.getLabel());
            }
        }
        return dataset;
    }

    @Override
    public SubsettingCoherenceVisitor getSubsettingCoherenceVisitor(Long dataModelId) throws ModuleException {
        return new SubsettingCoherenceVisitor(modelService.getModel(dataModelId), attributeService,
                                              modelAttributeService);
    }

    @Override
    protected void doCheck(final Dataset entity, final Dataset entityInDB) throws ModuleException {
        Dataset ds = checkDataSource(entity);
        checkSubsettingCriterion(ds);
        // check for updates on data model or datasource
        // if entityInDB is null then it is a creation so we cannot be modifying the data model or the datasource
        if (entityInDB != null) {
            if (!Objects.equal(entity.getDataSource(), entityInDB.getDataSource())) {
                throw new EntityOperationForbiddenException("Datasources of datasets cannot be updated");
            }
            if (!Objects.equal(entity.getDataModel(), entityInDB.getDataModel())) {
                throw new EntityOperationForbiddenException("Data models of datasets cannot be updated");
            }
        }
    }

    @Override
    public Page<AttributeModel> getDataAttributeModels(final Set<UniformResourceName> urns, final Set<Long> modelIds,
            final Pageable pageable) throws ModuleException {
        if (((modelIds == null) || modelIds.isEmpty()) && ((urns == null) || urns.isEmpty())) {
            final List<Dataset> datasets = datasetRepository.findAll();
            return getDataAttributeModelsFromDatasets(datasets, pageable);
        } else {
            if ((modelIds == null) || modelIds.isEmpty()) {
                final List<Dataset> datasets = datasetRepository.findByIpIdIn(urns);
                return getDataAttributeModelsFromDatasets(datasets, pageable);
            } else {
                final Set<Dataset> datasets = datasetRepository.findAllByModelId(modelIds);
                return getDataAttributeModelsFromDatasets(datasets, pageable);
            }
        }
    }

    @Override
    public DescriptionFile retrieveDescription(UniformResourceName datasetIpId) throws EntityNotFoundException {
        Dataset ds = datasetRepository.findOneDescriptionFile(datasetIpId);
        if (ds == null) {
            throw new EntityNotFoundException(datasetIpId.toString(), Dataset.class);
        }
        return ds.getDescriptionFile();
    }

    @Override
    public void removeDescription(UniformResourceName datasetIpId) throws EntityNotFoundException {
        Dataset ds = datasetRepository.findOneDescriptionFile(datasetIpId);
        if (ds == null) {
            throw new EntityNotFoundException(datasetIpId.toString(), Dataset.class);
        }
        DescriptionFile desc = ds.getDescriptionFile();
        ds.setDescriptionFile(null);
        descriptionFileRepository.delete(desc);
    }

    /**
     * extract all the AttributeModel of {@link DataObject} that can be contained into the datasets
     */
    private Page<AttributeModel> getDataAttributeModelsFromDatasets(final Collection<Dataset> datasets,
            final Pageable pageable) throws ModuleException {
        final List<Long> modelIds = datasets.stream().map(ds -> ds.getDataModel()).collect(Collectors.toList());
        Page<AttributeModel> attModelPage = modelAttributeService.getAttributeModels(modelIds, pageable);
        // Build JSON path
        attModelPage.forEach(attModel -> attModel.buildJsonPath(StaticProperties.PROPERTIES));
        return attModelPage;
    }

}
