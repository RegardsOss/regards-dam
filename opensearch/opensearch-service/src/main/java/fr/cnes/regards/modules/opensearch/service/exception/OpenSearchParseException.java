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
package fr.cnes.regards.modules.opensearch.service.exception;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;

/**
 * Thrown when an error occurs during the parsing of an OpenSearch request.
 * @author Xavier-Alexandre Brochard
 */
@SuppressWarnings("serial")
public class OpenSearchParseException extends ModuleException { // NOSONAR

    /**
     * @param pMessage the message
     */
    public OpenSearchParseException(String pMessage) {
        super(pMessage);
    }

    /**
     * @param pCause the caught exception which triggered this exception
     */
    public OpenSearchParseException(Throwable pCause) {
        super("An error occured while parsing the OpenSearch request", pCause);
    }

    /**
     * @param pMessage the message
     * @param pCause the caught exception which triggered this exception
     */
    public OpenSearchParseException(String pMessage, Throwable pCause) {
        super(pMessage, pCause);
    }

}
