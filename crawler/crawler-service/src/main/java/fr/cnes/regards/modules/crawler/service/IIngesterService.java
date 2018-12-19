package fr.cnes.regards.modules.crawler.service;

import java.util.Optional;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.service.event.MessageEvent;
import fr.cnes.regards.modules.dam.gson.entities.DamGsonReadyEvent;

/**
 * Ingester service interface.<br/>
 * Manage all periodic datasource ingestions.
 * @author oroussel
 */
public interface IIngesterService {

    void manage();

    void updateAndCleanTenantDatasourceIngestions(String tenant);

    Optional<DatasourceIngestion> pickAndStartDatasourceIngestion(String tenant);

    /**
     * Set or unset "consume only" mode where messages are polled but nothing is done
     * @param b true or false (it's a boolean, what do you expect ?)
     */
    void setConsumeOnlyMode(boolean b);

    void updatePlannedDate(DatasourceIngestion dsIngestion, Long pluginConfId) throws ModuleException;

    void handleApplicationReadyEvent(DamGsonReadyEvent event);

    void handleMessageEvent(MessageEvent event);
}
