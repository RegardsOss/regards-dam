/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.crawler.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.google.common.base.Strings;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.notification.NotificationLevel;
import fr.cnes.regards.framework.notification.client.INotificationClient;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.crawler.dao.IDatasourceIngestionRepository;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.service.consumer.DataObjectAssocRemover;
import fr.cnes.regards.modules.crawler.service.consumer.DataObjectGroupAssocRemover;
import fr.cnes.regards.modules.crawler.service.consumer.DataObjectGroupAssocUpdater;
import fr.cnes.regards.modules.crawler.service.consumer.DataObjectUpdater;
import fr.cnes.regards.modules.crawler.service.consumer.SaveDataObjectsCallable;
import fr.cnes.regards.modules.crawler.service.event.DataSourceMessageEvent;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessLevel;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.plugins.IDataObjectAccessFilterPlugin;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.attribute.AbstractAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.ObjectAttribute;
import fr.cnes.regards.modules.dam.domain.entities.event.BroadcastEntityEvent;
import fr.cnes.regards.modules.dam.domain.entities.event.EventType;
import fr.cnes.regards.modules.dam.domain.entities.metadata.DatasetMetadata.DataObjectGroup;
import fr.cnes.regards.modules.dam.domain.models.IComputedAttribute;
import fr.cnes.regards.modules.dam.gson.entities.DamGsonReadyEvent;
import fr.cnes.regards.modules.dam.service.dataaccess.IAccessRightService;
import fr.cnes.regards.modules.dam.service.entities.DataObjectService;
import fr.cnes.regards.modules.dam.service.entities.ICollectionService;
import fr.cnes.regards.modules.dam.service.entities.IDatasetService;
import fr.cnes.regards.modules.dam.service.entities.IEntitiesService;
import fr.cnes.regards.modules.dam.service.entities.visitor.AttributeBuilderVisitor;
import fr.cnes.regards.modules.indexer.dao.BulkSaveResult;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.dao.spatial.ProjectGeoSettings;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * @author oroussel
 */
