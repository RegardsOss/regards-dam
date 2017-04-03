package fr.cnes.regards.modules.crawler.service;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;

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
     * @return the count of DataObjects ingested from given datasource
     */
    int ingest(PluginConfiguration pluginConfiguration) throws ModuleException;

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
}
