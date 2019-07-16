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
package fr.cnes.regards.modules.dam.plugin.dataaccess.accessright;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.plugins.IDataObjectAccessFilterPlugin;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.IOpenSearchService;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * Plugin to allow access to dataobjects with an opensearch request.
 * @author Sébastien Binda
 */
@Plugin(id = "CustomDataObjectsAccessPlugin", version = "4.0.0-SNAPSHOT",
        description = "Allow access to dataObjects matching the given opensearch lucene formated query.",
        markdown = "CustomDataObjectsAccessPlugin.md",
        author = "REGARDS Team", contact = "regards@c-s.fr", license = "GPLv3", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class CustomDataObjectsAccessPlugin implements IDataObjectAccessFilterPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDataObjectsAccessPlugin.class);

    public static final String OPEN_SEARCH_FILTER = "openSearchFilter";

    @Autowired
    IOpenSearchService openSearchService;

    @PluginParameter(label = OPEN_SEARCH_FILTER)
    private String openSearchFilter;

    @Override
    public ICriterion getSearchFilter() {
        try {
            return openSearchService.parse(openSearchFilter);
        } catch (OpenSearchParseException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

}
