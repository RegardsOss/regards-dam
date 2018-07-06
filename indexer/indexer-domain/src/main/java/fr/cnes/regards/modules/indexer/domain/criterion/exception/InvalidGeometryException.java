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
package fr.cnes.regards.modules.indexer.domain.criterion.exception;

/**
 * Exception thrown when a geometry format is not recognized by criterion builders.
 * @author Sébastien Binda
 */
@SuppressWarnings("serial")
public class InvalidGeometryException extends Exception {

    public InvalidGeometryException() {
        super();
    }

    public InvalidGeometryException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InvalidGeometryException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidGeometryException(String message) {
        super(message);
    }

    public InvalidGeometryException(Throwable cause) {
        super(cause);
    }

}
