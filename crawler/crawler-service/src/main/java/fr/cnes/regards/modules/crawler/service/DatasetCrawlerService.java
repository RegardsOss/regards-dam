package fr.cnes.regards.modules.crawler.service;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.entities.domain.event.DatasetEvent;

/**
 * Crawler service for Dataset. <b>This service need @EnableSchedule at Configuration</b>
 * @author oroussel
 */
@Service
public class DatasetCrawlerService extends AbstractCrawlerService<DatasetEvent> implements IDatasetCrawlerService {

    /**
     * Self proxy
     */
    private IDatasetCrawlerService self;

    /**
     * Once ICrawlerService bean has been initialized, retrieve self proxy to permit transactional call of doPoll.
     */
    @PostConstruct
    private void init() {
        self = applicationContext.getBean(IDatasetCrawlerService.class);
    }

    @Override
    @Async
    public void crawl() {
        super.crawl(self::doPoll);
    }

}
