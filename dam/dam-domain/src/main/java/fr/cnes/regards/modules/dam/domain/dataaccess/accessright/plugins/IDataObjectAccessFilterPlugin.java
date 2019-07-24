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
package fr.cnes.regards.modules.dam.domain.dataaccess.accessright.plugins;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

@PluginInterface(description = "Plugin used to filter access of data objects in a given dataset")
public interface IDataObjectAccessFilterPlugin {

    /**
     * Get the {@link ICriterion} search filters to select accessible dataObjects of the current dataset.
     * @return {@link ICriterion}
     */
    ICriterion getSearchFilter();

    /**
     * Dynamic IDataObjectAccessFilterPlugin are used to recalculated dataObjects groups every day.
     * A dynamic plugin is a plugin that can return different criterion without any changes in his parameters.
     * @return boolean
     */
    boolean isDynamic();
}
