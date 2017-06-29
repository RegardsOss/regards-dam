package fr.cnes.regards.modules.crawler.service;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.service.IEntitiesService;
import fr.cnes.regards.modules.entities.service.visitor.AttributeBuilderVisitor;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;

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

    @PersistenceContext
    private EntityManager em;

    /**
     * Load given entity from database and update Elasticsearch
     *
     * @param tenant concerned tenant (also index intoES)
     * @param ipId concerned entity IpId
     * @param lastUpdateDate for dataset entity, if this date is provided, only more recent data objects must be taken
     * into account
     * @param forceDataObjectsUpdate in case of dataset update, if set to true, force update of all associated data
     * objects else do it only when necessary (subCriteria, datasource, ... change)
     */
    @Override
    public void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate, boolean forceDataObjectsUpdate) {
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
                createIndexIfNeeded(tenant);
            }
            // Remove parameters of dataset datasource to avoid expose security values
            if (entity instanceof Dataset) {
                // entity must be detached else Hibernate tries to commit update (datasource is cascade.DETACHed)
                em.detach(entity);
                ((Dataset) entity).getDataSource().setParameters(null);
            }
            // Then save entity
            LOGGER.debug("Saving entity {}", entity);
            boolean needAssociatedDataObjectsUpdate = forceDataObjectsUpdate;
            if (entity instanceof Dataset) {
                Dataset dataset = (Dataset) entity;
                needAssociatedDataObjectsUpdate |= needAssociatedDataObjectsUpdate(dataset,
                                                                                   esRepos.get(tenant, dataset));
            }
            boolean created = esRepos.save(tenant, entity);
            LOGGER.debug("Elasticsearch saving result : {}", created);
            if ((entity instanceof Dataset) && needAssociatedDataObjectsUpdate) {
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
        return !newDataset.getSubsettingClause().equals(curDataset.getSubsettingClause())
                      || !newDataset.getGroups().equals(curDataset.getGroups());
    }

    /**
     * Load given entities from database and update Elasticsearch
     *
     * @param tenant concerned tenant (also index intoES)
     * @param ipIds concerned entity IpIds
     * @param forceDataObjectsUpdate in case of dataset update, if set to true, force update of all associated data
     * objects else do it only when necessary (subCriteria, datasource, ... change)
     */
    @Override
    public void updateEntitiesIntoEs(String tenant, UniformResourceName[] ipIds, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate, boolean forceDataObjectsUpdate) {
        LOGGER.info("received msg for " + Arrays.toString(ipIds));
        Set<UniformResourceName> toDeleteIpIds = Sets.newHashSet(ipIds);
        List<AbstractEntity> entities = entitiesService.loadAllWithRelations(ipIds);
        entities.forEach(e -> toDeleteIpIds.remove(e.getIpId()));
        // Entities to save
        if (!entities.isEmpty()) {
            if (!esRepos.indexExists(tenant)) {
                createIndexIfNeeded(tenant);
            }
            // Remove pluginConf parameters from datasets to avoid jsonify into Elasticsearch
            entities.stream().filter(e -> e instanceof Dataset).forEach(dataset -> {
                // Don't forget to detach dataset in order to cascade.Detach datasource
                em.detach(dataset);
                ((Dataset) dataset).getDataSource().setParameters(null);
            });
            //esRepos.saveBulk(tenant, entities);
            entities.stream().forEach(e -> {
                boolean needAssociatedDataObjectsUpdate = forceDataObjectsUpdate;
                if (e instanceof Dataset) {
                    Dataset dataset = (Dataset) e;
                    needAssociatedDataObjectsUpdate |= needAssociatedDataObjectsUpdate(dataset,
                                                                                       esRepos.get(tenant, dataset));
                }
                esRepos.save(tenant, e);
                if ((e instanceof Dataset) && needAssociatedDataObjectsUpdate) {
                    manageDatasetUpdate((Dataset) e, lastUpdateDate, updateDate);
                }
            });
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
        // Create a callable which bulk save into ES a set of data objects
        SaveDataObjectsCallable saveDataObjectsCallable = new SaveDataObjectsCallable(tenant, dataset.getId());
        // Create an updater to be executed on each data object of dataset subsetting criteria results
        DataObjectUpdater dataObjectUpdater = new DataObjectUpdater(dsIpId, groups, updateDate, datasetModelId,
                                                                    toSaveObjects, saveDataObjectsCallable,
                                                                    dataset.getId());
        esRepos.searchAll(searchKey, dataObjectUpdater, subsettingCrit);
        if (!toSaveObjects.isEmpty()) {
            dataObjectUpdater.waitForEndOfTask();
            LOGGER.info("Saving {} data objects (dataset {})...", toSaveObjects.size(), dataset.getId());
            esRepos.saveBulk(tenant, toSaveObjects);
            LOGGER.info("...data objects saved");
        }

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
        LOGGER.info("Datatset {} updated", dataset.getId());
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

        private long datasetId;

        private Future<Void> saveBulkTask = null;

        private ExecutorService executor = Executors.newFixedThreadPool(1);

        public DataObjectUpdater(String dsIpId, Set<String> groups, OffsetDateTime updateDate, Long datasetModelId,
                HashSet<DataObject> toSaveObjects, SaveDataObjectsCallable saveDataObjectsCallable, long datasetId) {
            this.dsIpId = dsIpId;
            this.groups = groups;
            this.updateDate = updateDate;
            this.datasetModelId = datasetModelId;
            this.toSaveObjects = toSaveObjects;
            this.saveDataObjectsCallable = saveDataObjectsCallable;
            this.datasetId = datasetId;
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
                LOGGER.info("Launching Saving of {} data objects task (dataset {})...", toSaveObjects.size(),
                            datasetId);
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
                LOGGER.info("Waiting for previous task to end (dataset {})...", datasetId);
                try {
                    saveBulkTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error(String.format("Unable to save data objects (dataset %d)", datasetId), e);
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

        private long datasetId;

        public SaveDataObjectsCallable(String tenant, long datasetId) {
            this.tenant = tenant;
            this.datasetId = datasetId;
        }

        public void setSet(Set<DataObject> set) {
            this.set = set;
        }

        @Override
        public Void call() throws Exception {
            if ((set != null) && !set.isEmpty()) {
                LOGGER.info("Saving {} data objects (dataset {})...", set.size(), datasetId);
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
        if (datasets.size() == 1) {
            updateEntityIntoEs(tenant, datasets.iterator().next().getIpId(), lastUpdateDate, null,
                               forceDataObjectsUpdate);
        } else {
            updateEntitiesIntoEs(tenant,
                                 datasets.stream().map(Dataset::getIpId).toArray(size -> new UniformResourceName[size]),
                                 lastUpdateDate, null, forceDataObjectsUpdate);
        }
    }

    @Override
    public int createDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects) {
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

    @Override
    public int mergeDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects) {
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

}
