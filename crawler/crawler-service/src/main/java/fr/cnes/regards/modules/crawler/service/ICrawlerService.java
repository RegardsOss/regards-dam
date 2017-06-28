package fr.cnes.regards.modules.crawler.service;

/**
 * Crawler interface.
 * <b>This interface is package private to avoid autowiring ICrawlerService.</b>
 * @see ICrawlerAndIngesterService for a crawler and ingester service
 * @see IDatasetCrawlerService for a Dataset specialized crawlerService
 * @author oroussel
 */
interface ICrawlerService {

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
