/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.notifier.service.plugin;

import java.util.Set;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.notification.domain.plugin.IRuleMatcher;

/**
 * @author kevin
 *
 */
@Plugin(author = "REGARDS Team", description = "Default rule matcher for feature", id = "DefaultRuleMatcher",
        version = "1.0.0", contact = "regards@c-s.fr", license = "GPLv3", owner = "CNES",
        url = "https://regardsoss.github.io/")
public class DefaultRuleMatcher implements IRuleMatcher {

    /**
     * Attribute name to seek in properties
     */
    @PluginParameter(label = "attributeToSeek")
    private String attributeToSeek;

    /**
     * Attribute value to seek in properties
     */
    @PluginParameter(label = "attributeValueToSeek")
    private String attributeValueToSeek;

    @Override
    public boolean match(Feature feature) {

        return handleProperties(feature.getProperties());

    }

    /**
     * Browse a list of properties to find the one with the name of the class attribute 'attributeToSeek'
     * and the value 'attributeValueToSeek'
     * @param properties
     */
    private boolean handleProperties(Set<IProperty<?>> properties) {
        boolean match = false;
        if (properties == null) {
            return false;
        }
        for (IProperty<?> property : properties) {
            if (property.getValue() instanceof Set) {
                match = handleProperties((Set<IProperty<?>>) property.getValue());
            } else {
                if (property.getName().equals(attributeToSeek)) {
                    return property.getValue().equals(attributeValueToSeek);
                }
            }
        }
        return match;
    }

}
