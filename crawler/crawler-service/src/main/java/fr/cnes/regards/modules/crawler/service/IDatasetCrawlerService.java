package fr.cnes.regards.modules.crawler.service;

import fr.cnes.regards.modules.dam.gson.entities.DamGsonReadyEvent;
import fr.cnes.regards.modules.dam.service.models.event.ComputedAttributeModelEvent;

/**
 * @author oroussel
 */
public interface IDatasetCrawlerService extends ICrawlerService {
    void onApplicationReadyEvent(ApplicationReadyEvent event);

    void onApplicationReadyEvent(DamGsonReadyEvent event);

    void onComputedAttributeModelEvent(ComputedAttributeModelEvent event);
}
