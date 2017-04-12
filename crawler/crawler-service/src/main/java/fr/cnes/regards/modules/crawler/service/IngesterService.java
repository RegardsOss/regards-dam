package fr.cnes.regards.modules.crawler.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.crawler.dao.IDatasourceIngestionRepository;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.crawler.domain.IngestionStatus;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;

@Service
public class IngesterService implements IIngesterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngesterService.class);

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
    private ICrawlerService crawlerService;

    @Autowired
    private IPluginService pluginService;

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
     * Once IIngesterService bean has been initialized, retrieve self proxy to permit transactional calls.
     */
    @PostConstruct
    private void init() {
        self = applicationContext.getBean(IIngesterService.class);
    }

    /**
     * By default, launched 15 mn after last one
     */
    @Override
    @Scheduled(fixedRateString = "${regards.ingester.rate.ms:900000}")
    public void manage() {
        // First, update all DatasourceIngestions of all tenants (to reflect all datasource plugin configurations
        // states and to update nextPlannedIngestDate)
        for (String tenant : tenantResolver.getAllActiveTenants()) {
            runtimeTenantResolver.forceTenant(tenant);

            self.updateAndCleanTenantDatasourceIngestions(tenant);
        }
        // Then ingest...
        boolean atLeastOnIngestionDone;
        do {
            atLeastOnIngestionDone = false;
            for (String tenant : tenantResolver.getAllActiveTenants()) {
                runtimeTenantResolver.forceTenant(tenant);

                // Pick an available dsIngestion marking it as STARTED if present
                Optional<DatasourceIngestion> dsIngestionOpt = self.pickAndStartDatasourceIngestion(tenant);
                if (dsIngestionOpt.isPresent()) {
                    atLeastOnIngestionDone = true;
                    DatasourceIngestion dsIngestion = dsIngestionOpt.get();
                    try {
                        // Launch datasource ingestion
                        IngestionResult summary = crawlerService
                                .ingest(pluginService.loadPluginConfiguration(dsIngestion.getId()),
                                        dsIngestion.getLastIngestDate());
                        dsIngestion.setStatus(IngestionStatus.FINISHED);
                        dsIngestion.setSavedObjectsCount(summary.getSavedObjectsCount());
                        dsIngestion.setLastIngestDate(summary.getDate());
                    } catch (ModuleException | RuntimeException e) {
                        dsIngestion.setStatus(IngestionStatus.ERROR);

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
        } while (atLeastOnIngestionDone);
    }

    @Override
    @Transactional
    public void updateAndCleanTenantDatasourceIngestions(String tenant) {
        // First, check if all existing datasource plugins are managed
        Map<Long, DatasourceIngestion> dsIngestionsMap = dsIngestionRepos.findAll().stream()
                .collect(Collectors.toMap(DatasourceIngestion::getId, Function.identity()));
        List<PluginConfiguration> pluginConfs = new ArrayList<>(
                pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class));
        // Add DatasourceIngestion for unmanaged datasource with immediate next planned ingestion date
        pluginConfs.stream().mapToLong(PluginConfiguration::getId).filter(id -> !dsIngestionRepos.exists(id))
                .mapToObj(id -> dsIngestionRepos.save(new DatasourceIngestion(id, LocalDateTime.now())))
                .forEach(dsIngestion -> dsIngestionsMap.put(dsIngestion.getId(), dsIngestion));
        // Remove DatasourceIngestion for removed datasources
        dsIngestionsMap.keySet().stream().filter(id -> !pluginService.exists(id)).forEach(dsIngestionRepos::delete);
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

    /**
     * Compute next ingestion planned date if needed
     */
    private void updatePlannedDate(DatasourceIngestion dsIngestion, int refreshRate) {
        switch (dsIngestion.getStatus()) {
            case ERROR: // las ingest in error, launch as soon as possible with same ingest date (last one with no error)
                dsIngestion.setNextPlannedIngestDate(dsIngestion.getLastIngestDate());
                dsIngestionRepos.save(dsIngestion);
                break;
            case FINISHED: // last ingest + refreshRate
                dsIngestion.setNextPlannedIngestDate(dsIngestion.getLastIngestDate().plus(refreshRate,
                                                                                          ChronoUnit.SECONDS));
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
    @Transactional
    public Optional<DatasourceIngestion> pickAndStartDatasourceIngestion(String pTenant) {
        Optional<DatasourceIngestion> dsIngestionOpt = dsIngestionRepos
                .findTopByNextPlannedIngestDateLessThanAndStatusNot(LocalDateTime.now(), IngestionStatus.STARTED);
        if (dsIngestionOpt.isPresent()) {
            DatasourceIngestion dsIngestion = dsIngestionOpt.get();
            dsIngestion.setStatus(IngestionStatus.STARTED);
            dsIngestionRepos.save(dsIngestion);
        }
        return dsIngestionOpt;
    }

}
