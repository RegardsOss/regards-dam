/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fr.cnes.regards.modules.models.domain.exception.AlreadyExistsException;
import fr.cnes.regards.modules.models.domain.exception.ModelException;
import fr.cnes.regards.modules.models.rest.representation.ServerErrorResponse;

/**
 *
 * Manage exception handling
 *
 * @author Marc Sordi
 *
 */
@RestControllerAdvice(annotations = RestController.class)
public class ModelAdvice {

    @ExceptionHandler(ModelException.class)
    public ResponseEntity<ServerErrorResponse> handleModelException(ModelException pEx) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServerErrorResponse("Internal server error"));
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ServerErrorResponse> handleModelException(AlreadyExistsException pEx) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ServerErrorResponse(pEx.getMessage()));
    }
}
