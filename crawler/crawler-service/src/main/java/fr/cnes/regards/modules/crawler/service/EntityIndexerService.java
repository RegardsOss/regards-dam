package fr.cnes.regards.modules.crawler.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.google.common.base.Strings;
import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.modules.crawler.service.consumer.DataObjectAssocRemover;
import fr.cnes.regards.modules.crawler.service.consumer.DataObjectUpdater;
import fr.cnes.regards.modules.crawler.service.consumer.SaveDataObjectsCallable;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessLevel;
import fr.cnes.regards.modules.dataaccess.service.AccessGroupService;
import fr.cnes.regards.modules.dataaccess.service.IAccessRightService;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.event.BroadcastEntityEvent;
import fr.cnes.regards.modules.entities.domain.event.EventType;
import fr.cnes.regards.modules.entities.service.IEntitiesService;
import fr.cnes.regards.modules.entities.service.visitor.AttributeBuilderVisitor;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.notification.client.INotificationClient;
import fr.cnes.regards.modules.notification.domain.NotificationType;
import fr.cnes.regards.modules.notification.domain.dto.NotificationDTO;
import fr.cnes.regards.modules.storage.domain.AIPState;
import fr.cnes.regards.modules.storage.domain.event.AIPEvent;

/**
 * @author oroussel
 */
