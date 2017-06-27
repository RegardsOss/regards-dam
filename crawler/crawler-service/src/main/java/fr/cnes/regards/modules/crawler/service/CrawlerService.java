/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.entities.domain.event.NotDatasetEntityEvent;

/**
 * Crawler service for other entity than Dataset. <b>This service need @EnableSchedule at Configuration</b>
 * This service is the primary to autowire (by IngesterService) in order to ingest datasources
 * @author oroussel
 */
@Service// Transactionnal is handle by hand on the right method, do not specify Multitenant or InstanceTransactionnal
@Primary
public class CrawlerService extends AbstractCrawlerService<NotDatasetEntityEvent> implements ICrawlerService {
}
