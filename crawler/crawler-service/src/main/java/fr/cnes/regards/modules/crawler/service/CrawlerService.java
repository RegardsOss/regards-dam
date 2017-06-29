/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.service;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.InactiveDatasourceException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.event.NotDatasetEntityEvent;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.EntityType;

/**
 * Crawler service for other entity than Dataset. <b>This service need @EnableSchedule at Configuration</b>
 * This service is the primary to autowire (by IngesterService) in order to ingest datasources
 * @author oroussel
 */
@Service
// Transactionnal is handle by hand on the right method, do not specify Multitenant or InstanceTransactionnal
public class CrawlerService extends AbstractCrawlerService<NotDatasetEntityEvent>
        implements ICrawlerAndIngesterService {

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IEsRepository esRepos;

    /**
     * Self proxy
     */
    private ICrawlerAndIngesterService self;

    /**
     * Once ICrawlerService bean has been initialized, retrieve self proxy to permit transactional call of doPoll.
     */
    @PostConstruct
    private void init() {
        self = applicationContext.getBean(ICrawlerAndIngesterService.class);
    }

    @Override
    @Async
    public void crawl() {
        super.crawl(self::doPoll);
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
        if (!entityIndexerService.createIndexIfNeeded(tenant)) {
            Page<DataObject> page = findAllFromDatasource(date, tenant, dsPlugin, datasourceId,
                                                          new PageRequest(0, IEsRepository.BULK_SIZE));
            savedObjectsCount += entityIndexerService.createDataObjects(tenant, datasourceId, now, page.getContent());

            while (page.hasNext()) {
                page = findAllFromDatasource(date, tenant, dsPlugin, datasourceId, page.nextPageable());
                savedObjectsCount += entityIndexerService.createDataObjects(tenant, datasourceId, now, page.getContent());
            }
        } else { // index exists, data objects may also exist
            Page<DataObject> page = findAllFromDatasource(date, tenant, dsPlugin, datasourceId,
                                                          new PageRequest(0, IEsRepository.BULK_SIZE));
            savedObjectsCount += entityIndexerService.mergeDataObjects(tenant, datasourceId, now, page.getContent());

            while (page.hasNext()) {
                page = findAllFromDatasource(date, tenant, dsPlugin, datasourceId, page.nextPageable());
                savedObjectsCount += entityIndexerService.mergeDataObjects(tenant, datasourceId, now, page.getContent());
            }
            // In case Dataset associated with datasourceId already exists, we must search for it and do as it has
            // been updated (to update all associated data objects which have a lastUpdate date >= now)
            SimpleSearchKey<Dataset> searchKey = new SimpleSearchKey<>(tenant, EntityType.DATASET.toString(),
                                                                       Dataset.class);
            Set<Dataset> datasetsToUpdate = new HashSet<>();
            esRepos.searchAll(searchKey, datasetsToUpdate::add, ICriterion.eq("plgConfDataSource.id", datasourceId));
            if (!datasetsToUpdate.isEmpty()) {
                // transactional method => use self, not this
                entityIndexerService.updateDatasets(tenant, datasetsToUpdate, now, true);
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
}
