package fr.cnes.regards.modules.crawler.service;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.event.AccessRightEvent;
import fr.cnes.regards.modules.dam.domain.entities.event.DatasetEvent;

/**
 * Crawler service for Dataset. <b>This service need @EnableSchedule at Configuration</b>
 * @author oroussel
 */
@Service
public class DatasetCrawlerService extends AbstractCrawlerService<DatasetEvent>
        implements ApplicationListener<ApplicationReadyEvent>, IDatasetCrawlerService, IHandler<AccessRightEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetCrawlerService.class);

    @Autowired
    private ISubscriber subscriber;

    /**
     * Self proxy
     */
    @Autowired
    @Lazy
    private IDatasetCrawlerService self;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        subscriber.subscribeTo(AccessRightEvent.class, this);
    }

    @Override
    @Async
    public void crawl() {
        super.crawl(self::doPoll);
    }

    @Override
    public void handle(TenantWrapper<AccessRightEvent> wrapper) {
        if (wrapper.getContent() != null) {
            AccessRightEvent event = wrapper.getContent();
            try {
                entityIndexerService.updateEntityIntoEs(wrapper.getTenant(), event.getDatasetIpId(),
                                                        OffsetDateTime.now(), false);
            } catch (ModuleException e) {
                LOGGER.error("Cannot handle access right event", e);
                // FIXME notify
            }
        }
    }
}
