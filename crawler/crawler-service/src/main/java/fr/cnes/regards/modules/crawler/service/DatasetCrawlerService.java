package fr.cnes.regards.modules.crawler.service;

import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.entities.domain.event.DatasetEvent;

/**
 * Crawler service for Dataset. <b>This service need @EnableSchedule at Configuration</b>
 * @author oroussel
 */
@Service("datasetCrawlerService")
public class DatasetCrawlerService extends AbstractCrawlerService<DatasetEvent> implements ICrawlerService {

}
