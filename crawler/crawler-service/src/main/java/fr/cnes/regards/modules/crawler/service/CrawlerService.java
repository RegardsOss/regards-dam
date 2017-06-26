/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import fr.cnes.regards.framework.amqp.IPoller;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.InactiveDatasourceException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.event.EntityEvent;
import fr.cnes.regards.modules.entities.service.IEntitiesService;
import fr.cnes.regards.modules.entities.service.visitor.AttributeBuilderVisitor;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;

/**
 * Crawler service. <b>This service need @EnableAsync at Configuration and is used in conjunction with
 * CrawlerInitializer</b>
 */
@Service// Transactionnal is handle by hand on the right method, do not specify Multitenant or InstanceTransactionnal
public class CrawlerService implements ICrawlerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerService.class);

    /**
     * To avoid CPU overload, a delay is set between each loop of tenants event inspection. This delay is doubled each
     * time no event has been pulled (limited to MAX_DELAY_MS). When an event is pulled (during a tenants event
     * inspection), no wait is done and delay is reset to INITIAL_DELAY_MS
     */
    private static final int INITIAL_DELAY_MS = 1;

    /**
     * To avoid CPU overload, a delay is set between each loop of tenants event inspection. This delay is doubled each
     * time no event has been pulled (limited to MAX_DELAY_MS). When an event is pulled (during a tenants event
     * inspection), no wait is done and delay is reset to INITIAL_DELAY_MS
     */
    private static final int MAX_DELAY_MS = 1000;

    /**
     * All tenants resolver
     */
    @Autowired
    private ITenantResolver tenantResolver;

    /**
     * Current tenant resolver
     */
    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * AMQP poller
     */
    @Autowired
    private IPoller poller;

    @Autowired
    private IEntitiesService entitiesService;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IEsRepository esRepos;

    /**
     * To retrieve ICrawlerService (self) proxy
     */
    @Autowired
    private ApplicationContext applicationContext;

    @PersistenceContext
    private EntityManager em;

    /**
     * Self proxy
     */
    private ICrawlerService self;

    /**
     * Indicate that daemon stop has been asked
     */
    private boolean stopAsked = false;

    /**
     * Current delay between all tenants poll check
     */
    private final AtomicInteger delay = new AtomicInteger(INITIAL_DELAY_MS);

    /**
     * Boolean indicating that a work is scheduled
     */
    private static boolean scheduledWork = false;

    /**
     * Boolean indicating that something has been done
     */
    private static boolean somethingDone = false;

    /**
     * Boolean indicating that something is currently in progress
     */
    private static boolean inProgress = false;

    /**
     * Boolean indicating wether or not crawler service is in "consume only" mode (to be used by tests only)
     */
    private static boolean consumeOnlyMode = false;

    /**
     * Once ICrawlerService bean has been initialized, retrieve self proxy to permit transactional call of doPoll.
     */
    @PostConstruct
    private void init() {
        self = applicationContext.getBean(ICrawlerService.class);
    }

    /**
     * Ask for termination of daemon process
     */
    @PreDestroy
    private void endCrawl() {
        stopAsked = true;
    }

    /**
     * Daemon process. Poll entity events on all tenants and update Elasticsearch to reflect Postgres database
     */
    @Async
    @Override
    public void crawl() {
        delay.set(INITIAL_DELAY_MS);
        // Infinite loop
        while (true) {
            // Manage termination
            if (stopAsked) {
                break;
            }
            boolean atLeastOnePoll = false;
            // For all tenants
            for (String tenant : tenantResolver.getAllActiveTenants()) {
                try {
                    runtimeTenantResolver.forceTenant(tenant);
                    // Try to poll an entity event on this tenant
                    atLeastOnePoll |= self.doPoll();
                } catch (Throwable t) {
                    LOGGER.error("Cannot manage entity event message", t);
                }
                // Reset inProgress AFTER transaction
                inProgress = false;
            }
            // If a poll has been done, don't wait and reset delay to initial value
            if (atLeastOnePoll) {
                delay.set(INITIAL_DELAY_MS);
            } else { // else, wait and double delay for next time (limited to MAX_DELAY)
                try {
                    Thread.sleep(delay.get());
                    delay.set(Math.min(delay.get() * 2, MAX_DELAY_MS));
                } catch (InterruptedException e) {
                    LOGGER.error("Thread sleep interrupted.");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Try to do a transactional poll. If a poll is done but an exception occurs, the transaction is rolbacked and the
     * event is still present into AMQP
     *
     * @return true if a poll has been done, false otherwise
     */
    @Override
    @MultitenantTransactional
    public boolean doPoll() {
        boolean atLeastOnePoll = false;
        // Try to poll an EntityEvent
        TenantWrapper<EntityEvent> wrapper = poller.poll(EntityEvent.class);
        if (wrapper != null) {
            String tenant = wrapper.getTenant();
            LOGGER.info("Received message from tenant {} created at {}...", tenant, wrapper.getDate());
            UniformResourceName[] ipIds = wrapper.getContent().getIpIds();
            if ((ipIds != null) && (ipIds.length != 0)) {
                LOGGER.debug("IpIds received {}", Arrays.toString(ipIds));
                atLeastOnePoll = true;
                // Message consume only, nothing else to be done, returning...
                if (consumeOnlyMode) {
                    LOGGER.debug("CONSUME ONLY MODE TRUE !!!!");
                    return atLeastOnePoll;
                }
                // Only one entity
                if (ipIds.length == 1) {
                    inProgress = true;
                    LOGGER.info("Update entity into Elasticsearch {}", ipIds[0]);
                    updateEntityIntoEs(tenant, ipIds[0], OffsetDateTime.now());
                } else if (ipIds.length > 1) { // several entities at once
                    inProgress = true;
                    updateEntitiesIntoEs(tenant, ipIds, OffsetDateTime.now());
                }
                somethingDone = true;
            }
        }
        return atLeastOnePoll;
    }

    private void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime updateDate) {
        this.updateEntityIntoEs(tenant, ipId, null, updateDate);
    }

    /**
     * Load given entity from database and update Elasticsearch
     *
     * @param tenant concerned tenant (also index intoES)
     * @param ipId concerned entity IpId
     * @param lastUpdateDate for dataset entity, if this date is provided, only more recent data objects must be taken
     * into account
     */
    private void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate) {
        LOGGER.info("received msg for {}", ipId.toString());
        LOGGER.debug("Loading entity {}", ipId);
        AbstractEntity entity = entitiesService.loadWithRelations(ipId);
        // If entity does no more exist in database, it must be deleted from ES
        if (entity == null) {
            LOGGER.debug("Entity is null !!");
            if (ipId.getEntityType() == EntityType.DATASET) {
                manageDatasetDelete(tenant, ipId.toString());
            }
            esRepos.delete(tenant, ipId.getEntityType().toString(), ipId.toString());
        } else { // entity has been created or updated, it must be saved into ES
            // First, check if index exists
            if (!esRepos.indexExists(tenant)) {
                createIndex(tenant);
            }
            // Remove parameters of dataset datasource to avoid expose security values
            if (entity instanceof Dataset) {
                // entity must be detached else Hibernate tries to commit update (datasource is cascade.DETACHed)
                em.detach(entity);
                ((Dataset) entity).getDataSource().setParameters(null);
            }
            // Then save entity
            LOGGER.debug("Saving entity {}", entity);
            boolean created = esRepos.save(tenant, entity);
            LOGGER.debug("Elasticsearch saving result : {}", created);
            if (entity instanceof Dataset) {
                manageDatasetUpdate((Dataset) entity, lastUpdateDate, updateDate);
            }
        }
        LOGGER.info(ipId.toString() + " managed into Elasticsearch");
    }

    private void updateEntitiesIntoEs(String tenant, UniformResourceName[] ipIds, OffsetDateTime updateDate) {
        this.updateEntitiesIntoEs(tenant, ipIds, null, updateDate);
    }

    /**
     * Load given entities from database and update Elasticsearch
     *
     * @param tenant concerned tenant (also index intoES)
     * @param ipIds concerned entity IpIds
     */
    private void updateEntitiesIntoEs(String tenant, UniformResourceName[] ipIds, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate) {
        LOGGER.info("received msg for " + Arrays.toString(ipIds));
        Set<UniformResourceName> toDeleteIpIds = Sets.newHashSet(ipIds);
        List<AbstractEntity> entities = entitiesService.loadAllWithRelations(ipIds);
        entities.forEach(e -> toDeleteIpIds.remove(e.getIpId()));
        // Entities to save
        if (!entities.isEmpty()) {
            if (!esRepos.indexExists(tenant)) {
                createIndex(tenant);
            }
            // Remove pluginConf parameters from datasets to avoid jsonify into Elasticsearch
            entities.stream().filter(e -> e instanceof Dataset).forEach(dataset -> {
                // Don't forget to detach dataset in order to cascade.Detach datasource
                em.detach(dataset);
                ((Dataset) dataset).getDataSource().setParameters(null);
            });
            esRepos.saveBulk(tenant, entities);
            entities.stream().filter(e -> e instanceof Dataset)
                    .forEach(e -> manageDatasetUpdate((Dataset) e, lastUpdateDate, updateDate));
        }
        // Entities to remove
        if (!toDeleteIpIds.isEmpty()) {
            toDeleteIpIds.forEach(ipId -> esRepos.delete(tenant, ipId.getEntityType().toString(), ipId.toString()));
        }
        LOGGER.info(Arrays.toString(ipIds) + " managed into Elasticsearch");
    }

    /**
     * Search and update associated dataset data objects (ie remove dataset IpId from tags)
     *
     * @param tenant concerned tenant
     * @param ipId dataset identifier
     */
    private void manageDatasetDelete(String tenant, String ipId) {
        // Search all DataObjects tagging this Dataset (only DataObjects because all other entities are already managed
        // with the system Postgres/RabbitMQ)
        ICriterion taggingObjectsCrit = ICriterion.eq("tags", ipId);
        // Groups must also be managed so for all objects they have to be completely recomputed (a group can be
        // associated to several datasets). A Multimap { IpId -> groups } is used to avoid calling ES for all
        // datasets associated to an object
        Multimap<String, String> groupsMultimap = HashMultimap.create();
        // Same thing with dataset modelId (several datasets can have same model)
        Map<String, Long> modelIdMap = new HashMap<>();
        // A set containing already known not dataset ipId (to avoid creating UniformResourceName object for all tags
        // each time an object is encountered)
        Set<String> notDatasetIpIds = new HashSet<>();

        Set<DataObject> toSaveObjects = new HashSet<>();
        OffsetDateTime updateDate = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        // Function to update an object (tags, groups, lastUpdate, ...)
        Consumer<DataObject> updateDataObject = object -> {
            object.getTags().remove(ipId);
            // reset groups
            object.getGroups().clear();
            // reset datasetModelIds
            object.getDatasetModelIds().clear();
            // Search on all tags which ones are datasets and retrieve their groups and modelIds
            computeGroupsAndModelIdsFromTags(tenant, groupsMultimap, modelIdMap, notDatasetIpIds, object);
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
     * Compute groups and model ids from tags
     *
     * @param tenant tenant
     * @param groupsMultimap a multimap of { dataset IpId, groups }
     * @param modelIdMap a map of { dataset IpId, dataset model Id }
     * @param notDatasetIpIds a set containing already known not dataset ipIds
     * @param object DataObject to update
     */
    private void computeGroupsAndModelIdsFromTags(String tenant, Multimap<String, String> groupsMultimap,
            Map<String, Long> modelIdMap, Set<String> notDatasetIpIds, DataObject object) {
        // Compute groups from tags
        for (Iterator<String> i = object.getTags().iterator(); i.hasNext(); ) {
            String tag = i.next();
            // already known free tag or other entity than Dataset ipId
            if (notDatasetIpIds.contains(tag)) {
                continue;
            }
            // new tag encountered
            if (!groupsMultimap.containsKey(tag)) {
                // Managing Dataset IpId tag
                if (UniformResourceName.isValidUrn(tag) && (UniformResourceName.fromString(tag).getEntityType()
                        == EntityType.DATASET)) {
                    Dataset dataset = esRepos.get(tenant, EntityType.DATASET.toString(), tag, Dataset.class);
                    // Must not occurs, this means a Dataset has been deleted from ES but not cleaned on all
                    // objects associated to it
                    if (dataset == null) {
                        LOGGER.warn("Dataset {} no more exists, it will be removed from DataObject {} tags", tag,
                                    object.getDocId());
                        i.remove();
                        // In this case, this tag must be managed on all objects so it is not added nor on
                        // notDatasetIpIds nor on groupsMultimap
                    } else { // dataset found, retrieving its groups and add them on groupsMultimap
                        groupsMultimap.putAll(tag, dataset.getGroups());
                        modelIdMap.put(tag, dataset.getModel().getId());
                    }
                } else { // free tag or not dataset tag
                    notDatasetIpIds.add(tag);
                }
            }
            object.getGroups().addAll(groupsMultimap.get(tag));
            object.getDatasetModelIds().add(modelIdMap.get(tag));
        }
    }

    /**
     * Search and update associated dataset data objects (ie add dataset IpId into tags)
     *
     * @param dataset concerned dataset
     */
    private void manageDatasetUpdate(Dataset dataset, OffsetDateTime lastUpdateDate, OffsetDateTime updateDate) {
        ICriterion subsettingCrit = dataset.getSubsettingClause();

        // Add lastUpdate restriction if a date is provided
        if (lastUpdateDate != null) {
            subsettingCrit = ICriterion.and(subsettingCrit, ICriterion.gt(Dataset.LAST_UPDATE, lastUpdateDate));
        }

        String tenant = runtimeTenantResolver.getTenant();
        // To avoid losing time doing same stuf on all objects
        String dsIpId = dataset.getIpId().toString();
        Set<String> groups = dataset.getGroups();
        Long datasetModelId = dataset.getModel().getId();

        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(tenant, EntityType.DATA.toString(),
                                                                      DataObject.class);
        HashSet<DataObject> toSaveObjects = new HashSet<>();
        // Create a one thread pool
        ExecutorService executor = Executors.newFixedThreadPool(1);
        // Create a callable which bulk save into ES a set of data objects
        SaveDataObjectsCallable saveDataObjectsCallable = new SaveDataObjectsCallable(tenant);
        // Add callable to thread pool executor
        Future<Void> saveBulkTask = executor.submit(saveDataObjectsCallable);
        // Lambda to be executed on each data object of dataset subsetting criteria results
        DataObjectUpdater dataObjectUpdater = new DataObjectUpdater(dsIpId, groups, updateDate, datasetModelId,
                                                                    toSaveObjects, saveDataObjectsCallable);
        esRepos.searchAll(searchKey, dataObjectUpdater, subsettingCrit);
        if (!toSaveObjects.isEmpty()) {
            dataObjectUpdater.waitForEndOfTask();
            LOGGER.info("Saving {} data objects...", toSaveObjects.size());
            esRepos.saveBulk(tenant, toSaveObjects);
            LOGGER.info("...data objects saved");
        }

        // lets compute computed attributes from the dataset model
        Set<IComputedAttribute<Dataset, ?>> computationPlugins = entitiesService.getComputationPlugins(dataset);
        LOGGER.info("Starting parallel computing of {} attributes...", computationPlugins.size());
        computationPlugins.parallelStream().forEach(p -> {
            runtimeTenantResolver.forceTenant(tenant);
            p.compute(dataset);
        });
        LOGGER.info("...computing OK");
        // Once computations has been done, associated attributes are created or updated
        LOGGER.info("Creating computed attributes...");
        createComputedAttributes(dataset, computationPlugins);

        esRepos.save(tenant, dataset);
        LOGGER.info("Datatset {} updated", dataset);
    }

    /**
     * Data object accumulator and multi thread Elasticsearch bulk saver
     */
    private class DataObjectUpdater implements Consumer<DataObject> {

        private String dsIpId;

        private Set<String> groups;

        private OffsetDateTime updateDate;

        private Long datasetModelId;

        private HashSet<DataObject> toSaveObjects;

        private SaveDataObjectsCallable saveDataObjectsCallable;

        private Future<Void> saveBulkTask = null;

        private ExecutorService executor = Executors.newFixedThreadPool(1);

        public DataObjectUpdater(String dsIpId, Set<String> groups, OffsetDateTime updateDate, Long datasetModelId,
                HashSet<DataObject> toSaveObjects, SaveDataObjectsCallable saveDataObjectsCallable) {
            this.dsIpId = dsIpId;
            this.groups = groups;
            this.updateDate = updateDate;
            this.datasetModelId = datasetModelId;
            this.toSaveObjects = toSaveObjects;
            this.saveDataObjectsCallable = saveDataObjectsCallable;
        }

        @Override
        public void accept(DataObject object) {
            object.getTags().add(dsIpId);
            object.getGroups().addAll(groups);
            object.setLastUpdate(updateDate);
            object.getDatasetModelIds().add(datasetModelId);
            toSaveObjects.add(object);
            if (toSaveObjects.size() == IEsRepository.BULK_SIZE) {
                this.waitForEndOfTask();
                LOGGER.info("Launching Saving of {} data objects task...", toSaveObjects.size());
                // Give a clone of data objects to save set
                saveDataObjectsCallable.setSet((Set<DataObject>) toSaveObjects.clone());
                // Clear data objects to save set
                toSaveObjects.clear();
                // Add task to thread pool executor
                saveBulkTask = executor.submit(saveDataObjectsCallable);
            }
        }

        public void waitForEndOfTask() {
            if (saveBulkTask != null) {
                LOGGER.info("Waiting for previous task to end...");
                try {
                    saveBulkTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Unable to save data objects", e);
                }
            }
        }
    }

    /**
     * Callable used to parallelize data objects bulk save into Elasticsearch
     */
    private class SaveDataObjectsCallable implements Callable<Void> {

        private String tenant;

        private Set<DataObject> set;

        public SaveDataObjectsCallable(String tenant) {
            this.tenant = tenant;
        }

        public void setSet(Set<DataObject> set) {
            this.set = set;
        }

        @Override
        public Void call() throws Exception {
            if ((set != null) && !set.isEmpty()) {
                LOGGER.info("Saving {} data objects...", set.size());
                runtimeTenantResolver.forceTenant(tenant);
                esRepos.saveBulk(tenant, set);
                LOGGER.info("...data objects saved");
            }
            return null;
        }
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
                    dataset.getProperties().add(attrInFragment);
                }
            } else {
                // the attribute is not inside a fragment so lets remove the old one if it exist and add the new one to
                // the root
                dataset.getProperties().remove(attributeToAdd);
                dataset.getProperties().add(attributeToAdd);
            }
        }
    }

    /**
     * Create ES index with tenant name and geometry mapping
     */
    private void createIndex(String tenant) {
        esRepos.createIndex(tenant);
        String[] types = Arrays.stream(EntityType.values()).map(EntityType::toString)
                .toArray(length -> new String[length]);
        esRepos.setAutomaticDoubleMapping(tenant, types);
        esRepos.setGeometryMapping(tenant, types);
    }

    @Override
    public IngestionResult ingest(PluginConfiguration pluginConf, OffsetDateTime date) throws ModuleException {
        String tenant = runtimeTenantResolver.getTenant();

        String datasourceId = pluginConf.getId().toString();
        if (!pluginConf.isActive()) {
            throw new InactiveDatasourceException();
        }
        IDataSourcePlugin dsPlugin = pluginService.getPlugin(pluginConf);

        int savedObjectsCount = 0;
        OffsetDateTime now = OffsetDateTime.now();
        // If index doesn't exist, just create all data objects
        if (!esRepos.indexExists(tenant)) {
            createIndex(tenant);

            Page<DataObject> page = findAllFromDatasource(date, tenant, dsPlugin, datasourceId,
                                                          new PageRequest(0, IEsRepository.BULK_SIZE));
            savedObjectsCount += createDataObjects(tenant, datasourceId, now, page.getContent());

            while (page.hasNext()) {
                page = findAllFromDatasource(date, tenant, dsPlugin, datasourceId, page.nextPageable());
                savedObjectsCount += createDataObjects(tenant, datasourceId, now, page.getContent());
            }
        } else { // index exists, data objects may also exist
            Page<DataObject> page = findAllFromDatasource(date, tenant, dsPlugin, datasourceId,
                                                          new PageRequest(0, IEsRepository.BULK_SIZE));
            savedObjectsCount += mergeDataObjects(tenant, datasourceId, now, page.getContent());

            while (page.hasNext()) {
                page = findAllFromDatasource(date, tenant, dsPlugin, datasourceId, page.nextPageable());
                savedObjectsCount += mergeDataObjects(tenant, datasourceId, now, page.getContent());
            }
            // In case Dataset associated with datasourceId already exists, we must search for it and do as it has
            // been updated (to update all associated data objects which have a lastUpdate date >= now)
            SimpleSearchKey<Dataset> searchKey = new SimpleSearchKey<>(tenant, EntityType.DATASET.toString(),
                                                                       Dataset.class);
            Set<Dataset> datasetsToUpdate = new HashSet<>();
            esRepos.searchAll(searchKey, datasetsToUpdate::add, ICriterion.eq("plgConfDataSource.id", datasourceId));
            if (!datasetsToUpdate.isEmpty()) {
                // transactional method => use self, not this
                self.updateDatasets(tenant, datasetsToUpdate, now);
            }
        }

        return new IngestionResult(now, savedObjectsCount);
    }

    private Page<DataObject> findAllFromDatasource(OffsetDateTime date, String tenant, IDataSourcePlugin dsPlugin,
            String datasourceId, Pageable pageable) {
        Page<DataObject> page = dsPlugin.findAll(tenant, pageable, date);
        page.forEach(dataObject -> dataObject.setIpId(buildIpId(tenant, dataObject.getSipId(), datasourceId)));
        return page;
    }

    /**
     * Build an URN for a {@link EntityType} of type DATA. The URN contains an UUID builds for a specific value, it used
     * {@link UUID#nameUUIDFromBytes(byte[]).
     *
     * @param tenant the tenant name
     * @param sipId the original primary key value
     * @return the IpId generated from given parameters
     */
    private static final UniformResourceName buildIpId(String tenant, String sipId, String datasourceId) {
        return new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, tenant,
                                       UUID.nameUUIDFromBytes((datasourceId + "$$" + sipId).getBytes()), 1);
    }

    @Override
    @MultitenantTransactional
    public void updateDatasets(String tenant, Set<Dataset> datasets, OffsetDateTime lastUpdateDate) {
        if (datasets.size() == 1) {
            this.updateEntityIntoEs(tenant, datasets.iterator().next().getIpId(), lastUpdateDate);
        } else {
            this.updateEntitiesIntoEs(tenant, datasets.stream().map(Dataset::getIpId)
                    .toArray(size -> new UniformResourceName[size]), lastUpdateDate);
        }
    }

    private int createDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects) {
        // On all objects, it is necessary to set datasourceId and creation date
        OffsetDateTime creationDate = now;
        objects.forEach(dataObject -> {
            dataObject.setDataSourceId(datasourceId);
            dataObject.setCreationDate(creationDate);
            if (Strings.isNullOrEmpty(dataObject.getLabel())) {
                dataObject.setLabel(dataObject.getIpId().toString());
            }
        });
        return esRepos.saveBulk(tenant, objects);
    }

    private int mergeDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects) {
        // Set of data objects to be saved (depends on existence of data objects into ES)
        Set<DataObject> toSaveObjects = new HashSet<>();

        for (DataObject dataObject : objects) {
            DataObject curObject = esRepos.get(tenant, dataObject);
            // if current object does already exist into ES, the new one wins. It is then mandatory to retrieve from
            // current creationDate, groups and tags.
            if (curObject != null) {
                dataObject.setCreationDate(curObject.getCreationDate());
                dataObject.setGroups(curObject.getGroups());
                dataObject.setTags(curObject.getTags());
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
            toSaveObjects.add(dataObject);
        }
        // Bulk save : toSaveObjects.size() isn't checked because it is more likely that toSaveObjects
        // has same size as page.getContent() or is empty
        return esRepos.saveBulk(tenant, toSaveObjects);
    }

    @Override
    public boolean working() { // NOSONAR : test purpose
        return delay.get() < MAX_DELAY_MS;
    }

    @Override
    public boolean workingHard() { // NOSONAR : test purpose
        return delay.get() == INITIAL_DELAY_MS;
    }

    @Override
    public boolean strolling() { // NOSONAR : test purpose
        return delay.get() == MAX_DELAY_MS;
    }

    @Override
    public void startWork() { // NOSONAR : test purpose
        // If crawler is busy, wait for it
        while (working()) {
            ;
        }
        scheduledWork = true;
        somethingDone = false;
        LOGGER.info("start working...");
    }

    @Override
    public void waitForEndOfWork() throws InterruptedException { // NOSONAR : test purpose
        if (!scheduledWork) {
            throw new IllegalStateException("Before waiting, startWork() must be called");
        }
        LOGGER.info("Waiting for work end");
        // In case work hasn't started yet.
        Thread.sleep(3_000);
        // As soon as something has been done, we wait for crawler service to no more be busy
        while (inProgress || (!somethingDone && !strolling())) {
            Thread.sleep(1_000);
        }
        LOGGER.info("...Work ended");
        scheduledWork = false;
    }

    @Override
    public void setConsumeOnlyMode(boolean b) {
        consumeOnlyMode = b;
    }
}