@Service
public class EntityIndexerService implements IEntityIndexerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityIndexerService.class);

    private static final DateTimeFormatter ISO_TIME_UTC = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_TIME).toFormatter();

    /**
     * Current tenant resolver
     */
    @Autowired
    protected IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    protected IEsRepository esRepos;

    @Autowired
    protected IPluginService pluginService;

    @Autowired
    private IEntitiesService entitiesService;

    @Autowired
    private IAccessRightService accessRightService;

    @Autowired
    private IPublisher publisher;

    @Autowired
    private ISubscriber subscriber;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private DataObjectService dataObjectService;

    @Autowired
    private IDatasetService datasetService;

    @Autowired
    private ICollectionService collectionService;

    @Autowired
    @Lazy
    private IEntityIndexerService self;

    /**
     * The spring application name ~= microservice type
     */
    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private INotificationClient notifClient;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private IDatasourceIngestionRepository dsIngestionRepository;

    @Autowired
    private ProjectGeoSettings projectGeoSettings;

    private static List<String> toErrors(Errors errorsObject) {
        List<String> errors = new ArrayList<>(errorsObject.getErrorCount());
        for (ObjectError objError : errorsObject.getAllErrors()) {
            if (objError instanceof FieldError) {
                StringBuilder buf = new StringBuilder();
                buf.append("Field error in object ").append(objError.getObjectName());
                buf.append(" on field '").append(((FieldError) objError).getField());
                buf.append("', rejected value '").append(((FieldError) objError).getRejectedValue());
                buf.append("': ").append(((FieldError) objError).getField());
                buf.append(" ").append(objError.getDefaultMessage());
                errors.add(buf.toString());
            } else {
                errors.add(objError.toString());
            }
        }
        return errors;
    }

    @Override
    @EventListener
    public void handleApplicationReady(DamGsonReadyEvent event) {
        // TODO : subscriber.subscribeTo(AIPEvent.class, new AIPEventHandler());
    }

    /**
     * Load given entity from database and update Elasticsearch
     *
     * @param tenant                        concerned tenant (also index intoES)
     * @param ipId                          concerned entity IpId
     * @param lastUpdateDate                for dataset entity, if this date is provided, only more recent data objects must be taken
     *                                      into account
     * @param forceAssociatedEntitiesUpdate for dataset entity, force associated entities (ie data objects) update
     * @param dsiId                         DataSourceIngestion identifier
     */
    @Override
    public void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate, boolean forceAssociatedEntitiesUpdate, String dsiId) throws ModuleException {
        LOGGER.info("Updating {}", ipId.toString());
        runtimeTenantResolver.forceTenant(tenant);
        AbstractEntity<?> entity = entitiesService.loadWithRelations(ipId);
        // If entity does no more exist in database, it must be deleted from ES
        if (entity == null) {
            LOGGER.debug("Entity is null !!");
            if (ipId.getEntityType() == EntityType.DATASET) {
                sendDataSourceMessage(String.format("    Dataset with IP_ID %s no more exists...", ipId.toString()),
                                      dsiId);
                manageDatasetDelete(tenant, ipId.toString(), dsiId);
            }
            esRepos.delete(tenant, ipId.getEntityType().toString(), ipId.toString());
            sendDataSourceMessage(String.format("    ...Dataset with IP_ID %s de-indexed.", ipId.toString()), dsiId);
        } else { // entity has been created or updated, it must be saved into ES
            createIndexIfNeeded(tenant);
            ICriterion savedSubsettingClause = null;
            // Remove parameters of dataset datasource to avoid expose security values
            if (entity instanceof Dataset) {
                Dataset dataset = (Dataset) entity;
                // entity must be detached else Hibernate tries to commit update (datasource is cascade.DETACHED)
                em.detach(entity);
                if (dataset.getDataSource() != null) {
                    dataset.getDataSource().getParameters().clear();
                }
                // Subsetting clause must not be jsonify into Elasticsearch
                savedSubsettingClause = dataset.getSubsettingClause();
                dataset.setSubsettingClause(null);
                // Retrieve dataset metadata information for indexer
                dataset.setMetadata(accessRightService.retrieveDatasetMetadata(dataset.getIpId()));
                // update dataset groups
                for (Entry<String, DataObjectGroup> entry : dataset.getMetadata().getDataObjectsGroupsMap()
                        .entrySet()) {
                    // remove group if no access
                    if (!entry.getValue().getDatasetAccess()) {
                        dataset.getGroups().remove(entry.getKey());
                    } else { // add (or let) group if FULL_ACCESS or RESTRICTED_ACCESS
                        dataset.getGroups().add(entry.getKey());
                    }
                }
            } 
            // Then save entity
            LOGGER.debug("Saving entity {}", entity);
            // If lastUpdateDate is provided, this means that update comes from an ingestion, in this case all data
            // objects must be updated.
            // If lastUpdatedDate isn't provided it means update come from a change into dataset
            // (cf. DatasetCrawlerService.handle(...)) and so it is necessary to check if differences exist between
            // previous version of dataset and current one.
            // It may also mean that it comes from a first ingestion. In this case, lastUpdateDate is null but all
            // data objects must be updated
            boolean needAssociatedDataObjectsUpdate = (lastUpdateDate != null) || forceAssociatedEntitiesUpdate;
            // A dataset change may need associated data objects update
            if (!needAssociatedDataObjectsUpdate && (entity instanceof Dataset)) {
                Dataset dataset = (Dataset) entity;
                needAssociatedDataObjectsUpdate |= needAssociatedDataObjectsUpdate(dataset,
                                                                                   esRepos.get(tenant, dataset));
            }
            boolean created = esRepos.save(tenant, entity);
            LOGGER.debug("Elasticsearch saving result : {}", created);
            if ((entity instanceof Dataset) && needAssociatedDataObjectsUpdate) {
                // Subsetting clause is needed by many things
                ((Dataset) entity).setSubsettingClause(savedSubsettingClause);
                manageDatasetUpdate((Dataset) entity, lastUpdateDate, updateDate, dsiId);
            }
        }
        LOGGER.info(ipId.toString() + " managed into Elasticsearch");
    }

    /**
     * Compare new dataset and current indexed one and determine if an update of all associated data objects is needed
     * or no
     */
    private boolean needAssociatedDataObjectsUpdate(Dataset newDataset, Dataset curDataset) {
        if (curDataset == null) {
            return true;
        }

        if (newDataset == null) {
            return false;
        }

        boolean need = false;
        if (newDataset.getOpenSearchSubsettingClause() != null) {
            need = !newDataset.getOpenSearchSubsettingClause().equals(curDataset.getOpenSearchSubsettingClause());
        } else if (curDataset.getOpenSearchSubsettingClause() != null) {
            need = true;
        }

        Map<String, DataObjectGroup> curentMetadata = curDataset.getMetadata() != null
                ? curDataset.getMetadata().getDataObjectsGroupsMap()
                : null;
        Map<String, DataObjectGroup> newMetadata = newDataset.getMetadata() != null
                ? newDataset.getMetadata().getDataObjectsGroupsMap()
                : null;
        if (curentMetadata != null) {
            need = need || !curentMetadata.equals(newMetadata);
        } else {
            need = true;
        }

        return need;
    }

    /**
     * Search and update associated dataset data objects (ie remove dataset IpId from tags)
     *
     * @param tenant concerned tenant
     * @param ipId   dataset identifier
     */
    private void manageDatasetDelete(String tenant, String ipId, String dsiId) {
        // Search all DataObjects tagging this Dataset (only DataObjects because all other entities are already managed
        // with the system Postgres/RabbitMQ)
        sendDataSourceMessage(String.format("      Searching for all data objects tagging dataset IP_ID %s", ipId),
                              dsiId);
        AtomicInteger objectsCount = new AtomicInteger(0);
        ICriterion taggingObjectsCrit = ICriterion.eq("tags", ipId);

        Set<DataObject> toSaveObjects = new HashSet<>();
        OffsetDateTime updateDate = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        // Function to update an object (tags, groups, lastUpdate, ...)
        Consumer<DataObject> updateDataObject = object -> {
            object.removeTags(Arrays.asList(ipId));
            // reset datasetModelIds
            object.getDatasetModelIds().clear();
            // Remove dataset ipId from metadata.groups dans modelIds
            object.getMetadata().removeDatasetIpId(ipId);
            // update groups
            object.setGroups(object.getMetadata().getGroups());
            // update modelIds
            object.setDatasetModelIds(object.getMetadata().getModelIds());
            object.setLastUpdate(updateDate);
            toSaveObjects.add(object);
            if (toSaveObjects.size() == IEsRepository.BULK_SIZE) {
                esRepos.saveBulk(tenant, toSaveObjects);
                objectsCount.addAndGet(toSaveObjects.size());
                toSaveObjects.clear();
            }
        };
        // Apply updateTag function to all tagging objects
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(EntityType.DATA.toString(), DataObject.class);
        addProjectInfos(tenant, searchKey);
        esRepos.searchAll(searchKey, updateDataObject, taggingObjectsCrit);
        // Bulk save remaining objects to save
        if (!toSaveObjects.isEmpty()) {
            esRepos.saveBulk(tenant, toSaveObjects);
            objectsCount.addAndGet(toSaveObjects.size());
        }
        sendDataSourceMessage(String.format("      ...Removed dataset IP_ID from %d data objects tags.",
                                            objectsCount.get()),
                              dsiId);
    }

    /**
     * Search and update associated dataset data objects (ie add dataset IpId into tags)
     *
     * @param dataset concerned dataset
     */
    private void manageDatasetUpdate(Dataset dataset, OffsetDateTime lastUpdateDate, OffsetDateTime updateDate,
            String dsiId) {
        String tenant = runtimeTenantResolver.getTenant();
        sendDataSourceMessage(String
                .format("      Updating dataset %s indexation and all its associated data objects...",
                        dataset.getLabel()), dsiId);
        sendDataSourceMessage(String.format("        Searching for dataset %s associated data objects...",
                                            dataset.getLabel()),
                              dsiId);
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(EntityType.DATA.toString(), DataObject.class);
        addProjectInfos(tenant, searchKey);

        ExecutorService executor = Executors.newFixedThreadPool(1);

        // Create a callable which bulk save into ES a set of data objects
        SaveDataObjectsCallable saveDataObjectsCallable = new SaveDataObjectsCallable(runtimeTenantResolver, esRepos,
                tenant, dataset.getId());
        // Remove association between dataobjects and dataset for all dataobjects which does not match the dataset filter anymore.
        removeOldDatasetDataObjectsAssoc(dataset, updateDate, searchKey, executor, saveDataObjectsCallable, dsiId);

        // Associate dataset to all dataobjets. Associate groups of dataset to the dataobjets through metadata
        addOrUpdateDatasetDataObjectsAssoc(dataset, lastUpdateDate, updateDate, searchKey, executor,
                                           saveDataObjectsCallable, dsiId);

        // Update dataset access groups for dynamic plugin access rights
        manageDatasetUpdateFilteredAccessrights(tenant, dataset, updateDate, executor, saveDataObjectsCallable, dsiId);

        // To remove thread used by executor
        executor.shutdown();
        computeComputedAttributes(dataset, dsiId, tenant);

        esRepos.save(tenant, dataset);
        LOGGER.info("Dataset {} updated", dataset.getId());
        sendDataSourceMessage("      ...Dataset indexation updated.", dsiId);
    }

    private void addProjectInfos(String tenant, SimpleSearchKey<DataObject> searchKey) {
        searchKey.setSearchIndex(tenant);
        searchKey.setCrs(projectGeoSettings.getCrs());
    }

    /**
     * Handle Access rights filter for the given dataset. An Access right filter is an accessRight with a {@link IDataObjectAccessFilterPlugin}.
     */
    private void manageDatasetUpdateFilteredAccessrights(String tenant, Dataset dataset, OffsetDateTime updateDate,
            ExecutorService executor, SaveDataObjectsCallable saveDataObjectsCallable, String dsiId) {
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(EntityType.DATA.toString(), DataObject.class);
        addProjectInfos(tenant, searchKey);
        // handle association between dataobjects and groups for all access rights set by plugin
        for (DataObjectGroup group : dataset.getMetadata().getDataObjectsGroupsMap().values()) {
            // If access to the dataset is allowed and a plugin access filter is set on dataobject metadata, calculate which dataObjects are in the given group
            if (group.getDatasetAccess() && (group.getMetaDataObjectAccessFilterPluginId() != null)) {
                try {
                    IDataObjectAccessFilterPlugin plugin = pluginService
                            .getPlugin(group.getMetaDataObjectAccessFilterPluginId());
                    ICriterion searchFilter = plugin.getSearchFilter();
                    if (searchFilter != null) {
                        removeOldDataObjectsGroupAssoc(dataset, updateDate, searchKey, executor,
                                                       saveDataObjectsCallable, dsiId, group.getGroupName(),
                                                       searchFilter);
                        // Handle specific dataobjet groups by access filter plugin
                        addOrUpdateDataObectGroupAssoc(dataset, updateDate, searchKey, executor,
                                                       saveDataObjectsCallable, dsiId, group.getGroupName(),
                                                       searchFilter);
                    }
                } catch (ModuleException | NotAvailablePluginConfigurationException e) {
                    // Plugin conf doesn't exists anymore, so remove all group assoc
                    removeOldDataObjectsGroupAssoc(dataset, updateDate, searchKey, executor, saveDataObjectsCallable,
                                                   dsiId, group.getGroupName(), ICriterion.all());
                }
            }
        }
    }

    /**
     * Manage computed attributes computation
     *
     * @param dataset concerned dataset
     * @param dsiId   can be null (in this case, no notification is sent)
     */
    @Override
    public void computeComputedAttributes(Dataset dataset, String dsiId, String tenant) {
        // lets compute computed attributes from the dataset model
        Set<IComputedAttribute<Dataset, ?>> computationPlugins = entitiesService.getComputationPlugins(dataset);
        LOGGER.info("Starting parallel computing of {} attributes (dataset {})...", computationPlugins.size(),
                    dataset.getId());

        sendDataSourceMessage(String.format("        Starting computing of %d attributes...",
                                            computationPlugins.size()),
                              dsiId);
        computationPlugins.parallelStream().forEach(p -> {
            runtimeTenantResolver.forceTenant(tenant);
            p.compute(dataset);
        });
        // Once computations has been done, associated attributes are created or updated
        createComputedAttributes(dataset, computationPlugins);

        List<IComputedAttribute<Dataset, ?>> ll = new ArrayList<>(computationPlugins);
        ll.stream()
                .forEach(comAtt -> LOGGER.info("attribute {} is computed", comAtt.getAttributeToCompute().getName()));

        LOGGER.info("...computing OK");
        sendDataSourceMessage(String.format("        ...Computing ended.", computationPlugins.size()), dsiId);
    }

    /**
     * Associate all DATA entities matching the subsetting clause to the given DATASET entity and dataset groups.<br/>
     * Only groups with no {@link AccessLevel#CUSTOM_ACCESS} are associated the the dataobjects in this method.<br/>
     * The association is done by the {@link DataObjectUpdater} consumer.<br/>
     * To handle the groups with {@link AccessLevel#CUSTOM_ACCESS} see {@link EntityIndexerService#addOrUpdateDataObectGroupAssoc}.<br/>
     * <b>NOTE</b> : The subsetting clause to find DATA entities is computed by adding dataset subsetting clause and "lastUpdate > lastUpdateDate parameter".<br/>
     *
     * @param dataset                 {@link Dataset} to associate to DATA entities
     * @param lastUpdateDate          {@link OffsetDateTime}. If not null, add a datatime criterion in the subsesstin clause
     *                                to find only DATA with a lastUpdateDate greter than this parameter
     * @param updateDate              {@link OffsetDateTime} of the current update process
     * @param searchKey               {@link SimpleSearchKey} used to run elasticsearch searh of DATA entities to update
     * @param executor                {@link ExecutorService}
     * @param saveDataObjectsCallable {@link SaveDataObjectsCallable} used to save data
     * @param dsiId                   {@link DatasourceIngestion} identifier
     */
    private void addOrUpdateDatasetDataObjectsAssoc(Dataset dataset, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate, SimpleSearchKey<DataObject> searchKey, ExecutorService executor,
            SaveDataObjectsCallable saveDataObjectsCallable, String dsiId) {
        // A set used to accumulate data objects to save into ES
        HashSet<DataObject> toSaveObjects = new HashSet<>();
        sendDataSourceMessage("          Adding or updating dataset data objects association...", dsiId);
        // Create an updater to be executed on each data object of dataset subsetting criteria results
        DataObjectUpdater dataObjectUpdater = new DataObjectUpdater(dataset, updateDate, toSaveObjects,
                saveDataObjectsCallable, executor);
        ICriterion subsettingCrit = dataset.getSubsettingClause();
        // Add lastUpdate restriction if a date is provided
        if (lastUpdateDate != null) {
            subsettingCrit = ICriterion.and(subsettingCrit, ICriterion.gt(Dataset.LAST_UPDATE, lastUpdateDate));
        }
        esRepos.searchAll(searchKey, dataObjectUpdater, subsettingCrit);
        // Saving remaining objects...
        dataObjectUpdater.finalSave();
        sendDataSourceMessage(String.format("          ...%d data objects dataset association saved.",
                                            dataObjectUpdater.getObjectsCount()),
                              dsiId);
    }

    /**
     * Associates all DATA entities matching the subsetting clause to the given DATASET groups with {@link AccessLevel#CUSTOM_ACCESS}.<br/>
     * The association is done by the {@link DataObjectGroupAssocUpdater} consumer.<br/>
     *
     * @param dataset                 {@link Dataset} to associate to DATA entities
     * @param updateDate              {@link OffsetDateTime} of the current update process
     * @param searchKey               {@link SimpleSearchKey} used to run elasticsearch searh of DATA entities to update
     * @param executor                {@link ExecutorService}
     * @param saveDataObjectsCallable {@link SaveDataObjectsCallable} used to save data
     * @param dsiId                   {@link DatasourceIngestion} identifier
     * @param groupName               Name of the group to associate to DATA entities.
     * @param groupSubsettingClause   {@link ICriterion} group subsetting clause. Caculate by {@link IDataObjectAccessFilterPlugin} plugin.
     */
    private void addOrUpdateDataObectGroupAssoc(Dataset dataset, OffsetDateTime updateDate,
            SimpleSearchKey<DataObject> searchKey, ExecutorService executor,
            SaveDataObjectsCallable saveDataObjectsCallable, String dsiId, String groupName,
            ICriterion groupSubsettingClause) {
        // A set used to accumulate data objects to save into ES
        HashSet<DataObject> toSaveObjects = new HashSet<>();
        sendDataSourceMessage(String.format("          Adding or Updating data objects group <%s> association...",
                                            groupName),
                              dsiId);
        ICriterion subsettingCrit = dataset.getSubsettingClause();
        // First : Retrieve objects associated  matching the groupSubsettingClause
        subsettingCrit = ICriterion.and(subsettingCrit, groupSubsettingClause);
        // For each objet remove group if not associated throught an other dataset
        DataObjectGroupAssocUpdater dataObjectAssocUpdater = new DataObjectGroupAssocUpdater(dataset, updateDate,
                toSaveObjects, saveDataObjectsCallable, executor, groupName);
        esRepos.searchAll(searchKey, dataObjectAssocUpdater, subsettingCrit);
        // Saving remaining objects...
        dataObjectAssocUpdater.finalSave();
        sendDataSourceMessage(String.format("          ...%d data objects group <%s> association saved.",
                                            dataObjectAssocUpdater.getObjectsCount(), groupName),
                              dsiId);
    }

    /**
     * Remove association between DATASET entity and DATA entities that are no more into the subsetting clause.<br/>
     * Only groups with no {@link AccessLevel#CUSTOM_ACCESS} are dissociated the the dataobjects in this method.<br/>
     * The dissociation is done by the {@link DataObjectAssocRemover} consumer.<br/>
     * To handle the groups with {@link AccessLevel#CUSTOM_ACCESS} see {@link EntityIndexerService#removeOldDataObjectsGroupAssoc}.<br/>
     *
     * @param dataset                 {@link Dataset} to associate to DATA entities
     * @param updateDate              {@link OffsetDateTime} of the current update process
     * @param searchKey               {@link SimpleSearchKey} used to run elasticsearch searh of DATA entities to update
     * @param executor                {@link ExecutorService}
     * @param saveDataObjectsCallable {@link SaveDataObjectsCallable} used to save data
     * @param dsiId                   {@link DatasourceIngestion} identifier
     */
    private void removeOldDatasetDataObjectsAssoc(Dataset dataset, OffsetDateTime updateDate,
            SimpleSearchKey<DataObject> searchKey, ExecutorService executor,
            SaveDataObjectsCallable saveDataObjectsCallable, String dsiId) {
        // A set used to accumulate data objects to save into ES
        HashSet<DataObject> toSaveObjects = new HashSet<>();
        sendDataSourceMessage("          Removing old dataset data objects association...", dsiId);
        // First : remove association between dataset and data objects for data objects that are no more associated to
        // new subsetting clause so search data objects that are tagged with dataset IPID and with NOT(user subsetting
        // clause)
        ICriterion oldAssociatedObjectsCrit = ICriterion.and(ICriterion.eq("tags", dataset.getIpId().toString()),
                                                             ICriterion.not(dataset.getUserSubsettingClause()));
        // Create a Consumer to be executed on each data object of dataset subsetting criteria results
        DataObjectAssocRemover dataObjectAssocRemover = new DataObjectAssocRemover(dataset, updateDate, toSaveObjects,
                saveDataObjectsCallable, executor);
        esRepos.searchAll(searchKey, dataObjectAssocRemover, oldAssociatedObjectsCrit);
        // Saving remaining objects...
        dataObjectAssocRemover.finalSave();
        sendDataSourceMessage(String.format("          ...%d data objects dataset association removed.",
                                            dataObjectAssocRemover.getObjectsCount()),
                              dsiId);
    }

    /**
     * Remove association between DATASET entity and DATA entities that are no more into the subsetting clause.<br/>
     * Only groups with {@link AccessLevel#CUSTOM_ACCESS} are dissociated the the dataobjects in this method.<br/>
     * The dissociation is done by the {@link DataObjectGroupAssocRemover} consumer.<br/>
     * To handle the groups with {@link AccessLevel#CUSTOM_ACCESS} see {@link EntityIndexerService#removeOldDataObjectsGroupAssoc}.<br/>
     *
     * @param dataset                 {@link Dataset} to associate to DATA entities
     * @param updateDate              {@link OffsetDateTime} of the current update process
     * @param searchKey               {@link SimpleSearchKey} used to run elasticsearch searh of DATA entities to update
     * @param executor                {@link ExecutorService}
     * @param saveDataObjectsCallable {@link SaveDataObjectsCallable} used to save data
     * @param dsiId                   {@link DatasourceIngestion} identifier
     */
    private void removeOldDataObjectsGroupAssoc(Dataset dataset, OffsetDateTime updateDate,
            SimpleSearchKey<DataObject> searchKey, ExecutorService executor,
            SaveDataObjectsCallable saveDataObjectsCallable, String dsiId, String groupName,
            ICriterion groupSubsettingClause) {
        // A set used to accumulate data objects to save into ES
        HashSet<DataObject> toSaveObjects = new HashSet<>();
        sendDataSourceMessage(String.format("          Removing old data objects group <%s> association...", groupName),
                              dsiId);
        // First : Retrieve objects associated to given group and not matching the groupSubsettingClause
        ICriterion oldAssociatedObjectsCrit = ICriterion.and(ICriterion.eq("tags", dataset.getIpId().toString()),
                                                             ICriterion.contains("groups", groupName),
                                                             ICriterion.not(groupSubsettingClause));
        // For each objet remove group if not associated throught an other dataset
        DataObjectGroupAssocRemover dataObjectAssocRemover = new DataObjectGroupAssocRemover(dataset, updateDate,
                toSaveObjects, saveDataObjectsCallable, executor, groupName);
        esRepos.searchAll(searchKey, dataObjectAssocRemover, oldAssociatedObjectsCrit);
        // Saving remaining objects...
        dataObjectAssocRemover.finalSave();
        sendDataSourceMessage(String.format("          ...%d data objects group <%s> association removed.",
                                            dataObjectAssocRemover.getObjectsCount(), groupName),
                              dsiId);
    }

    /**
     * Create computed attributes from computation
     */
    private void createComputedAttributes(Dataset dataset, Set<IComputedAttribute<Dataset, ?>> computationPlugins) {
        // for each computation plugin lets add the computed attribute
        for (IComputedAttribute<Dataset, ?> plugin : computationPlugins) {
            AbstractAttribute<?> attributeToAdd = plugin.accept(new AttributeBuilderVisitor());
            if (attributeToAdd instanceof ObjectAttribute) {
                ObjectAttribute attrInFragment = (ObjectAttribute) attributeToAdd;
                // the attribute is inside a fragment so lets find the right one to add the attribute inside it
                Optional<AbstractAttribute<?>> candidate = dataset.getProperties().stream()
                        .filter(attr -> (attr instanceof ObjectAttribute)
                                && attr.getName().equals(attrInFragment.getName()))
                        .findFirst();
                if (candidate.isPresent()) {
                    Set<AbstractAttribute<?>> properties = ((ObjectAttribute) candidate.get()).getValue();
                    // the fragment is already here, lets remove the old properties if they exist
                    properties.removeAll(attrInFragment.getValue());
                    // and now set the new ones
                    properties.addAll(attrInFragment.getValue());
                } else {
                    // the fragment is not here so lets create it by adding it
                    dataset.addProperty(attrInFragment);
                }
            } else {
                // the attribute is not inside a fragment so lets remove the old one if it exists and add the new one to
                // the root
                dataset.removeProperty(attributeToAdd);
                dataset.addProperty(attributeToAdd);
            }
        }
    }

    /**
     * Create ES index with tenant name and geometry mapping if needed
     *
     * @return true if index has been created, false if it already existed
     */
    @Override
    public boolean createIndexIfNeeded(String tenant) {
        if (esRepos.indexExists(tenant)) {
            return false;
        }
        esRepos.createIndex(tenant);
        return true;
    }

    @Override
    public boolean deleteIndex(String tenant) {
        if (!esRepos.indexExists(tenant)) {
            return false;
        }
        esRepos.deleteIndex(tenant);
        return true;
    }

    @Override
    @MultitenantTransactional
    public void updateDatasets(String tenant, Collection<Dataset> datasets, OffsetDateTime lastUpdateDate,
            boolean forceDataObjectsUpdate, String dsiId) throws ModuleException {
        OffsetDateTime now = OffsetDateTime.now();

        for (Dataset dataset : datasets) {
            sendDataSourceMessage(String.format("  Updating dataset %s...", dataset.getLabel()), dsiId);
            updateEntityIntoEs(tenant, dataset.getIpId(), lastUpdateDate, now, forceDataObjectsUpdate, dsiId);
            sendDataSourceMessage(String.format("  ...Dataset %s updated.", dataset.getLabel()), dsiId);
        }
    }

    @Override
    public void createNotificationForAdmin(String tenant, String title, String message, NotificationLevel level) {
        notifClient.notify(message, title, level, DefaultRole.PROJECT_ADMIN);
    }

    /**
     * Validate given DataObject. If no error, add it to given set else log validation errors
     */
    private void validateDataObject(Set<DataObject> toSaveObjects, DataObject dataObject, BulkSaveResult bulkSaveResult,
            StringBuilder buf, Long datasourceId) {
        Errors errorsObject = new MapBindingResult(new HashMap<>(), dataObject.getIpId().toString());
        List<String> errors = null;
        // If some validation errors occur, don't index data object
        try {
            dataObjectService.validate(dataObject, errorsObject, false);
        } catch (EntityInvalidException e) {
            // If such an exception has been thrown, it contains all errors ( as a List<String>) else errors are
            // described into errorsObject
            errors = e.getMessages();
        }
        // No exception thrown but still validation errors
        if ((errors == null) && errorsObject.hasErrors()) {
            errors = toErrors(errorsObject);
        }
        // No error => dataObject is valid
        if (errors == null) {
            toSaveObjects.add(dataObject);
        } else {
            // Validation error
            StringBuilder dataObjectBuffer = new StringBuilder("Data object with id '");
            dataObjectBuffer.append(dataObject.getDocId()).append("' not indexed due to ");
            dataObjectBuffer.append(errors.size()).append(" validation error(s):\n");
            dataObjectBuffer.append(errors.stream().collect(Collectors.joining("\n")));
            String msg = dataObjectBuffer.toString();
            // Log error msg
            LOGGER.warn(msg);
            // Append error msg to buffer
            buf.append("\n").append(msg);
            // Add data object in error into summary result
            bulkSaveResult.addInErrorDoc(dataObject.getDocId(), new EntityInvalidException(msg));
        }
    }

    @Override
    public BulkSaveResult createDataObjects(String tenant, String datasourceId, OffsetDateTime now,
            List<DataObject> objects) {
        StringBuilder buf = new StringBuilder();
        BulkSaveResult bulkSaveResult = new BulkSaveResult();
        // For all objects, it is necessary to set datasourceId, creation date AND to validate them
        OffsetDateTime creationDate = now;
        Set<DataObject> toSaveObjects = new HashSet<>();
        for (DataObject dataObject : objects) {
            dataObject.setDataSourceId(datasourceId);
            dataObject.setCreationDate(creationDate);
            dataObject.setLastUpdate(creationDate);
            if (Strings.isNullOrEmpty(dataObject.getLabel())) {
                dataObject.setLabel(dataObject.getIpId().toString());
            }
            // Validate data object
            validateDataObject(toSaveObjects, dataObject, bulkSaveResult, buf, Long.parseLong(datasourceId));
        }
        esRepos.saveBulk(tenant, bulkSaveResult, toSaveObjects, buf);
        publishEventsAndManageErrors(tenant, datasourceId, buf, bulkSaveResult);

        return bulkSaveResult;
    }

    @Override
    public BulkSaveResult mergeDataObjects(String tenant, String datasourceId, OffsetDateTime now,
            List<DataObject> objects) {
        StringBuilder buf = new StringBuilder();
        BulkSaveResult bulkSaveResult = new BulkSaveResult();
        // Set of data objects to be saved (depends on existence of data objects into ES)
        Set<DataObject> toSaveObjects = new HashSet<>();

        for (DataObject dataObject : objects) {
            mergeDataObject(tenant, datasourceId, now, dataObject);
            validateDataObject(toSaveObjects, dataObject, bulkSaveResult, buf, Long.parseLong(datasourceId));
        }
        esRepos.saveBulk(tenant, bulkSaveResult, toSaveObjects, buf);
        publishEventsAndManageErrors(tenant, datasourceId, buf, bulkSaveResult);
        return bulkSaveResult;
    }

    /**
     * Merge data object with current indexed one if it does exist
     */
    private void mergeDataObject(String tenant, String datasourceId, OffsetDateTime now, DataObject dataObject) {
        DataObject curObject = esRepos.get(tenant, dataObject);
        // Be careful : in some case, some data objects from another datasource can be retrieved (AipDataSource
        // search objects from storage only using tags so if this tag has been used
        // if current object does already exist into ES, the new one wins. It is then mandatory to retrieve from
        // current creationDate, groups, tags and modelIds
        if (curObject != null) {
            dataObject.setCreationDate(curObject.getCreationDate());
            dataObject.setMetadata(curObject.getMetadata());
            dataObject.setGroups(dataObject.getMetadata().getGroups());
            dataObject.setDatasetModelIds(dataObject.getMetadata().getModelIds());
            // In case to ingest object has new tags
            if (!curObject.getTags().isEmpty()) {
                dataObject.addTags(curObject.getTags());
            }
        } else { // else it must be created
            dataObject.setCreationDate(now);
        }
        // Don't forget to update lastUpdate
        dataObject.setLastUpdate(now);
        // Don't forget to set datasourceId
        dataObject.setDataSourceId(datasourceId);
        if (Strings.isNullOrEmpty(dataObject.getLabel())) {
            dataObject.setLabel(dataObject.getIpId().toString());
        }
    }

    /**
     * Publish events concerning data objects indexation status (indexed or in error), notify admin and update detailed
     * save bulk result message in case of errors
     */
    private void publishEventsAndManageErrors(String tenant, String datasourceId, StringBuilder buf,
            BulkSaveResult bulkSaveResult) {
        if (bulkSaveResult.getSavedDocsCount() != 0) {
            // Ingest needs to know when an internal DataObject is indexed (if DataObject is not internal, it doesn't
            // care)
            publisher.publish(new BroadcastEntityEvent(EventType.INDEXED, bulkSaveResult.getSavedDocIdsStream()
                    .map(UniformResourceName::fromString).toArray(UniformResourceName[]::new)));
        }
        if (bulkSaveResult.getInErrorDocsCount() > 0) {
            // Ingest also needs to know when an internal DataObject cannot be indexed (if DataObject is not internal,
            // it doesn't care)
            publisher.publish(new BroadcastEntityEvent(EventType.INDEX_ERROR, bulkSaveResult.getInErrorDocIdsStream()
                    .map(UniformResourceName::fromString).toArray(UniformResourceName[]::new)));
        }
        // If there are errors, notify Admin
        if (buf.length() > 0) {
            // Also add detailed message to datasource ingestion
            DatasourceIngestion dsIngestion = dsIngestionRepository.findById(datasourceId).get();
            String notifTitle = String.format("'%s' Datasource ingestion error", dsIngestion.getLabel());
            self.createNotificationForAdmin(tenant, notifTitle, buf.toString(), NotificationLevel.ERROR);
            bulkSaveResult.setDetailedErrorMsg(buf.toString());
        }
    }

    @Override
    public boolean deleteDataObject(String tenant, String ipId) {
        return esRepos.delete(tenant, EntityType.DATA.toString(), ipId);
    }

    @Override
    public void updateAllDatasets(String tenant) throws ModuleException {
        updateDatasets(tenant, datasetService.findAll(), null, true, null);
    }

    @Override
    public void updateAllCollections(String tenant) throws ModuleException {
        OffsetDateTime now = OffsetDateTime.now();
        for (fr.cnes.regards.modules.dam.domain.entities.Collection col : collectionService.findAll()) {
            updateEntityIntoEs(tenant, col.getIpId(), null, now, false, null);
        }
    }

    /**
     * Send a message to IngesterService (or whoever want to listen to it) concerning given datasourceIngestionId
     *
     * @param dsId {@link DatasourceIngestion} id
     */
    public void sendDataSourceMessage(String message, String dsId) {
        if (dsId != null) {
            String msg = String.format("%s: %s",
                                       ISO_TIME_UTC.format(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC)),
                                       message);
            eventPublisher.publishEvent(new DataSourceMessageEvent(this, runtimeTenantResolver.getTenant(), msg, dsId));
        }
    }

    /**
    TODO
    private class AIPEventHandler implements IHandler<AIPEvent> {
    
        @Override
        public void handle(TenantWrapper<AIPEvent> wrapper) {
            AIPEvent event = wrapper.getContent();
            if (event.getAipState() == AIPState.DELETED) {
                runtimeTenantResolver.forceTenant(wrapper.getTenant());
                try {
                    deleteDataObject(wrapper.getTenant(), event.getAipId());
                } catch (RsRuntimeException e) {
                    String msg = String.format("Cannot delete DataObject (%s)", event.getAipId());
                    LOGGER.error(msg, e);
                }
    
            }
        }
    }
    */
}
