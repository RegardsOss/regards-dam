package fr.cnes.regards.modules.crawler.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import fr.cnes.regards.framework.module.rest.exception.InactiveDatasourceException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.crawler.service.exception.NotFinishedException;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.DataSourceException;

/**
 * @author oroussel
 */
public interface IDatasourceIngesterService {

    /**
     * Ingest provided datasource (from plugin configuration) data objects into Elasticsearch
     * @param dsi datasource ingestion status object
     * @return a summary containing the count of DataObjects ingested from given datasource and the ingestion date
     */
    Optional<IngestionResult> ingest(String dsi) throws ModuleException, InterruptedException, ExecutionException,
            DataSourceException, NotFinishedException, InactiveDatasourceException;

    /**
     * Retrieve all {@link DatasourceIngestion}
     */
    List<DatasourceIngestion> getDatasourceIngestions();

    /**
     * Delete given {@link DatasourceIngestion}
     * @param id DatasourceIngestion id
     */
    void deleteDatasourceIngestion(String id);

    /**
     * Schedule datasource ingestion to be executed as soon as possible
     */
    void scheduleNowDatasourceIngestion(String id);
}