@Service
public class EntityIndexerService implements IEntityIndexerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityIndexerService.class);

    /**
     * Current tenant resolver
     */
    @Autowired
    protected IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private IEntitiesService entitiesService;

    @Autowired
    protected IEsRepository esRepos;

    @Autowired
    private IAccessRightService accessRightService;

    @Autowired
    private IPublisher publisher;

    @Autowired
    private ISubscriber subscriber;

    @PersistenceContext
    private EntityManager em;

    /**
     * Validator used to validate each DataObject creation before its indexing into ElasticSearch
     */
    @Autowired
    private Validator validator;

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

    private IModelAttrAssocService modelService;

    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        subscriber.subscribeTo(AIPEvent.class, new AIPEventHandler());
    }

    /**
     * Load given entity from database and update Elasticsearch
     * @param tenant concerned tenant (also index intoES)
     * @param ipId concerned entity IpId
     * @param lastUpdateDate for dataset entity, if this date is provided, only more recent data objects must be taken
     * into account
     */
    @Override
    public void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate) {
        LOGGER.info("Updating {}", ipId.toString());
        runtimeTenantResolver.forceTenant(tenant);
        AbstractEntity entity = entitiesService.loadWithRelations(ipId);
        // If entity does no more exist in database, it must be deleted from ES
        if (entity == null) {
            LOGGER.debug("Entity is null !!");
            if (ipId.getEntityType() == EntityType.DATASET) {
                manageDatasetDelete(tenant, ipId.toString());
            }
            esRepos.delete(tenant, ipId.getEntityType().toString(), ipId.toString());
        } else { // entity has been created or updated, it must be saved into ES
            createIndexIfNeeded(tenant);
            ICriterion savedSubsettingClause = null;
            // Remove parameters of dataset datasource to avoid expose security values
            if (entity instanceof Dataset) {
                Dataset dataset = (Dataset) entity;
                // entity must be detached else Hibernate tries to commit update (datasource is cascade.DETACHED)
                em.detach(entity);
                if (dataset.getDataSource() != null) {
                    dataset.getDataSource().setParameters(null);
                }
                // Subsetting clause must not be jsonify into Elasticsearch
                savedSubsettingClause = dataset.getSubsettingClause();
                dataset.setSubsettingClause(null);
                // Retrieve map of { group, AccessLevel }
                Map<String, AccessLevel> volatileMap;
                try {
                    volatileMap = accessRightService.retrieveGroupAccessLevelMap(dataset.getIpId());
                } catch (EntityNotFoundException e) {
                    volatileMap = Collections.emptyMap();
                }
                // Need to be final to be used into following lambda
                final Map<String, AccessLevel> map = volatileMap;
                // Compute groups for associated data objects
                dataset.getMetadata().setDataObjectsGroups(dataset.getGroups().stream()
                                                                   .filter(group -> map.containsKey(group)
                                                                           && map.get(group) == AccessLevel.FULL_ACCESS)
                                                                   .collect(Collectors.toSet()));
                // update dataset groups
                for (Map.Entry<String, AccessLevel> entry : map.entrySet()) {
                    // remove group if no access
                    if (entry.getValue() == AccessLevel.NO_ACCESS) {
                        dataset.getGroups().remove(entry.getKey());
                    } else { // add (or let) group if FULL_ACCESS or RESTRICTED_ACCESS
                        dataset.getGroups().add(entry.getKey());
                    }
                }
            } else if (entity instanceof Document) {
                // Add the internal AccessGroup to allow everyone to see this document
                entity.getGroups().add(AccessGroupService.ACCESS_GROUP_PUBLIC_DOCUMENTS);
            }
            // Then save entity
            LOGGER.debug("Saving entity {}", entity);
            // If lastUpdateDate is provided, this means that update comes from an ingestion, in this case all data
            // objects must be updated
            boolean needAssociatedDataObjectsUpdate = (lastUpdateDate != null);
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
                manageDatasetUpdate((Dataset) entity, lastUpdateDate, updateDate);
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
        return !newDataset.getOpenSearchSubsettingClause().equals(curDataset.getOpenSearchSubsettingClause())
                || !newDataset.getMetadata().getDataObjectsGroups()
                .equals(curDataset.getMetadata().getDataObjectsGroups());
    }

    /**
     * Search and update associated dataset data objects (ie remove dataset IpId from tags)
     * @param tenant concerned tenant
     * @param ipId dataset identifier
     */
    private void manageDatasetDelete(String tenant, String ipId) {
        // Search all DataObjects tagging this Dataset (only DataObjects because all other entities are already managed
        // with the system Postgres/RabbitMQ)
        ICriterion taggingObjectsCrit = ICriterion.eq("tags", ipId);

        Set<DataObject> toSaveObjects = new HashSet<>();
        OffsetDateTime updateDate = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        // Function to update an object (tags, groups, lastUpdate, ...)
        Consumer<DataObject> updateDataObject = object -> {
            object.getTags().remove(ipId);
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
                toSaveObjects.clear();
            }
        };
        // Apply updateTag function to all tagging objects
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(tenant, EntityType.DATA.toString(),
                                                                      DataObject.class);
        esRepos.searchAll(searchKey, updateDataObject, taggingObjectsCrit);
        // Bulk save remaining objects to save
        if (!toSaveObjects.isEmpty()) {
            esRepos.saveBulk(tenant, toSaveObjects);
        }
    }

    /**
     * Search and update associated dataset data objects (ie add dataset IpId into tags)
     * @param dataset concerned dataset
     */
    private void manageDatasetUpdate(Dataset dataset, OffsetDateTime lastUpdateDate, OffsetDateTime updateDate) {
        String tenant = runtimeTenantResolver.getTenant();
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(tenant, EntityType.DATA.toString(),
                                                                      DataObject.class);
        // A set used to accumulate data objects to save into ES
        HashSet<DataObject> toSaveObjects = new HashSet<>();
        ExecutorService executor = Executors.newFixedThreadPool(1);

        // Create a callable which bulk save into ES a set of data objects
        SaveDataObjectsCallable saveDataObjectsCallable = new SaveDataObjectsCallable(runtimeTenantResolver, esRepos,
                                                                                      tenant, dataset.getId());
        removeOldDatasetDataObjectsAssoc(dataset, updateDate, searchKey, toSaveObjects, executor,
                                         saveDataObjectsCallable);
        addOrUpdateDatasetDataObjectsAssoc(dataset, lastUpdateDate, updateDate, searchKey, toSaveObjects, executor,
                                           saveDataObjectsCallable);

        // lets compute computed attributes from the dataset model
        Set<IComputedAttribute<Dataset, ?>> computationPlugins = entitiesService.getComputationPlugins(dataset);
        LOGGER.info("Starting parallel computing of {} attributes (dataset {})...", computationPlugins.size(),
                    dataset.getId());
        computationPlugins.parallelStream().forEach(p -> {
            runtimeTenantResolver.forceTenant(tenant);
            p.compute(dataset);
        });
        // Once computations has been done, associated attributes are created or updated
        createComputedAttributes(dataset, computationPlugins);
        LOGGER.info("...computing OK");

        esRepos.save(tenant, dataset);
        LOGGER.info("Dataset {} updated", dataset.getId());
    }

    /**
     * Add or update association between dataset and data objects that are into subsetting clause
     */
    private void addOrUpdateDatasetDataObjectsAssoc(Dataset dataset, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate, SimpleSearchKey<DataObject> searchKey, HashSet<DataObject> toSaveObjects,
            ExecutorService executor, SaveDataObjectsCallable saveDataObjectsCallable) {
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
    }

    /**
     * Remove association between dataset and data objects that are no more into subsetting clause
     */
    private void removeOldDatasetDataObjectsAssoc(Dataset dataset, OffsetDateTime updateDate,
            SimpleSearchKey<DataObject> searchKey, HashSet<DataObject> toSaveObjects, ExecutorService executor,
            SaveDataObjectsCallable saveDataObjectsCallable) {
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
                        .filter(attr -> (attr instanceof ObjectAttribute) && attr.getName()
                                .equals(attrInFragment.getName())).findFirst();
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
     * @return true if index has been created, false if it already existed
     */
    @Override
    public boolean createIndexIfNeeded(String tenant) {
        if (esRepos.indexExists(tenant)) {
            return false;
        }
        esRepos.createIndex(tenant);
        String[] types = Arrays.stream(EntityType.values()).map(EntityType::toString)
                .toArray(length -> new String[length]);
        esRepos.setAutomaticDoubleMapping(tenant, types);
        esRepos.setGeometryMapping(tenant, types);
        return true;
    }

    @Override
    @MultitenantTransactional
    public void updateDatasets(String tenant, Set<Dataset> datasets, OffsetDateTime lastUpdateDate,
            boolean forceDataObjectsUpdate) {
        OffsetDateTime now = OffsetDateTime.now();
        datasets.forEach(dataset -> updateEntityIntoEs(tenant, dataset.getIpId(), lastUpdateDate, now));
    }

    @Override
    public int createDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects) {
        StringBuilder buf = new StringBuilder();
        // On all objects, it is necessary to set datasourceId and creation date
        OffsetDateTime creationDate = now;
        Set<DataObject> toSaveObjects = new HashSet<>();
        for (DataObject dataObject : objects) {
            dataObject.setDataSourceId(datasourceId);
            dataObject.setCreationDate(creationDate);
            if (Strings.isNullOrEmpty(dataObject.getLabel())) {
                dataObject.setLabel(dataObject.getIpId().toString());
            }
            validateDataObject(toSaveObjects, dataObject, buf);
        }
        if (buf.length() > 0) {
            self.createNotificationForAdmin(tenant, buf);
        }
        int createdCount = esRepos.saveBulk(tenant, toSaveObjects);
        if (createdCount != 0) {
            publisher.publish(new BroadcastEntityEvent(EventType.INDEXED,
                                                       toSaveObjects.stream().filter(DataObject::containsPhysicalData)
                                                               .map(DataObject::getIpId)
                                                               .toArray(n -> new UniformResourceName[n])));
        }
        return createdCount;
    }

    @Async
    @Override
    public void createNotificationForAdmin(String tenant, CharSequence buf) {
        runtimeTenantResolver.forceTenant(tenant);
        FeignSecurityManager.asSystem();
        NotificationDTO notif = new NotificationDTO(buf.toString(), Collections.emptySet(),
                                                    Collections.singleton(DefaultRole.PROJECT_ADMIN.name()),
                                                    applicationName, "Datasource ingestion error",
                                                    NotificationType.ERROR);
        notifClient.createNotification(notif);
        FeignSecurityManager.reset();
    }

    /**
     * Validate given DataObject. If no error, add it to given set else log validation errors
     */
    private void validateDataObject(Set<DataObject> toSaveObjects, DataObject dataObject, StringBuilder buf) {
        Errors errors = new MapBindingResult(new HashMap<>(), dataObject.getIpId().toString());
        validator.validate(dataObject, errors);
        // If some validation errors occur, don't index data object
        if (errors.getErrorCount() == 0) {
            toSaveObjects.add(dataObject);
        } else {
            buf.append("\n").append(errors.getErrorCount()).append(" validation errors:");
            for (ObjectError objError : errors.getAllErrors()) {
                if (objError instanceof FieldError) {
                    buf.append("\nField error in object ").append(objError.getObjectName());
                    buf.append(" on field '").append(((FieldError) objError).getField());
                    buf.append("', rejected value '").append(((FieldError) objError).getRejectedValue());
                    buf.append("': ").append(((FieldError) objError).getField());
                    buf.append(" ").append(objError.getDefaultMessage());
                } else {
                    buf.append("\n").append(objError.toString());
                }
            }
            LOGGER.warn("Data object not indexed due to {}", buf.toString());
        }
    }

    @Override
    public int mergeDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects) {
        StringBuilder buf = new StringBuilder();
        // Set of data objects to be saved (depends on existence of data objects into ES)
        Set<DataObject> toSaveObjects = new HashSet<>();

        for (DataObject dataObject : objects) {
            DataObject curObject = esRepos.get(tenant, dataObject);
            // if current object does already exist into ES, the new one wins. It is then mandatory to retrieve from
            // current creationDate, groups, tags and modelIds
            if (curObject != null) {
                dataObject.setCreationDate(curObject.getCreationDate());
                dataObject.setMetadata(curObject.getMetadata());
                dataObject.setGroups(dataObject.getMetadata().getGroups());
                dataObject.setDatasetModelIds(dataObject.getMetadata().getModelIds());
                // In case to ingest object has new tags
                dataObject.getTags().addAll(curObject.getTags());
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
            validateDataObject(toSaveObjects, dataObject, buf);
        }
        if (buf.length() > 0) {
            self.createNotificationForAdmin(tenant, buf);
        }
        // Bulk save : toSaveObjects.size() isn't checked because it is more likely that toSaveObjects
        // has same size as page.getContent() or is empty
        int savedCount = esRepos.saveBulk(tenant, toSaveObjects);
        if (savedCount != 0) {
            publisher.publish(new BroadcastEntityEvent(EventType.INDEXED,
                                                       toSaveObjects.stream().filter(DataObject::containsPhysicalData)
                                                               .map(DataObject::getIpId)
                                                               .toArray(n -> new UniformResourceName[n])));
        }
        return savedCount;
    }

    @Override
    public boolean deleteDataObject(String tenant, String ipId) {
        return esRepos.delete(tenant, EntityType.DATA.toString(), ipId);
    }

    private class AIPEventHandler implements IHandler<AIPEvent> {
        @Override
        public void handle(TenantWrapper<AIPEvent> wrapper) {
                AIPEvent event = wrapper.getContent();
                if (event.getAipState() == AIPState.DELETED) {
                    runtimeTenantResolver.forceTenant(wrapper.getTenant());
                    try {
                        deleteDataObject(wrapper.getTenant(), event.getIpId());
                    } catch (RsRuntimeException e) {
                        String msg = String.format("Cannot delete DataObject (%s)", event.getIpId());
                        LOGGER.error(msg, e);
                    }

                }
        }
    }
}
