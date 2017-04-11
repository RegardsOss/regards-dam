package fr.cnes.regards.modules.crawler.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.entities.domain.Dataset;

/**
 * Crawler interface.
 * @author oroussel
 */
public interface ICrawlerService {

    /**
     * Crawl daemon method.
     * This method is called asynchronously and never ends.
     * It waits for AMQP events comming from entity module informing that an entity has been created/updated/deleted
     * and then do associated operation on Elastisearch.
     */
    void crawl();

    /**
     * Transactional method processing an event polling on AMQP and Elasticsearch associated operation.
     * If something failed, the transaction is rolled back and AMQP event is automaticaly reposted.
     * <b>This method must not be called</b>
     * @return true if some event has been polled
     */
    boolean doPoll();

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
    IngestionResult ingest(PluginConfiguration pluginConfiguration, LocalDateTime date) throws ModuleException;

    /**
     * Transactional method updating a page of datasets
     * @param lastUpdateDate Take into account only more recent lastUpdateDate than provided
     */
    void updateDatasets(String tenant, Page<Dataset> dsDatasetsPage, LocalDateTime lastUpdateDate);

    /**
     * To be used by tests only.
     * Set a landmark used by method {@link #waitForEndOfWork()}
     */
    void startWork();

    /**
     * Once {@link #startWork()} has been called, wait for the crawler to no more be busy (it must have do something)
     * @throws InterruptedException
     */
    void waitForEndOfWork() throws InterruptedException;

    /**
     * Indicate that the daemon service is currently working ie delay between poll of events is strictly between its
     * maximum
     */
    boolean working();

    /**
     * Indicate that the daemon service is currently working hard ie delay between poll of events is at its minimal
     * value
     */
    boolean workingHard();

    /**
     * Indicate that the daemon service is waiting for events ie deley between poll of events is at its maximum
     */
    boolean strolling();

    /**
     * Set or unset "consume only" mode where messages are polled but nothing is done
     * @param b true or false (it's a boolean, what do you expect ?)
     */
    void setConsumeOnlyMode(boolean b);
}
