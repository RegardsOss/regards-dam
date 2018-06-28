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
package fr.cnes.regards.modules.entities.rest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fr.cnes.regards.framework.module.rest.representation.ServerErrorResponse;
import fr.cnes.regards.modules.entities.rest.exception.AssociatedAccessRightExistsException;
import fr.cnes.regards.modules.entities.service.exception.InvalidCharsetException;
import fr.cnes.regards.modules.entities.service.exception.InvalidContentTypeException;

/**
 * Advice for specific entity exceptions
 * @author Marc Sordi
 */
@RestControllerAdvice(annotations = RestController.class)
@Order(Ordered.LOWEST_PRECEDENCE - 300)
public class EntityControllerAdvice {

    @ExceptionHandler(InvalidCharsetException.class)
    public ResponseEntity<ServerErrorResponse> invalidCharsetException(InvalidCharsetException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ServerErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(InvalidContentTypeException.class)
    public ResponseEntity<ServerErrorResponse> invalidContentTypeException(InvalidContentTypeException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ServerErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(AssociatedAccessRightExistsException.class)
    public ResponseEntity<ServerErrorResponse> handleAssociatedAccessRightExistsException(
            AssociatedAccessRightExistsException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ServerErrorResponse(e.getMessage()));
    }

}
