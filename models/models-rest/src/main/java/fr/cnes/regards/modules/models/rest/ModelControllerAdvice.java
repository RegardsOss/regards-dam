/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fr.cnes.regards.framework.module.rest.representation.ServerErrorResponse;
import fr.cnes.regards.modules.models.service.exception.ImportException;

/**
 * @author Marc Sordi
 *
 */
@RestControllerAdvice(annotations = RestController.class)
@Order(0)
public class ModelControllerAdvice {

    @ExceptionHandler(ImportException.class)
    public ResponseEntity<ServerErrorResponse> importException(final ImportException pException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ServerErrorResponse(pException.getMessage()));
    }
}
