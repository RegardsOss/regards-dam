package fr.cnes.regards.modules.crawler.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.amqp.IPoller;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.crawler.dao.IEsRepository;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.event.EntityEvent;
import fr.cnes.regards.modules.entities.service.IEntityService;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Crawler service.
 * <b>This service need @EnableAsync at Configuration and is used in conjunction with CrawlerInitializer</b>
 */
@Service
public class CrawlerService implements ICrawlerService {

    private static final String DATA_SOURCE_ID = "dataSourceId";

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerService.class);

    /**
     * To avoid CPU overload, a delay is set between each loop of tenants event inspection.
     * This delay is doubled each time no event has been pulled (limited to MAX_DELAY_MS).
     * When an event is pulled (during a tenants event inspection), no wait is done and
     * delay is reset to INITIAL_DELAY_MS
     */
    private static final int INITIAL_DELAY_MS = 1;

    /**
     * To avoid CPU overload, a delay is set between each loop of tenants event inspection.
     * This delay is doubled each time no event has been pulled (limited to MAX_DELAY_MS).
     * When an event is pulled (during a tenants event inspection), no wait is done and
     * delay is reset to INITIAL_DELAY_MS
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
    @Qualifier(value = "entityService")
    private IEntityService entityService;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IEsRepository esRepos;

    /**
     * To retrieve ICrawlerService (self) proxy
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Self proxy
     */
    private ICrawlerService self;

    /**
     * Indicate that daemon stop has been asked
     */
    private boolean stopAsked = false;

    /**
     * Once ICrawlerService bean has been initialized, retrieve self proxy to permit transactional call of doPoll.
     */
    @PostConstruct
    private void init() {
        this.self = applicationContext.getBean(ICrawlerService.class);
    }

    /**
     * Ask for termination of daemon process
     */
    @PreDestroy
    private void endCrawl() {
        this.stopAsked = true;
    }

    /**
     * Daemon process.
     * Poll entity events on all tenants and update Elasticsearch to reflect Postgres database
     */
    @Async
    @Override
    public void crawl() {
        int delay = INITIAL_DELAY_MS;
        // Infinite loop
        while (true) {
            // Manage termination
            if (this.stopAsked) {
                break;
            }
            boolean atLeastOnPoll = false;
            // For all tenants
            for (String tenant : tenantResolver.getAllTenants()) {
                try {
                    runtimeTenantResolver.forceTenant(tenant);
                    // Try to poll an entity event on this tenant
                    atLeastOnPoll |= self.doPoll();
                } catch (RuntimeException t) {
                    LOGGER.error("Cannot manage entity event message", t);
                }
            }
            // If a poll has been done, don't wait and reset delay to initial value
            if (atLeastOnPoll) {
                delay = INITIAL_DELAY_MS;
            } else { // else, wait and double delay for next time (limited to MAX_DELAY)
                try {
                    Thread.sleep(delay);
                    delay = Math.min(delay * 2, MAX_DELAY_MS);
                } catch (InterruptedException e) {
                    LOGGER.error("Thread sleep interrupted.");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Try to do a transactional poll.
     * If a poll is done but an exception occurs, the transaction is rolbacked and the event is still present
     * into AMQP
     * @return true if a poll has been done, false otherwise
     */
    @Override
    @Transactional
    public boolean doPoll() {
        boolean atLeastOnePoll = false;
        // Try to poll an EntityEvent
        TenantWrapper<EntityEvent> wrapper = poller.poll(EntityEvent.class);
        if (wrapper != null) {
            String tenant = wrapper.getTenant();
            UniformResourceName[] ipIds = wrapper.getContent().getIpIds();
            if ((ipIds != null) && (ipIds.length != 0)) {
                atLeastOnePoll = true;
                // Only one entity
                if (ipIds.length == 1) {
                    updateEntityIntoEs(tenant, ipIds[0]);
                } else if (ipIds.length > 1) { // serveral entities at once
                    updateEntitiesIntoEs(tenant, ipIds);
                }
            }
        }
        return atLeastOnePoll;
    }

    /**
     * Load given entity from database and update Elasticsearch
     * @param tenant concerned tenant (also index intoES)
     * @param ipId concerned entity IpId
     */
    private void updateEntityIntoEs(String tenant, UniformResourceName ipId) {
        LOGGER.debug("received msg for " + ipId.toString());
        AbstractEntity entity = entityService.loadWithRelations(ipId);
        // If entity does no more exist in database, it must be deleted from ES
        if (entity == null) {
            if (entity instanceof Dataset) {
                this.manageDatasetDelete(tenant, ipId.toString());
            }
            esRepos.delete(tenant, ipId.getEntityType().toString(), ipId.toString());
        } else { // entity has been created or updated, it must be saved into ES
            // First, check if index exists
            if (!esRepos.indexExists(tenant)) {
                esRepos.createIndex(tenant);
            }
            // Then save entity
            esRepos.save(tenant, entity);
            if (entity instanceof Dataset) {
                this.manageDatasetUpdate((Dataset) entity);
            }
        }
        LOGGER.debug(ipId.toString() + " managed into Elasticsearch");
    }

    /**
     * Load given entities from database and update Elasticsearch
     * @param tenant concerned tenant (also index intoES)
     * @param ipIds concerned entity IpIds
     */
    private void updateEntitiesIntoEs(String tenant, UniformResourceName[] ipIds) {
        LOGGER.debug("received msg for " + Arrays.toString(ipIds));
        Set<UniformResourceName> toDeleteIpIds = Sets.newHashSet(ipIds);
        List<AbstractEntity> entities = entityService.loadAllWithRelations(ipIds);
        entities.forEach(e -> toDeleteIpIds.remove(e.getIpId()));
        // Entities to save
        if (!entities.isEmpty()) {
            if (!esRepos.indexExists(tenant)) {
                esRepos.createIndex(tenant);
            }
            esRepos.saveBulk(tenant, entities);
            entities.stream().filter(e -> e instanceof Dataset).forEach(e -> this.manageDatasetUpdate((Dataset) e));
        }
        // Entities to remove
        if (!toDeleteIpIds.isEmpty()) {
            toDeleteIpIds.forEach(ipId -> esRepos.delete(tenant, ipId.getEntityType().toString(), ipId.toString()));
        }
        LOGGER.debug(Arrays.toString(ipIds) + " managed into Elasticsearch");
    }

    /**
     * Search and update associated dataset data objects (ie remove dataset IpId from tags)
     * @param dataset concerned dataset
     */
    private void manageDatasetDelete(String tenant, String ipId) {
        // Search all DataObjects tagging this Dataset (only DataObjects because all other entities are already managed
        // with th systeme Postgres/RabbitMQ
        ICriterion taggingObjectsCrit = ICriterion.equals("tags", ipId.toString());
        Set<DataObject> toSaveObjects = new HashSet<>();
        Consumer<DataObject> updateTag = object -> {
            object.getTags().remove(ipId);
            toSaveObjects.add(object);
            if (toSaveObjects.size() == IEsRepository.BULK_SIZE) {
                esRepos.saveBulk(tenant, toSaveObjects);
                toSaveObjects.clear();
            }
        };
        // Apply updateTag function to all tagging objects
        esRepos.searchAll(tenant, DataObject.class, updateTag, taggingObjectsCrit);
        // Bulk save remaining objects to save
        if (toSaveObjects.size() == IEsRepository.BULK_SIZE) {
            esRepos.saveBulk(tenant, toSaveObjects);
        }
    }

    /**
     * Search and update associated dataset data objects (ie add dataset IpId into tags)
     * @param dataset concerned dataset
     */
    private void manageDatasetUpdate(Dataset dataset) {
        PluginConfiguration datasource = dataset.getDataSource();
        String datasourceId = datasource.getId() + ":" + datasource.getPluginId();
        ICriterion subsettingCrit = dataset.getSubsettingClause();
        if ((subsettingCrit == null) || (subsettingCrit == ICriterion.all())) {
            subsettingCrit = ICriterion.equals(DATA_SOURCE_ID, datasourceId);
        } else {
            subsettingCrit = ICriterion.and(subsettingCrit, ICriterion.equals(DATA_SOURCE_ID, datasourceId));
        }
        String tenant = runtimeTenantResolver.getTenant();
        String dsIpId = dataset.getIpId().toString();
        Page<DataObject> page = esRepos.search(tenant, DataObject.class, IEsRepository.BULK_SIZE, subsettingCrit);
        this.addTagToDataObjects(tenant, dsIpId, page.getContent());

        while (page.hasNext()) {
            page = esRepos.search(tenant, DataObject.class, page.nextPageable(), subsettingCrit);
            this.addTagToDataObjects(tenant, dsIpId, page.getContent());
        }
    }

    private void addTagToDataObjects(String tenant, String dsIpId, List<DataObject> objects) {
        Set<DataObject> toSaveObjects = new HashSet<>();
        objects.forEach(o -> {
            o.getTags().add(dsIpId);
            toSaveObjects.add(o);
        });
        esRepos.saveBulk(tenant, toSaveObjects);
    }

    @Override
    public void ingest(PluginConfiguration pluginConf) throws ModuleException {
        String tenant = runtimeTenantResolver.getTenant();

        String datasourceId = pluginConf.getId().toString();
        IDataSourcePlugin dsPlugin = pluginService.getPlugin(pluginConf);

        // If index doesn't exist, just create all data objects
        if (!esRepos.indexExists(tenant)) {
            esRepos.createIndex(tenant);
            Page<DataObject> page = dsPlugin.findAll(tenant, new PageRequest(0, IEsRepository.BULK_SIZE));
            this.createDataObjects(tenant, datasourceId, page.getContent());

            while (page.hasNext()) {
                page = dsPlugin.findAll(tenant, page.nextPageable());
                this.createDataObjects(tenant, datasourceId, page.getContent());
            }
        } else { // index exists, data objects may also exist
            Page<DataObject> page = dsPlugin.findAll(tenant, new PageRequest(0, IEsRepository.BULK_SIZE));
            this.mergeDataObjects(tenant, datasourceId, page.getContent());

            while (page.hasNext()) {
                page = dsPlugin.findAll(tenant, page.nextPageable());
                this.mergeDataObjects(tenant, datasourceId, page.getContent());
            }
        }
    }

    private void createDataObjects(String tenant, String datasourceId, List<DataObject> objects) {
        // On all objects, it is necessary to set datasourceId
        objects.forEach(dataObject -> dataObject.setDataSourceId(datasourceId));
        esRepos.saveBulk(tenant, objects);
    }

    private void mergeDataObjects(String tenant, String datasourceId, List<DataObject> objects) {
        // Set of data objects to be saved (depends on existence of data objects into ES)
        Set<DataObject> toSaveObjects = new HashSet<>();

        for (DataObject dataObject : objects) {
            DataObject curObject = esRepos.get(tenant, dataObject);
            // if current object does already exist into ES, nothing has to be done
            // else it must be created
            if (curObject == null) {
                // Don't forget to set datasourceId
                dataObject.setDataSourceId(datasourceId);
                toSaveObjects.add(dataObject);
            }
        }
        // Bulk save : toSaveObjects.size() isn't checked because it is more likely that toSaveObjects
        // has same size as page.getContent() or is empty
        esRepos.saveBulk(tenant, toSaveObjects);
    }
}
