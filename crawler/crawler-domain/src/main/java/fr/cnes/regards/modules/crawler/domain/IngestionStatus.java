package fr.cnes.regards.modules.crawler.domain;

/**
 * Ingestion status enumeration
 * @author oroussel
 */
public enum IngestionStatus {
    NEW, // Datasource not yet ingested
    STARTED, // Datasource currently ingested
    FINISHED, // Last ingestion finished without error
    FINISHED_WITH_WARNINGS, // Last ingestion finished with warnings
    ERROR, // Last ingestion finished with errors
    INACTIVE // ingestion is inactive
}
