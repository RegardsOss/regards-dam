/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.service;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

/**
 *
 * Asynchronous service exception handling
 * @author Marc Sordi
 *
 */
public class CrawlerServiceAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerServiceAsyncUncaughtExceptionHandler.class);

    private static final String HR = "*****************************************************";

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
        LOGGER.error(HR);
        LOGGER.error("Exception message - " + throwable.getMessage(), throwable);
        LOGGER.error("Method name - " + method.getName());
        for (Object param : params) {
            LOGGER.error("Parameter value - " + param);
        }
        LOGGER.error(HR);

    }

}
