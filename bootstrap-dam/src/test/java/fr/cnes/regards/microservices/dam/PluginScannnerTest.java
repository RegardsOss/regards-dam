/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.microservices.dam;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;

/**
 * Performance testing for plugin detection by introspection
 *
 * @author Marc Sordi
 *
 */
public class PluginScannnerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginScannnerTest.class);

    @Test
    public void singleScan() {

        long startTime = System.currentTimeMillis();
        // Map<String, PluginMetaData> map = PluginUtils.getPlugins("fr.cnes.regards", null);
        Reflections reflections = new Reflections("fr.cnes.regards");
        Set<Class<?>> annotatedPlugins = reflections.getTypesAnnotatedWith(Plugin.class, true);
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        LOGGER.info("Time elapsed : {}", elapsedTime);
        Assert.assertNotNull(annotatedPlugins);
    }

}
