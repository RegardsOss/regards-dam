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
     * @param pPluginConfiguration datasource plugin configuration
     */
    void ingest(PluginConfiguration pPluginConfiguration) throws ModuleException;
}
