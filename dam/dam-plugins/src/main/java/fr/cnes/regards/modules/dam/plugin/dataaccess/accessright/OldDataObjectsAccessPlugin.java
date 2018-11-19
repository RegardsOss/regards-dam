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

import java.time.OffsetDateTime;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.plugins.IDataObjectAccessFilterPlugin;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * Plugin to allow access to old dataobjects.
 * @author Sébastien Binda
 */
@Plugin(id = "OldDataObjectsAccess", version = "4.0.0-SNAPSHOT",
        description = "Allow access only to old data objects. Old data objects are thoses created at least X days ago. X is a parameter to configure.",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class OldDataObjectsAccessPlugin implements IDataObjectAccessFilterPlugin {

    public static final String NB_DAYS_PARAM = "numberOfDays";

    @PluginParameter(label = NB_DAYS_PARAM, description = "Number of days")
    private long numberOfDays;

    @Override
    public ICriterion getSearchFilter() {
        OffsetDateTime time = OffsetDateTime.now().minusDays(numberOfDays);
        return ICriterion.or(ICriterion.lt("creationDate", time), ICriterion.eq("creationDate", time));
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

}
