/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.service.models.xml;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.dam.service.models.exception.ImportException;
import fr.cnes.regards.modules.model.domain.schema.Attribute;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 *
 * Delegate plugin configuration initialization to module implementing computation plugins related to model configuration
 * @author Marc SORDI
 */
public interface IComputationPluginService {

    /**
     * Initialize a plugin configuration according to XML imported configuration
     * @return {@link PluginConfiguration}
     * @throws ImportException if plugin cannot be initialized
     */
    PluginConfiguration getPlugin(Attribute xmlAtt, PropertyType type) throws ImportException;
}
