package fr.cnes.regards.modules.crawler.service;

import fr.cnes.regards.modules.model.gson.ModelGsonReadyEvent;
import fr.cnes.regards.modules.model.service.event.ComputedAttributeModelEvent;

/**
 * @author oroussel
 */
public interface IDatasetCrawlerService extends ICrawlerService {

    void onApplicationReadyEvent(ModelGsonReadyEvent event);

    void onComputedAttributeModelEvent(ComputedAttributeModelEvent event);
}
