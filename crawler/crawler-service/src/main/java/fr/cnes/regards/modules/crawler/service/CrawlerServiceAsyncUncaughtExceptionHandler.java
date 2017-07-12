/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
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
