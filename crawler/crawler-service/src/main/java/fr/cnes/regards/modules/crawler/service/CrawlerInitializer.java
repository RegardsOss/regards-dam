package fr.cnes.regards.modules.crawler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Crawler initializer.
 * This component is used to launch crawler services and ingester service as daemons.
 * @author oroussel
 */
@Component
public class CrawlerInitializer {

    @Autowired
    private ICrawlerService datasetCrawlerService;

    @Autowired
    private ICrawlerAndIngesterService crawlerAndIngesterService;

    @Autowired
    private IIngesterService ingesterService;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        datasetCrawlerService.crawl();
    }

    @EventListener
    public void onApplicationEvent2(ContextRefreshedEvent event) {
        crawlerAndIngesterService.crawl();
    }


    @EventListener
    public void onApplicationEvent3(ContextRefreshedEvent event) {
        ingesterService.listenToPluginConfChange();
    }

}
