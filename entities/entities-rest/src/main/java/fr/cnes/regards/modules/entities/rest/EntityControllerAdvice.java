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
package fr.cnes.regards.modules.entities.rest;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fr.cnes.regards.framework.module.rest.representation.ServerErrorResponse;
import fr.cnes.regards.modules.entities.service.exception.EntityDescriptionTooLargeException;
import fr.cnes.regards.modules.entities.service.exception.EntityDescriptionUnacceptableCharsetException;
import fr.cnes.regards.modules.entities.service.exception.EntityDescriptionUnacceptableType;

/**
 *
 * Advice for specific entity exceptions
 * @author Marc Sordi
 *
 */
@RestControllerAdvice(annotations = RestController.class)
@Order(0)
public class EntityControllerAdvice {

    @ExceptionHandler(EntityDescriptionUnacceptableCharsetException.class)
    public ResponseEntity<ServerErrorResponse> entityDescriptionUnacceptableCharset(
            final EntityDescriptionUnacceptableCharsetException pException) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ServerErrorResponse(pException.getMessage()));
    }

    @ExceptionHandler(EntityDescriptionUnacceptableType.class)
    public ResponseEntity<ServerErrorResponse> entityDescriptionUnacceptableType(
            final EntityDescriptionUnacceptableType pException) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ServerErrorResponse(pException.getMessage()));
    }

    @ExceptionHandler(EntityDescriptionTooLargeException.class)
    public ResponseEntity<ServerErrorResponse> entityDescriptionTooLargeCharset(
            final EntityDescriptionTooLargeException pException) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ServerErrorResponse(pException.getMessage()));
    }
}