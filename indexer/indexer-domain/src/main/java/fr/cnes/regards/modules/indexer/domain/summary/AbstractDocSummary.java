package fr.cnes.regards.modules.indexer.domain.summary;

/**
 * @author oroussel
 */
public abstract class AbstractDocSummary extends FilesSummary {

    protected long documentsCount = 0;

    protected AbstractDocSummary() {
    }

    public long getDocumentsCount() {
        return documentsCount;
    }

    public void addDocumentsCount(long documentsCount) {
        this.documentsCount += documentsCount;
    }
}
