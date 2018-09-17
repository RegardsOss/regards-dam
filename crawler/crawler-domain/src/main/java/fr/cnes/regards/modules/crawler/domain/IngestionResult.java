package fr.cnes.regards.modules.crawler.domain;

import java.time.OffsetDateTime;

/**
 * Ingestion result summary
 * @author oroussel
 */
public class IngestionResult {

    private OffsetDateTime date;

    private int savedObjectsCount = 0;

    private int inErrorObjectsCount = 0;

    public IngestionResult(OffsetDateTime date, int saveObjectsCount, int inErrorObjectsCount) {
        super();
        this.date = date;
        this.savedObjectsCount = saveObjectsCount;
        this.inErrorObjectsCount = inErrorObjectsCount;
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

    public int getInErrorObjectsCount() {
        return inErrorObjectsCount;
    }
}
