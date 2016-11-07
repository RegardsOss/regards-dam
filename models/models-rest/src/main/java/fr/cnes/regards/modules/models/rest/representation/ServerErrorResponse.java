/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest.representation;

/**
 * Server error response representation
 *
 * @author Marc Sordi
 *
 *         TODO : generalize this representation
 */
public class ServerErrorResponse {

    /**
     * Error message
     */
    private String message;

    public ServerErrorResponse(String pMessage) {
        this.message = pMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String pMessage) {
        message = pMessage;
    }
}
