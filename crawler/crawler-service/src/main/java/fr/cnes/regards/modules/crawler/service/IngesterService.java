package fr.cnes.regards.modules.crawler.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPoller;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.InactiveDatasourceException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.event.PluginConfEvent;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.crawler.dao.IDatasourceIngestionRepository;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.crawler.domain.IngestionStatus;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

@Service// Transactionnal is handle by hand on the right method, do not specify Multitenant or InstanceTransactionnal
public class IngesterService implements IIngesterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngesterService.class);

    /**
     * To avoid CPU overload, a delay is set between each loop of tenants event inspection. This delay is doubled each
     * time no event has been pulled (limited to MAX_DELAY_MS). When an event is pulled (during a tenants event
     * inspection), no wait is done and delay is reset to INITIAL_DELAY_MS
     */
    private static final int INITIAL_DELAY_MS = 1000;

    /**
     * To avoid CPU overload, a delay is set between each loop of tenants event inspection. This delay is doubled each
     * time no event has been pulled (limited to MAX_DELAY_MS). When an event is pulled (during a tenants event
     * inspection), no wait is done and delay is reset to INITIAL_DELAY_MS
     */
    private static final int MAX_DELAY_MS = 10000;

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

    @Autowired
    private IDatasourceIngestionRepository dsIngestionRepos;

    @Autowired
    private IDatasourceIngesterService datasourceIngester;

    /**
     * Only used to delete all data objects from a removed datasource
     */
    @Autowired
    private IEsRepository esRepos;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IPoller poller;

    /**
     * To retrieve IIngesterService (self) proxy
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Proxied version of this service
     */
    private IIngesterService self;

    /**
     * Indicate that daemon stop has been asked
     */
    private boolean stopAsked = false;

    /**
     * Current delay between all tenants poll check
     */
    private final AtomicInteger delay = new AtomicInteger(INITIAL_DELAY_MS);

    /**
     * Once IIngesterService bean has been initialized, retrieve self proxy to permit transactional calls.
     */
    @PostConstruct
    private void init() {
        self = applicationContext.getBean(IIngesterService.class);
    }

    /**
     * An atomic boolean used to determine wether manage() method is currently executing (and avoid launching it
     * in parallel)
     */
    private static final AtomicBoolean managing = new AtomicBoolean(false);

    /**
     * An atomic boolean permitting to take into account a new datasource creation or update while managing current ones
     * (or inverse)
     */
    private static AtomicBoolean doItAgain = new AtomicBoolean(false);

    /**
     * Boolean indicating wether or not crawler service is in "consume only" mode (to be used by tests only)
     */
    private boolean consumeOnlyMode = false;

    @Override
    @Async
    public void listenToPluginConfChange() {

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
                    // Try to poll a plugin conf event on this tenant
                    TenantWrapper<PluginConfEvent> wrapper = poller.poll(PluginConfEvent.class);
                    if (wrapper != null) {
                        // If it concerns a Datasource, manage it
                        if (wrapper.getContent().getPluginTypes().contains(IDataSourcePlugin.class.getName())) {
                            if (!this.consumeOnlyMode) {
                                this.manage();
                            }
                        }
                    }
                } catch (RuntimeException t) {
                    LOGGER.error("Cannot manage plugin conf event message", t);
                }
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
     * Ask for termination of daemon process
     */
    @PreDestroy
    private void endListenToPluginConfChange() {
        stopAsked = true;
    }

    /**
     * By default, launched 5 mn after last one. BUT this method is also executed each time a datasource is created
     */
    @Override
    @Scheduled(fixedDelayString = "${regards.ingester.rate.ms:300000}")
    public void manage() {
        LOGGER.info("IngesterService.manage() called...");
        try {
            // if this method is called while currently been executed, doItAgain is set to true and nothing else is
            // done
            if (managing.getAndSet(true)) {
                doItAgain = new AtomicBoolean(true);
                return;
            }

            do {
                // First, update all DatasourceIngestions of all tenants (to reflect all datasource plugin configurations
                // states and to update nextPlannedIngestDate)
                for (String tenant : tenantResolver.getAllActiveTenants()) {
                    runtimeTenantResolver.forceTenant(tenant);

                    self.updateAndCleanTenantDatasourceIngestions(tenant);
                }
                // Then ingest...
                boolean atLeastOneIngestionDone;
                do {
                    atLeastOneIngestionDone = false;
                    for (String tenant : tenantResolver.getAllActiveTenants()) {
                        runtimeTenantResolver.forceTenant(tenant);

                        // Pick an available dsIngestion marking it as STARTED if present
                        Optional<DatasourceIngestion> dsIngestionOpt = self.pickAndStartDatasourceIngestion(tenant);
                        if (dsIngestionOpt.isPresent()) {
                            atLeastOneIngestionDone = true;
                            DatasourceIngestion dsIngestion = dsIngestionOpt.get();

                            try {
                                // Launch datasource ingestion
                                IngestionResult summary = datasourceIngester
                                        .ingest(pluginService.loadPluginConfiguration(dsIngestion.getId()),
                                                dsIngestion.getLastIngestDate());
                                dsIngestion.setStatus(IngestionStatus.FINISHED);
                                dsIngestion.setSavedObjectsCount(summary.getSavedObjectsCount());
                                dsIngestion.setLastIngestDate(summary.getDate());
                            } catch (InactiveDatasourceException ide) {
                                dsIngestion.setStatus(IngestionStatus.INACTIVE);
                                dsIngestion.setStackTrace(ide.getMessage());
                            } catch (Exception e) {
                                // Set Status to Error... (and status date)
                                dsIngestion.setStatus(IngestionStatus.ERROR);
                                // and log stack trace into database
                                StringWriter sw = new StringWriter();
                                e.printStackTrace(new PrintWriter(sw));
                                dsIngestion.setStackTrace(sw.toString());
                            }
                            // To avoid redoing an ingestion in this "do...while" (must be at next call to manage)
                            dsIngestion.setNextPlannedIngestDate(null);
                            // Save ingestion status
                            dsIngestionRepos.save(dsIngestion);
                        }
                    }
                    // At least one ingestion has to be done while looping through all tenants
                } while (atLeastOneIngestionDone);
                // set doItAgain to false in all cases and redo if asked to (this means a datasource has been created
                // or updated while manage() method was currently executing
            } while (doItAgain.getAndSet(false));
        } finally { // In all cases, set managing to false
            managing.set(false);
        }
        LOGGER.info("...IngesterService.manage() ended.");
    }

    @Override
    @MultitenantTransactional
    public void updateAndCleanTenantDatasourceIngestions(String tenant) {
        // First, check if all existing datasource plugins are managed
        Map<Long, DatasourceIngestion> dsIngestionsMap = dsIngestionRepos.findAll().stream()
                .collect(Collectors.toMap(DatasourceIngestion::getId, Function.identity()));
        List<PluginConfiguration> pluginConfs = new ArrayList<>(
                pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class));
        // Add DatasourceIngestion for unmanaged datasource with immediate next planned ingestion date
        pluginConfs.stream().mapToLong(PluginConfiguration::getId).filter(id -> !dsIngestionRepos.exists(id)).mapToObj(
                id -> dsIngestionRepos
                        .save(new DatasourceIngestion(id, OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC))))
                .forEach(dsIngestion -> dsIngestionsMap.put(dsIngestion.getId(), dsIngestion));
        // Remove DatasourceIngestion for removed datasources and plan data objects deletion from Elasticsearch
        dsIngestionsMap.keySet().stream().filter(id -> !pluginService.exists(id))
                .peek(id -> this.planDatasourceDataObjectsDeletion(tenant, id)).forEach(dsIngestionRepos::delete);
        // For previously ingested datasources, compute next planned ingestion date
        pluginConfs.stream().forEach(pluginConf -> {
            try {
                this.updatePlannedDate(dsIngestionsMap.get(pluginConf.getId()),
                                       ((IDataSourcePlugin) pluginService.getPlugin(pluginConf)).getRefreshRate());
            } catch (ModuleException e) {
                LOGGER.error("Cannot compute next ingestion planned date", e);
            }
        });
    }

    private ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(1);

    /**
     * Create a task to launch datasource data objects deletion later (use a thread pool of size 1)
     */
    public void planDatasourceDataObjectsDeletion(String tenant, Long dataSourceId) {
        threadPoolExecutor.submit(() -> esRepos.deleteByQuery(tenant, ICriterion.eq("dataSourceId", dataSourceId)));
    }

    /**
     * Compute next ingestion planned date if needed
     */
    private void updatePlannedDate(DatasourceIngestion dsIngestion, int refreshRate) {
        switch (dsIngestion.getStatus()) {
            case ERROR: // las ingest in error, launch as soon as possible with same ingest date (last one with no error)
                OffsetDateTime nextPlannedIngestDate = (dsIngestion.getLastIngestDate() == null) ?
                        OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC) :
                        dsIngestion.getLastIngestDate();
                dsIngestion.setNextPlannedIngestDate(nextPlannedIngestDate);
                dsIngestionRepos.save(dsIngestion);
                break;
            case FINISHED: // last ingest + refreshRate
                dsIngestion.setNextPlannedIngestDate(
                        dsIngestion.getLastIngestDate().plus(refreshRate, ChronoUnit.SECONDS));
                dsIngestionRepos.save(dsIngestion);
                break;
            case STARTED: // Already in progress
            case NEW: // dsIngestion just been created with a next planned date as now() ie launch as soon as possible
            default:
        }
    }

    /**
     * Find a Datasource ready to be ingested and mark it at "CREATED" in a transaction
     */
    @Override
    @MultitenantTransactional
    public Optional<DatasourceIngestion> pickAndStartDatasourceIngestion(String pTenant) {
        Optional<DatasourceIngestion> dsIngestionOpt = dsIngestionRepos
                .findTopByNextPlannedIngestDateLessThanAndStatusNot(
                        OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC), IngestionStatus.STARTED);
        if (dsIngestionOpt.isPresent()) {
            DatasourceIngestion dsIngestion = dsIngestionOpt.get();
            // Reinit old DatasourceIngestion properties
            dsIngestion.setStackTrace(null);
            dsIngestion.setSavedObjectsCount(0);
            dsIngestion.setStatus(IngestionStatus.STARTED);
            dsIngestionRepos.save(dsIngestion);
        }
        return dsIngestionOpt;
    }

    @Override
    public void setConsumeOnlyMode(boolean b) {
        consumeOnlyMode = b;
    }

}
