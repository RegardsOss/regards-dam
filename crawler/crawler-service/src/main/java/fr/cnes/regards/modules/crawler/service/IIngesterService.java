package fr.cnes.regards.modules.crawler.service;

import java.util.Optional;

import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;

/**
 * Ingester service interface.<br/>
 * Manage all periodic datasource ingestions.
 * @author oroussel
 */
public interface IIngesterService {

    public void manage();

    public void updateAndCleanTenantDatasourceIngestions(String tenant);

    public Optional<DatasourceIngestion> pickAndStartDatasourceIngestion(String tenant);
}
