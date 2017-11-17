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
package fr.cnes.regards.modules.crawler.service;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.InactiveDatasourceException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.datasources.plugins.exception.DataSourceException;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.event.NotDatasetEntityEvent;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

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
    @Autowired
    @Lazy
    private ICrawlerAndIngesterService self;

    @Override
    @Async
    public void crawl() {
        super.crawl(self::doPoll);
    }

    @Override
    public IngestionResult ingest(PluginConfiguration pluginConf, OffsetDateTime lastUpdateDate)
            throws ModuleException, InterruptedException, ExecutionException, DataSourceException {
        String tenant = runtimeTenantResolver.getTenant();

        if (!pluginConf.isActive()) {
            throw new InactiveDatasourceException();
        }
        IDataSourcePlugin dsPlugin = pluginService.getPlugin(pluginConf.getId());

        int savedObjectsCount = 0;
        OffsetDateTime now = OffsetDateTime.now();
        String datasourceId = pluginConf.getId().toString();
        // If index doesn't exist, just create all data objects
        if (entityIndexerService.createIndexIfNeeded(tenant)) {
            savedObjectsCount = readDatasourceAndCreateDataObjects(lastUpdateDate, tenant, dsPlugin, savedObjectsCount,
                                                                   now, datasourceId);
        } else { // index exists, data objects may also exist
            savedObjectsCount = readDatasourceAndMergeDataObjects(lastUpdateDate, tenant, dsPlugin, savedObjectsCount,
                                                                  now, datasourceId);
        }
        // In case Dataset associated with datasourceId already exists (or had been created between datasrouyce creation and its ingestion
        // , we must search for it and do as it has
        // been updated (to update all associated data objects which have a lastUpdate date >= now)
        SimpleSearchKey<Dataset> searchKey = new SimpleSearchKey<>(tenant, EntityType.DATASET.toString(),
                                                                   Dataset.class);
        Set<Dataset> datasetsToUpdate = new HashSet<>();
        esRepos.searchAll(searchKey, datasetsToUpdate::add, ICriterion.eq("plgConfDataSource.id", datasourceId));
        if (!datasetsToUpdate.isEmpty()) {
            // transactional method => use self, not this
            entityIndexerService.updateDatasets(tenant, datasetsToUpdate, lastUpdateDate, true);
        }

        return new IngestionResult(now, savedObjectsCount);
    }

    private int readDatasourceAndMergeDataObjects(OffsetDateTime lastUpdateDate, String tenant,
            IDataSourcePlugin dsPlugin, int savedObjectsCount, OffsetDateTime now, String datasourceId)
            throws DataSourceException, InterruptedException, ExecutionException {
        // Use a thread pool of size 1 to merge data while datasource pull other data
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Page<DataObject> page = findAllFromDatasource(lastUpdateDate, tenant, dsPlugin, datasourceId,
                                                      new PageRequest(0, IEsRepository.BULK_SIZE));
        final List<DataObject> list = page.getContent();
        Future<Integer> task = executor.submit(() -> {
            runtimeTenantResolver.forceTenant(tenant);
            return entityIndexerService.mergeDataObjects(tenant, datasourceId, now, list);
        });

        while (page.hasNext()) {
            page = findAllFromDatasource(lastUpdateDate, tenant, dsPlugin, datasourceId, page.nextPageable());
            savedObjectsCount += task.get();
            final List<DataObject> otherList = page.getContent();
            task = executor.submit(() -> {
                runtimeTenantResolver.forceTenant(tenant);
                return entityIndexerService.mergeDataObjects(tenant, datasourceId, now, otherList);
            });
        }
        savedObjectsCount += task.get();
        return savedObjectsCount;
    }

    private int readDatasourceAndCreateDataObjects(OffsetDateTime lastUpdateDate, String tenant,
            IDataSourcePlugin dsPlugin, int savedObjectsCount, OffsetDateTime now, String datasourceId)
            throws DataSourceException, InterruptedException, ExecutionException {
        // Use a thread pool of size 1 to merge data while datasource pull other data
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Page<DataObject> page = findAllFromDatasource(lastUpdateDate, tenant, dsPlugin, datasourceId,
                                                      new PageRequest(0, IEsRepository.BULK_SIZE));
        final List<DataObject> list = page.getContent();
        Future<Integer> task = executor.submit(() -> {
            runtimeTenantResolver.forceTenant(tenant);
            return entityIndexerService.createDataObjects(tenant, datasourceId, now, list);
        });

        while (page.hasNext()) {
            page = findAllFromDatasource(lastUpdateDate, tenant, dsPlugin, datasourceId, page.nextPageable());
            savedObjectsCount += task.get();
            final List<DataObject> otherList = page.getContent();
            task = executor.submit(() -> {
                runtimeTenantResolver.forceTenant(tenant);
                return entityIndexerService.createDataObjects(tenant, datasourceId, now, otherList);
            });
        }
        savedObjectsCount += task.get();
        return savedObjectsCount;
    }

    /**
     * Read datasource since given date page setting ipId to each objects
     * @param date date from which to read datasource data
     * @param tenant
     * @param dsPlugin
     * @param datasourceId
     * @param pageable
     * @return
     * @throws DataSourceException
     */
    private Page<DataObject> findAllFromDatasource(OffsetDateTime date, String tenant, IDataSourcePlugin dsPlugin,
            String datasourceId, Pageable pageable) throws DataSourceException {
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
