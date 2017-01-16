package fr.cnes.regards.modules.crawler.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "fr.cnes.regards.modules.entities.domain", "fr.cnes.regards.modules.models.domain",
        "fr.cnes.regards.modules.crawler", "fr.cnes.regards.framework.gson" })
public class CrawlerConfiguration {

}
