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
package fr.cnes.regards.modules.entities.service.exception;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class EntityDescriptionTooLargeException extends ModuleException {

    private static final String DESCRIPTION_TOO_LARGE = "The system only accept description up to 10MB. %s do not respect this constraint.";

    /**
     * @param pDescriptionName
     */
    public EntityDescriptionTooLargeException(String pDescriptionName) {
        super(String.format(DESCRIPTION_TOO_LARGE, pDescriptionName));
    }

}
