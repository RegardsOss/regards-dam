package fr.cnes.regards.modules.crawler.domain;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Ingestion result summary
 * @author oroussel
 */
public class IngestionResult {

    private OffsetDateTime date;

    private int savedObjectsCount = 0;

    public IngestionResult(OffsetDateTime pDate, int pSaveObjectsCount) {
        super();
        date = pDate;
        savedObjectsCount = pSaveObjectsCount;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime pDate) {
        date = pDate;
    }

    public int getSavedObjectsCount() {
        return savedObjectsCount;
    }

    public void setSavedObjectsCount(int pSaveObjectsCount) {
        savedObjectsCount = pSaveObjectsCount;
    }

}
