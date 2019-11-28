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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.module.rest.exception.InactiveDatasourceException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.notification.NotificationLevel;
import fr.cnes.regards.framework.notification.client.INotificationClient;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.crawler.dao.IDatasourceIngestionRepository;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.crawler.service.event.DataSourceMessageEvent;
import fr.cnes.regards.modules.crawler.service.exception.NotFinishedException;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.DataSourceException;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IAipDataSourcePlugin;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDataSourcePlugin;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.event.NotDatasetEntityEvent;
import fr.cnes.regards.modules.dam.domain.entities.feature.DataObjectFeature;
import fr.cnes.regards.modules.indexer.dao.BulkSaveLightResult;
import fr.cnes.regards.modules.indexer.dao.BulkSaveResult;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.dao.spatial.GeoHelper;
import fr.cnes.regards.modules.indexer.dao.spatial.ProjectGeoSettings;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.service.IModelService;

/**
 * Crawler service for other entity than Dataset. <b>This service need @EnableSchedule at Configuration</b>
 * This service is the primary to autowire (by IngesterService) in order to ingest datasources
 * @author oroussel
 */
@Service
// Transactionnal is handle by hand on the right method, do not specify Multitenant or InstanceTransactionnal
public class CrawlerService extends AbstractCrawlerService<NotDatasetEntityEvent>
        implements ICrawlerAndIngesterService {

    private static final DateTimeFormatter ISO_TIME_UTC = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_TIME).toFormatter();

    @Autowired
    private IModelService modelService;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IEsRepository esRepos;

    @Autowired
    private ProjectGeoSettings projectGeoSettings;

    @Autowired
    private INotificationClient notificationClient;

    /**
     * Self proxy
     */
    @Autowired
    @Lazy
    private ICrawlerAndIngesterService self;

    @Autowired
    private IDatasourceIngestionRepository datasourceIngestionRepo;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Build an URN for a {@link EntityType} of type DATA. The URN contains an UUID builds for a specific value, it used
     * {@link UUID#nameUUIDFromBytes(byte[])}.
     * @param tenant the tenant name
     * @param providerId the original primary key value
     * @return the IpId generated from given parameters
     */
    private static UniformResourceName buildIpId(String tenant, String providerId, String datasourceId) {
        return new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, tenant,
                UUID.nameUUIDFromBytes((datasourceId + "$$" + providerId).getBytes()), 1);
    }

    @Override
    @Async
    public void crawl() {
        super.crawl(self::doPoll);
    }

    @Override
    public IngestionResult ingest(PluginConfiguration pluginConf, DatasourceIngestion dsi)
            throws ModuleException, InterruptedException, ExecutionException, DataSourceException, NotFinishedException,
            InactiveDatasourceException {
        String tenant = runtimeTenantResolver.getTenant();
        OffsetDateTime lastUpdateDate = dsi.getLastIngestDate();
        // In case last ingestion has finished with a NOT_FINISHED status, failed page number is given
        int pageNumber = dsi.getErrorPageNumber() == null ? 0 : dsi.getErrorPageNumber();
        String dsiId = dsi.getId();

        IDataSourcePlugin dsPlugin;
        try {
            dsPlugin = pluginService.getPlugin(pluginConf.getBusinessId());
        } catch (NotAvailablePluginConfigurationException e) {
            throw new InactiveDatasourceException();
        }

        BulkSaveLightResult saveResult;
        OffsetDateTime now = OffsetDateTime.now();
        String datasourceId = pluginConf.getId().toString();
        // If index doesn't exist, just create all data objects
        boolean mergeNeeded = entityIndexerService.createIndexIfNeeded(tenant);
        // If index already exist, check if index already contains data objects (if not, no need to merge)
        if (mergeNeeded) {
            SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(EntityType.DATA.toString(), DataObject.class);
            mergeNeeded = esRepos.count(searchKey, ICriterion.all()) != 0;
        }
        if (mergeNeeded) {
            // index exists, data objects may also exist
            sendMessage("Start reading datasource and merging/creating objects...", dsiId);
            saveResult = readDatasourceAndMergeDataObjects(lastUpdateDate, tenant, dsPlugin, now, datasourceId, dsiId,
                                                           pageNumber);
        } else {
            sendMessage("Start reading datasource and creating objects...", dsiId);
            saveResult = readDatasourceAndCreateDataObjects(lastUpdateDate, tenant, dsPlugin, now, datasourceId, dsiId,
                                                            pageNumber);
        }
        sendMessage(String.format("...End reading datasource %s.", dsi.getLabel()), dsiId);
        // In case Dataset associated with datasourceId already exists (or had been created between datasource creation
        // and its ingestion), we must search for it and do as it has been updated (to update all associated data
        // objects which have a lastUpdate date >= now)
        SimpleSearchKey<Dataset> searchKey = new SimpleSearchKey<>(EntityType.DATASET.toString(), Dataset.class);
        searchKey.setSearchIndex(tenant);
        searchKey.setCrs(projectGeoSettings.getCrs());
        Set<Dataset> datasetsToUpdate = new HashSet<>();
        esRepos.searchAll(searchKey, datasetsToUpdate::add, ICriterion.eq("plgConfDataSource.id", datasourceId));
        if (!datasetsToUpdate.isEmpty()) {
            sendMessage("Start updating datasets associated to datasource...", dsiId);
            entityIndexerService.updateDatasets(tenant, datasetsToUpdate, lastUpdateDate, true, dsiId);
            sendMessage("...End updating datasets.", dsiId);
        }

        return new IngestionResult(now, saveResult.getSavedDocsCount(), saveResult.getInErrorDocsCount());
    }

    private BulkSaveLightResult readDatasourceAndMergeDataObjects(OffsetDateTime lastUpdateDate, String tenant,
            IDataSourcePlugin dsPlugin, OffsetDateTime now, String datasourceId, String dsiId, int pageNumber)
            throws InterruptedException, DataSourceException, ModuleException, NotFinishedException,
            ExecutionException {
        BulkSaveLightResult saveResult = new BulkSaveLightResult();
        int availableRecordsCount = 0;
        // Use a thread pool of size 1 to merge data while datasource pull other data
        ExecutorService executor = Executors.newFixedThreadPool(1);
        sendMessage(String.format("  Finding at most %d records from datasource...", IEsRepository.BULK_SIZE), dsiId);
        Page<DataObject> page = null;
        Future<BulkSaveResult> task = null;
        try {
            try {
                page = findAllFromDatasource(lastUpdateDate, tenant, dsPlugin, datasourceId,
                                             PageRequest.of(pageNumber, IEsRepository.BULK_SIZE));
                sendMessage(String.format("  ...Found %d records from datasource", page.getNumberOfElements()), dsiId);
                availableRecordsCount += page.getNumberOfElements();
                final List<DataObject> list = page.getContent();
                task = executor.submit(mergeDataObjectCallable(tenant, now, datasourceId, dsiId, list));

                while (page.hasNext()) {
                    sendMessage(String.format("  Finding at most %d records from datasource...",
                                              IEsRepository.BULK_SIZE),
                                dsiId);
                    page = findAllFromDatasource(lastUpdateDate, tenant, dsPlugin, datasourceId, page.nextPageable());
                    sendMessage(String.format("  ...Found %d records from datasource", page.getNumberOfElements()),
                                dsiId);
                    availableRecordsCount += page.getNumberOfElements();
                    saveResult.append(task.get());
                    final List<DataObject> otherList = page.getContent();
                    task = executor.submit(mergeDataObjectCallable(tenant, now, datasourceId, dsiId, otherList));
                }
            } catch (DataSourceException | ModuleException e) { // Find from datasource has failed
                // Failed at first find from datasource => "classical" ERROR
                if (page == null) {
                    throw e;
                }
                throw new NotFinishedException(e, saveResult, page.getNumber());
            } finally { // Don't forget current indexation task...(from previous page) if it does exist of course
                if (task != null) {
                    saveResult.append(task.get());
                }
            }
        } catch (ExecutionException e) { // ES indexation has failed
            // Failed at first indexation => "classical" ERROR
            int errorPageNumber = page.previousPageable().getPageNumber();
            if (errorPageNumber == pageNumber) {
                throw e;
            }
            throw new NotFinishedException(e, saveResult, errorPageNumber);
        } finally {
            // Remove thread used by executor
            executor.shutdown();
        }
        sendMessage(String.format("  ...Finally indexed %d objects for %d availables records.",
                                  saveResult.getSavedDocsCount(), availableRecordsCount),
                    dsiId);
        return saveResult;
    }

    private BulkSaveLightResult readDatasourceAndCreateDataObjects(OffsetDateTime lastUpdateDate, String tenant,
            IDataSourcePlugin dsPlugin, OffsetDateTime now, String datasourceId, String dsiId, int pageNumber)
            throws InterruptedException, DataSourceException, ModuleException, NotFinishedException,
            ExecutionException {
        BulkSaveLightResult saveResult = new BulkSaveLightResult();
        int availableRecordsCount = 0;
        // Use a thread pool of size 1 to merge data while datasource pull other data
        ExecutorService executor = Executors.newFixedThreadPool(1);
        sendMessage(String.format("  Finding at most %d records from datasource...", IEsRepository.BULK_SIZE), dsiId);
        Page<DataObject> page = null;
        Future<BulkSaveResult> task = null;
        try {
            try {
                page = findAllFromDatasource(lastUpdateDate, tenant, dsPlugin, datasourceId,
                                             PageRequest.of(pageNumber, IEsRepository.BULK_SIZE));
                sendMessage(String.format("  ...Found %d records from datasource", page.getNumberOfElements()), dsiId);
                availableRecordsCount += page.getNumberOfElements();
                final List<DataObject> list = page.getContent();
                task = executor.submit(createDataObjectsCallable(tenant, now, datasourceId, dsiId, list));

                while (page.hasNext()) {
                    sendMessage(String.format("  Finding at most %d records from datasource...",
                                              IEsRepository.BULK_SIZE),
                                dsiId);
                    page = findAllFromDatasource(lastUpdateDate, tenant, dsPlugin, datasourceId, page.nextPageable());
                    sendMessage(String.format("  ...Found %d records from datasource", page.getNumberOfElements()),
                                dsiId);
                    availableRecordsCount += page.getNumberOfElements();
                    saveResult.append(task.get());
                    final List<DataObject> otherList = page.getContent();
                    task = executor.submit(createDataObjectsCallable(tenant, now, datasourceId, dsiId, otherList));
                }
            } catch (DataSourceException | ModuleException e) { // Find from datasource has failed
                // Failed at first find from datasource => "classical" ERROR
                if (page == null) {
                    throw e;
                }
                throw new NotFinishedException(e, saveResult, page.getNumber());
            } finally { // Don't forget current indexation task...(from previous page)
                if (task != null) {
                    saveResult.append(task.get());
                }
            }
        } catch (ExecutionException e) { // ES indexation has failed
            // Failed at first indexation => "classical" ERROR
            int errorPageNumber = page.previousPageable().getPageNumber();
            if (errorPageNumber == pageNumber) {
                throw e;
            }
            throw new NotFinishedException(e, saveResult, errorPageNumber);
        } finally {
            // Remove thread used by executor
            executor.shutdown();
        }

        sendMessage(String.format("  ...Finally indexed %d distinct objects for %d availables records.",
                                  saveResult.getSavedDocsCount(), availableRecordsCount),
                    dsiId);
        return saveResult;
    }

    /**
     * Get Callable to be used by parallel tasks to create a bulk of data objects
     */
    private Callable<BulkSaveResult> createDataObjectsCallable(String tenant, OffsetDateTime now, String datasourceId,
            String dsiId, List<DataObject> list) {
        return () -> {
            runtimeTenantResolver.forceTenant(tenant);
            sendMessage(String.format("  Indexing %d objects...", list.size()), dsiId);
            BulkSaveResult bulkSaveResult = entityIndexerService.createDataObjects(tenant, datasourceId, now, list);
            if (bulkSaveResult.getInErrorDocsCount() > 0) {
                sendMessage(String.format("  ...%d objects cannot be saved:\n%s", bulkSaveResult.getInErrorDocsCount(),
                                          bulkSaveResult.getDetailedErrorMsg().replace("\n", "\n    ")),
                            dsiId);
            }
            sendMessage(String.format("  ...%d objects effectively indexed.", bulkSaveResult.getSavedDocsCount()),
                        dsiId);
            return bulkSaveResult;
        };
    }

    /**
     * Get Callable to be used by parallel tasks to merge a bulk of data objects
     */
    private Callable<BulkSaveResult> mergeDataObjectCallable(String tenant, OffsetDateTime now, String datasourceId,
            String dsiId, List<DataObject> list) {
        return () -> {
            runtimeTenantResolver.forceTenant(tenant);
            sendMessage(String.format("  Indexing %d objects...", list.size()), dsiId);
            BulkSaveResult bulkSaveResult = entityIndexerService.mergeDataObjects(tenant, datasourceId, now, list);
            if (bulkSaveResult.getInErrorDocsCount() > 0) {
                sendMessage(String.format("  ...%d objects cannot be saved:\n%s", bulkSaveResult.getInErrorDocsCount(),
                                          bulkSaveResult.getDetailedErrorMsg().replace("\n", "\n    ")),
                            dsiId);
            }
            sendMessage(String.format("  ...%d objects effectively indexed.", bulkSaveResult.getSavedDocsCount()),
                        dsiId);
            return bulkSaveResult;
        };
    }

    /**
     * Read datasource since given date page setting ipId to each objects
     * @param date date from which to read datasource data
     */
    private Page<DataObject> findAllFromDatasource(OffsetDateTime date, String tenant, IDataSourcePlugin dsPlugin,
            String datasourceId, Pageable pageable) throws DataSourceException, ModuleException {
        // Retrieve target model
        Model model = modelService.getModelByName(dsPlugin.getModelName());

        // Find all features
        Page<DataObjectFeature> page;
        try {
            page = dsPlugin.findAll(tenant, pageable, date);
        } catch (Exception e) {
            // Catch Exception in order to catch all exceptions from plugins. Plugins can be out of our scope.
            notificationClient.notify(e.getMessage(), "Datasource harvesting failure", NotificationLevel.ERROR,
                                      DefaultRole.ADMIN);
            LOGGER.error("Cannot retrieve data from datasource", e);
            throw e;
        }

        // Decorate features with its related entity (i.e. DataObject)
        List<DataObject> dataObjects = new ArrayList<>();

        for (DataObjectFeature feature : page.getContent()) {
            // Wrap each feature into its decorator
            DataObject dataObject = DataObject.wrap(model, feature,
                                                    IAipDataSourcePlugin.class.isAssignableFrom(dsPlugin.getClass()));
            dataObject.setDataSourceId(datasourceId);
            // Generate IpId only if datasource plugin hasn't yet generate it
            if (dataObject.getIpId().isRandomEntityId()) {
                dataObject.setIpId(buildIpId(tenant, dataObject.getProviderId(), datasourceId));
            }
            // Manage geometries
            if (feature.getGeometry() != null) {
                // This geometry has been set by plugin, IT IS NOT NORMALIZED
                IGeometry geometry = feature.getGeometry();
                // The crs is brought by project so it must be set on feature and taken into account for geometry
                // normalization
                feature.setCrs(projectGeoSettings.getCrs().toString());
                // Always normalize geometry in its origin CRS
                feature.setNormalizedGeometry(GeoHelper.normalize(geometry));
                // Then manage projected (or not) geometry into WGS84
                if (!feature.getCrs().get().equals(Crs.WGS_84.toString())) {
                    try {
                        // Transform to Wgs84...(not normalized one from its origin CRS)
                        IGeometry wgs84Geometry = GeoHelper.transform(geometry, Crs.valueOf(feature.getCrs().get()),
                                                                      Crs.WGS_84);
                        // ...and save it onto DataObject after having normalized it
                        dataObject.setWgs84(GeoHelper.normalize(wgs84Geometry));
                    } catch (IllegalArgumentException e) {
                        throw new RsRuntimeException(
                                String.format("Given Crs '%s' is not allowed.", feature.getCrs().get()), e);
                    }
                } else { // Even if Crs is WGS84, don't forget to normalize geometry (already done into feature)
                    dataObject.setWgs84(feature.getNormalizedGeometry());
                }
            }

            dataObjects.add(dataObject);
        }

        // Build decorated page
        return new PageImpl<>(dataObjects, PageRequest.of(page.getNumber(), page.getSize() == 0 ? 1 : page.getSize()),
                page.getTotalElements());
    }

    @Override
    public List<DatasourceIngestion> getDatasourceIngestions() {
        return datasourceIngestionRepo.findAll(Sort.by("label"));
    }

    @Override
    public void deleteDatasourceIngestion(String id) {
        datasourceIngestionRepo.deleteById(id);
    }

    @Override
    public void scheduleNowDatasourceIngestion(String id) {
        DatasourceIngestion dsi = datasourceIngestionRepo.findById(id).get();
        dsi.setNextPlannedIngestDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        datasourceIngestionRepo.save(dsi);
    }

    /**
     * Send a message to IngesterService (or whoever want to listen to it) concerning given datasourceIngestionId
     */
    public void sendMessage(String message, String dsId) {
        String msg = String.format("%s: %s",
                                   ISO_TIME_UTC.format(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC)),
                                   message);
        eventPublisher.publishEvent(new DataSourceMessageEvent(this, runtimeTenantResolver.getTenant(), msg, dsId));
    }
}
