package fr.cnes.regards.modules.crawler.service;

import fr.cnes.regards.modules.dam.service.models.event.ComputedAttributeModelEvent;
import fr.cnes.regards.modules.model.gson.DamGsonReadyEvent;

/**
 * @author oroussel
 */
public interface IDatasetCrawlerService extends ICrawlerService {

    void onApplicationReadyEvent(DamGsonReadyEvent event);

    void onComputedAttributeModelEvent(ComputedAttributeModelEvent event);
}
