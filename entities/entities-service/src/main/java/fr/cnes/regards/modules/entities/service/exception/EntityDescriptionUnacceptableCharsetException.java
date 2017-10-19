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
package fr.cnes.regards.modules.entities.service.exception;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class EntityDescriptionUnacceptableCharsetException extends ModuleException {

    private static final String MESSAGE_FORMAT = "Entity can only have description of Type: application/pdf or text/markdown;charset=utf-8 and not charset=%s";

    /**
     * @param pCharsetOfFile
     */
    public EntityDescriptionUnacceptableCharsetException(String pCharsetOfFile) {
        super(String.format(MESSAGE_FORMAT, pCharsetOfFile));
    }

}
