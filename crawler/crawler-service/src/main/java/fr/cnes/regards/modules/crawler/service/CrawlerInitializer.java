package fr.cnes.regards.modules.crawler.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Crawler initializer.
 * This component is used to launch crawler service as a daemon.
 */
@Component
public class CrawlerInitializer {

    @Autowired
    private ICrawlerService crawlerService;

    @PostConstruct
    public void launchCrawler() {
        crawlerService.crawl();
    }
}
