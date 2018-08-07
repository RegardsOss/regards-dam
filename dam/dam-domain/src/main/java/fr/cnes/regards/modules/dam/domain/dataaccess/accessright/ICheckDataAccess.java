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
package fr.cnes.regards.modules.dam.domain.dataaccess.accessright;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.User;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.indexer.domain.DataFile;

/**
 * Plugin used to check if a {@link DataFile} from a {@link Dataset} is accessible, or not, for an {@link AccessGroup} or a
 * {@link User}
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@PluginInterface(
        description = "plugin used to check if a data from a dataset is accessible, or not, for an access group or a user")
public interface ICheckDataAccess extends IIdentifiable<Long> {

    boolean isAccessible(DataFile pData);

}
