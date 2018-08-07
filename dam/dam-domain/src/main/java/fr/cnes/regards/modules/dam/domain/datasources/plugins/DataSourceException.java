package fr.cnes.regards.modules.dam.domain.datasources.plugins;

/**
 * Generic datasource exception used to rethrow an internal datasource ingestion exception.
 * This exception will be catched by crawler service to be logged onto database.
 * @author oroussel
 */
@SuppressWarnings("serial")
public class DataSourceException extends Exception {

    public DataSourceException() {
    }

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceException(Throwable cause) {
        super(cause);
    }
}
