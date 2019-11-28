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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.module.rest.exception.InactiveDatasourceException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.event.PluginConfEvent;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.service.event.DataSourceMessageEvent;
import fr.cnes.regards.modules.crawler.service.exception.NotFinishedException;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.DataSourceException;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDataSourcePlugin;
import fr.cnes.regards.modules.dam.gson.entities.DamGsonReadyEvent;

/**
 * Component used to schedule new {@link DatasourceIngestion} to ingest features in ES catalog for each tenants
 *
 * @author oroussel
 * @author Sébastien Binda
 */
@Component
public class IngesterService implements IHandler<PluginConfEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngesterService.class);

    /**
     * An atomic boolean used to determine whether manage() method is currently executing (and avoid launching it
     * in parallel)
     */
    private static final AtomicBoolean managing = new AtomicBoolean(false);

    /**
     * An atomic boolean permitting to take into account a new data source creation or update while managing current ones
     * (or inverse)
     */
    private static AtomicBoolean doItAgain = new AtomicBoolean(false);

    @Autowired
    private ITenantResolver tenantResolver;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private ISubscriber subscriber;

    @Autowired
    private DatasourceIngestionService dsIngestionService;

    /**
     * Boolean indicating whether or not crawler service is in "consume only" mode (to be used by tests only)
     */
    private boolean consumeOnlyMode = false;

    @EventListener
    public void handleApplicationReadyEvent(DamGsonReadyEvent event) {
        subscriber.subscribeTo(PluginConfEvent.class, this);
    }

    /**
     * Receiving a message from crawler
     */
    @EventListener
    public void handleMessageEvent(DataSourceMessageEvent event) {
        runtimeTenantResolver.forceTenant(event.getTenant());
        dsIngestionService.addMessageToStackTrace(event.getDataSourceId(), event.getMessage());
    }

    @Override
    public void handle(TenantWrapper<PluginConfEvent> wrapper) {
        try {
            runtimeTenantResolver.forceTenant(wrapper.getTenant());
            // If it concerns a data source, manage it
            if (wrapper.getContent().getPluginTypes().contains(IDataSourcePlugin.class.getName())) {
                if (!this.consumeOnlyMode) {
                    this.manage();
                }
            }
        } catch (RuntimeException t) {
            LOGGER.error("Cannot manage plugin conf event message", t);
        }
    }

    /**
     * By default, launched 5 mn after last one. BUT this method is also executed each time a datasource is created
     * Initial delay of 2 mn to avoid been launched too soon.
     */
    @Scheduled(initialDelay = 300000, fixedDelayString = "${regards.ingester.rate.ms:300000}")
    public void manage() {
        LOGGER.info("IngesterService.manage() called...");
        try {
            // if this method is called while currently been executed, doItAgain is set to true and nothing else is done
            if (managing.getAndSet(true)) {
                doItAgain.set(true);
                return;
            }
            do {
                // First, update all DatasourceIngestions of all tenants (to reflect all datasource plugin configurations
                // states and to update nextPlannedIngestDate)
                for (String tenant : tenantResolver.getAllActiveTenants()) {
                    runtimeTenantResolver.forceTenant(tenant);
                    dsIngestionService.updateAndCleanTenantDatasourceIngestions();
                }
                // Then ingest...
                boolean atLeastOneIngestionDone;
                do {
                    atLeastOneIngestionDone = false;
                    for (String tenant : tenantResolver.getAllActiveTenants()) {
                        runtimeTenantResolver.forceTenant(tenant);
                        // Pick an available dsIngestion marking it as STARTED if present
                        Optional<String> dsIngestionOpt = dsIngestionService.pickAndStartDatasourceIngestion();
                        if (dsIngestionOpt.isPresent()) {
                            String dsId = dsIngestionOpt.get();
                            atLeastOneIngestionDone = true;
                            try {
                                dsIngestionService.runDataSourceIngestion(dsId);
                            } catch (InactiveDatasourceException ide) {
                                LOGGER.error(ide.getMessage(), ide);
                                dsIngestionService.setInactive(dsId, ide.getMessage());
                            } catch (NotFinishedException nfe) {
                                LOGGER.error(nfe.getMessage(), nfe);
                                dsIngestionService.setNotFinished(dsId, nfe);
                            } catch (DataSourceException | ModuleException e) {
                                LOGGER.error(e.getMessage(), e);
                                try (StringWriter sw = new StringWriter()) {
                                    e.printStackTrace(new PrintWriter(sw));
                                    dsIngestionService.setError(dsId, sw.toString());
                                } catch (IOException e1) {
                                    LOGGER.error(e.getMessage(), e);
                                }
                            }
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

    /**
     * Set or unset "consume only" mode where messages are polled but nothing is done
     * @param b true or false (it's a boolean, what do you expect ?)
     */
    public void setConsumeOnlyMode(boolean b) {
        consumeOnlyMode = b;
    }

}
