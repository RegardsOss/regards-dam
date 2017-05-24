/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.service;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Asynchronous task configuration
 *
 * @author Marc Sordi
 *
 */
@Configuration
@EnableAsync
@Profile("!nocrawl")
public class CrawlerServiceConfiguration implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        return new SimpleAsyncTaskExecutor("crawler-task");
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CrawlerServiceAsyncUncaughtExceptionHandler();
    }
}
