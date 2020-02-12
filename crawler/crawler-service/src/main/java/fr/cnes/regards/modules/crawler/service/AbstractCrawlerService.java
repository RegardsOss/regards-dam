package fr.cnes.regards.modules.crawler.service;

import java.lang.reflect.ParameterizedType;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.amqp.IPoller;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.framework.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.entities.event.AbstractEntityEvent;

/**
 * Abstract crawler service.
 * This service is intended to be specialized for Dataset (DatasetCrawlerService) and other entity than Dataset
 * (CrawlerService)
 * @author oroussel
 */
public abstract class AbstractCrawlerService<T extends AbstractEntityEvent> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractCrawlerService.class);

    /**
     * To avoid CPU overload, a delay is set between each loop of tenants event inspection. This delay is doubled each
     * time no event has been pulled (limited to MAX_DELAY_MS). When an event is pulled (during a tenants event
     * inspection), no wait is done and delay is reset to INITIAL_DELAY_MS
     */
    private static final int INITIAL_DELAY_MS = 1;

    /**
     * To avoid CPU overload, a delay is set between each loop of tenants event inspection. This delay is doubled each
     * time no event has been pulled (limited to MAX_DELAY_MS). When an event is pulled (during a tenants event
     * inspection), no wait is done and delay is reset to INITIAL_DELAY_MS
     */
    private static final int MAX_DELAY_MS = 1000;

    /**
     * All tenants resolver
     */
    @Autowired
    private ITenantResolver tenantResolver;

    /**
     * Current tenant resolver
     */
    @Autowired
    protected IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    protected IEntityIndexerService entityIndexerService;

    /**
     * AMQP poller
     */
    @Autowired
    private IPoller poller;

    /**
     * Indicate that daemon stop has been asked
     */
    private boolean stopAsked = false;

    /**
     * Current delay between all tenants poll check
     */
    private final AtomicInteger delay = new AtomicInteger(INITIAL_DELAY_MS);

    /**
     * Boolean indicating that a work is scheduled
     */
    private static boolean scheduledWork = false;

    /**
     * Boolean indicating that something has been done
     */
    private static boolean somethingDone = false;

    /**
     * Boolean indicating that something is currently in progress
     */
    private static boolean inProgress = false;

    /**
     * Boolean indicating wether or not crawler service is in "consume only" mode (to be used by tests only)
     */
    private static boolean consumeOnlyMode = false;

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    protected AbstractCrawlerService() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    /**
     * Ask for termination of daemon process
     */
    @PreDestroy
    private void endCrawl() {
        stopAsked = true;
    }

    /**
     * Daemon process. Poll entity events on all tenants and update Elasticsearch to reflect Postgres database
     */
    public void crawl(Supplier<Boolean> pollMethod) {
        delay.set(INITIAL_DELAY_MS);
        // Infinite loop
        while (true) {
            // Manage termination
            if (stopAsked) {
                break;
            }
            boolean atLeastOnePoll = false;
            // For all tenants
            for (String tenant : tenantResolver.getAllActiveTenants()) {
                try {
                    runtimeTenantResolver.forceTenant(tenant);
                    // Try to poll an entity event on this tenant
                    atLeastOnePoll |= pollMethod.get();
                } catch (Exception e) {
                    LOGGER.error("Cannot manage entity event message", e);
                }
                // Reset inProgress AFTER transaction
                inProgress = false;
            }
            // If a poll has been done, don't wait and reset delay to initial value
            if (atLeastOnePoll) {
                delay.set(INITIAL_DELAY_MS);
            } else { // else, wait and double delay for next time (limited to MAX_DELAY)
                try {
                    Thread.sleep(delay.get());
                    delay.set(Math.min(delay.get() * 2, MAX_DELAY_MS));
                } catch (InterruptedException e) {
                    LOGGER.error("Thread sleep interrupted.");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Try to do a transactional poll. If a poll is done but an exception occurs, the transaction is roll backed and the
     * event is still present into AMQP
     * @return true if a poll has been done, false otherwise
     */
    @MultitenantTransactional
    public boolean doPoll() {
        boolean atLeastOnePoll = false;
        // Try to poll an AbstractEntityEvent
        TenantWrapper<T> wrapper = poller.poll(this.entityClass);
        if (wrapper != null) {
            String tenant = wrapper.getTenant();
            LOGGER.info("Received message from tenant {} created at {}...", tenant, wrapper.getDate());
            UniformResourceName[] ipIds = wrapper.getContent().getIpIds();
            if ((ipIds != null) && (ipIds.length != 0)) {
                LOGGER.debug("IpIds received {}", Arrays.toString(ipIds));
                atLeastOnePoll = true;
                // Message consume only, nothing else to be done, returning...
                if (consumeOnlyMode) {
                    LOGGER.debug("CONSUME ONLY MODE TRUE !!!!");
                    return atLeastOnePoll;
                }
                inProgress = true;
                OffsetDateTime now = OffsetDateTime.now();
                Arrays.stream(ipIds).forEach(ipId -> {
                    try {
                        entityIndexerService.updateEntityIntoEs(tenant, ipId, now, false);
                    } catch (ModuleException e) {
                        LOGGER.error("Error handling entity update events", e);
                        // FIXME notify
                    }
                });
                somethingDone = true;
            }
        }
        return atLeastOnePoll;
    }

    public boolean working() { // NOSONAR : test purpose
        return delay.get() < MAX_DELAY_MS;
    }

    public boolean workingHard() { // NOSONAR : test purpose
        return delay.get() == INITIAL_DELAY_MS;
    }

    public boolean strolling() { // NOSONAR : test purpose
        return delay.get() == MAX_DELAY_MS;
    }

    public void startWork() { // NOSONAR : test purpose
        // If crawler is busy, wait for it
        while (working()) {
            ;
        }
        scheduledWork = true;
        somethingDone = false;
        LOGGER.info("start working...");
    }

    public void waitForEndOfWork() throws InterruptedException { // NOSONAR : test purpose
        if (!scheduledWork) {
            throw new IllegalStateException("Before waiting, startWork() must be called");
        }
        LOGGER.info("Waiting for work end");
        // In case work hasn't started yet.
        Thread.sleep(3_000);
        // As soon as something has been done, we wait for crawler service to no more be busy
        while (inProgress || (!somethingDone && !strolling())) {
            Thread.sleep(1_000);
        }
        LOGGER.info("...Work ended");
        scheduledWork = false;
    }

    public void setConsumeOnlyMode(boolean b) {
        consumeOnlyMode = b;
    }
}
