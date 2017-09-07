package fr.cnes.regards.modules.entities.rest.advice;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * Global controller advice
 *
 * @author CS SI
 * @author Marc Sordi
 * @author Sébastien Binda
 * @since 1.1-SNAPSHOT
 */
@RestControllerAdvice(annotations = RestController.class)
@Order(Ordered.LOWEST_PRECEDENCE - 101)
public class DocumentExceptionAdvice  {

    /**
     * Customize response body for spring managed exception
     */

    /*
    @ExceptionHandler(DocumentFilesException.class)
    public ResponseEntity<ServerErrorResponse> handleModelException(final DocumentFilesException pEx) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServerErrorResponse("This is working so far"));
    }


    @ExceptionHandler(DocumentPluginNotFoundException.class)
    public ResponseEntity<ServerErrorResponse> handleModelException(final DocumentPluginNotFoundException pEx) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServerErrorResponse("This endpoint cannot work without an active plugin setup"));
    }

*/
}
