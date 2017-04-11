package fr.cnes.regards.modules.crawler.domain;

import java.time.LocalDateTime;

/**
 * Ingestion result summary
 * @author oroussel
 */
public class IngestionResult {

    private LocalDateTime date;

    private int savedObjectsCount = 0;

    public IngestionResult(LocalDateTime pDate, int pSaveObjectsCount) {
        super();
        date = pDate;
        savedObjectsCount = pSaveObjectsCount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime pDate) {
        date = pDate;
    }

    public int getSavedObjectsCount() {
        return savedObjectsCount;
    }

    public void setSavedObjectsCount(int pSaveObjectsCount) {
        savedObjectsCount = pSaveObjectsCount;
    }

}
