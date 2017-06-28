package fr.cnes.regards.modules.crawler.service;

import java.time.OffsetDateTime;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;

/**
 * @author oroussel
 */
public interface IDatasourceIngesterService {
    /**
     * Ingest provided datasource (from plugin configuration) data objects into Elasticsearch
     * @param pluginConfiguration datasource plugin configuration
     * @return a summary containing the count of DataObjects ingested from given datasource and the ingestion date
     */
    default IngestionResult ingest(PluginConfiguration pluginConfiguration) throws ModuleException {
        return this.ingest(pluginConfiguration, null);
    }

    /**
     * Ingest provided datasource (from plugin configuration) data objects into Elasticsearch
     * @param pluginConfiguration datasource plugin configuration
     * @param date date used for finding objects on datasource (strictly greatest than)
     * @return a summary containing the count of DataObjects ingested from given datasource and the ingestion date
     */
    IngestionResult ingest(PluginConfiguration pluginConfiguration, OffsetDateTime date) throws ModuleException;
}
