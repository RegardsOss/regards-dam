package fr.cnes.regards.modules.crawler.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;

import fr.cnes.regards.modules.dam.service.models.event.ComputedAttributeModelEvent;

/**
 * @author oroussel
 */
public interface IDatasetCrawlerService extends ICrawlerService {
    void onApplicationReadyEvent(ApplicationReadyEvent event);

    void onComputedAttributeModelEvent(ComputedAttributeModelEvent event);
}
