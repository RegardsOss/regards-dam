package fr.cnes.regards.modules.crawler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Crawler initializer.
 * This component is used to launch crawler service as a daemon.
 */
@Component
public class CrawlerInitializer {

    @Autowired
    private ICrawlerService crawlerService;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        crawlerService.crawl();
    }
}
