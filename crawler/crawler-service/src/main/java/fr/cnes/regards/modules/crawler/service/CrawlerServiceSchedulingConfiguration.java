/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduling configuration
 *
 * @author Marc Sordi
 *
 */
@Configuration
@EnableScheduling
@Profile("!noschedule")
public class CrawlerServiceSchedulingConfiguration {

}
