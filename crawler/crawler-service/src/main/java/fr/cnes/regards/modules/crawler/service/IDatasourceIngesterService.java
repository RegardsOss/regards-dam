package fr.cnes.regards.modules.crawler.service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.datasources.domain.plugins.DataSourceException;

/**
 * @author oroussel
 */
public interface IDatasourceIngesterService {

    /**
     * Ingest provided datasource (from plugin configuration) data objects into Elasticsearch
     * @param pluginConfiguration datasource plugin configuration
     * @param dsi datasource ingestion status object
     * @return a summary containing the count of DataObjects ingested from given datasource and the ingestion date
     */
    IngestionResult ingest(PluginConfiguration pluginConfiguration, DatasourceIngestion dsi)
            throws ModuleException, InterruptedException, ExecutionException, DataSourceException;

    /**
     * Retrieve all {@link DatasourceIngestion}
     */
    List<DatasourceIngestion> getDatasourceIngestions();

    /**
     * Delete given {@link DatasourceIngestion}
     * @param id DatasourceIngestion id
     */
    void deleteDatasourceIngestion(Long id);
}
