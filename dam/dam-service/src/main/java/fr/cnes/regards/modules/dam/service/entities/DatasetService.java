/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.service.entities;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
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
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.modules.dam.dao.dataaccess.IAccessRightRepository;
import fr.cnes.regards.modules.dam.dao.entities.IAbstractEntityRepository;
import fr.cnes.regards.modules.dam.dao.entities.ICollectionRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDeletedEntityRepository;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessRight;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDataSourcePlugin;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.service.entities.visitor.SubsettingCoherenceVisitor;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;
import fr.cnes.regards.modules.dam.service.models.IModelAttrAssocService;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.IOpenSearchService;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;

/**
 * Specific EntityService for Datasets
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Service
@MultitenantTransactional
public class DatasetService extends AbstractEntityService<Dataset> implements IDatasetService {

    /**
     * {@link IOpenSearchService} instance
     */
    private final IOpenSearchService openSearchService;

    private final IPluginService pluginService;

    @Autowired
    private IAttributeFinder finder;

    @Autowired
    private IAccessRightRepository accessRightRepository;

    public DatasetService(IDatasetRepository repository, IAttributeModelService attributeService,
            IModelAttrAssocService modelAttributeService, IAbstractEntityRepository<AbstractEntity<?>> entityRepository,
            IModelService modelService, IDeletedEntityRepository deletedEntityRepository,
            ICollectionRepository collectionRepository, EntityManager em, IPublisher publisher,
            IRuntimeTenantResolver runtimeTenantResolver, IOpenSearchService openSearchService,
            IPluginService pluginService) {
        super(modelAttributeService, entityRepository, modelService, deletedEntityRepository, collectionRepository,
              repository, repository, em, publisher, runtimeTenantResolver);
        this.openSearchService = openSearchService;
        this.pluginService = pluginService;
    }

    /**
     * Control the DataSource associated to the {@link Dataset} in parameter if needed.</br>
     * If any DataSource is associated, sets the default DataSource.
     * @throws ModuleException if error occurs!
     */
    private Dataset checkDataSource(final Dataset dataset) throws ModuleException {
        if (dataset.getDataSource() != null) {
            // Retrieve plugin from associated datasource
            IDataSourcePlugin datasourcePlugin = pluginService.getPlugin(dataset.getDataSource().getId());
            String modelName = datasourcePlugin.getModelName();
            try {
                Model model = modelService.getModelByName(modelName);
                dataset.setDataModel(model.getName());
            } catch (ModuleException e) {
                logger.error("Unable to dejsonify model parameter from PluginConfiguration", e);
                throw new EntityNotFoundException(String
                        .format("Unable to dejsonify model parameter from PluginConfiguration (%s)", e.getMessage()),
                        PluginConfiguration.class);
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
            // if this exception happens it's really an issue as the whole system relys on the fact UTF-8 is handled
            throw new RsRuntimeException(e);
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
    public SubsettingCoherenceVisitor getSubsettingCoherenceVisitor(String dataModelName) throws ModuleException {
        return new SubsettingCoherenceVisitor(finder);
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
                final Set<Dataset> datasets = datasetRepository.findAllByModelIdIn(modelIds);
                return getDataAttributeModelsFromDatasets(datasets, pageable);
            }
        }
    }

    @Override
    public Page<AttributeModel> getAttributeModels(Set<UniformResourceName> urns, Set<Long> modelIds, Pageable pageable)
            throws ModuleException {
        Page<AttributeModel> attModelPage;
        if (((modelIds == null) || modelIds.isEmpty()) && ((urns == null) || urns.isEmpty())) {
            // Retrieve all dataset models attributes
            List<Model> allDsModels = modelService.getModels(EntityType.DATASET);
            Set<Long> dsModelIds = allDsModels.stream().map(ds -> ds.getId()).collect(Collectors.toSet());
            attModelPage = modelAttributeService.getAttributeModels(dsModelIds, pageable);
        } else {
            if ((modelIds == null) || modelIds.isEmpty()) {
                // Retrieve all attributes associated to the given datasets
                List<Dataset> datasets = datasetRepository.findByIpIdIn(urns);
                Set<Long> dsModelIds = datasets.stream().map(ds -> ds.getModel().getId()).collect(Collectors.toSet());
                attModelPage = modelAttributeService.getAttributeModels(dsModelIds, pageable);
            } else {
                // Retrieve all attributes associated to the given models.
                attModelPage = modelAttributeService.getAttributeModels(modelIds, pageable);
            }
        }

        return attModelPage;
    }

    /**
     * extract all the AttributeModel of {@link DataObject} that can be contained into the datasets
     */
    private Page<AttributeModel> getDataAttributeModelsFromDatasets(final Collection<Dataset> datasets,
            final Pageable pageable) throws ModuleException {
        final List<String> modelNames = datasets.stream().map(ds -> ds.getDataModel()).collect(Collectors.toList());
        Page<AttributeModel> attModelPage = modelAttributeService.getAttributeModelsByName(modelNames, pageable);
        return attModelPage;
    }

    @Override
    public Dataset delete(Long datasetId) throws ModuleException {
        Assert.notNull(datasetId, "Entity identifier is required");
        Dataset toDelete = load(datasetId);
        // Remove access rights
        List<AccessRight> accessRights = accessRightRepository.findAllByDataset(toDelete);
        accessRights.forEach(ar -> accessRightRepository.delete(ar));
        // Delete dataset
        return delete(toDelete);
    }
}
