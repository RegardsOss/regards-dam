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
package fr.cnes.regards.modules.dataaccess.domain.accessright;

/**
 *
 * describe the level of access to the physical data of a datum in a dataset
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public enum DataAccessLevel {
    /**
     * despite the full access on the dataset, we still do no have access to the physical data of the datum(still have
     * access to the meta data of the datum)
     */
    NO_ACCESS,
    /**
     * default level, the access to the physical data is inherited from the access to the meta data of the datum
     */
    INHERITED_ACCESS,
    /**
     * access is defined thanks to a plugin of type {@link ICheckDataAccess}
     */
    CUSTOM_ACCESS
}
