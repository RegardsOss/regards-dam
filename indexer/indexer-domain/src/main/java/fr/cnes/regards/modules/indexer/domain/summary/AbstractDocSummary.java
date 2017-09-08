package fr.cnes.regards.modules.indexer.domain.summary;

/**
 * @author oroussel
 */
public abstract class AbstractDocSummary extends FilesSummary {

    protected long documentsCount = 0;

    protected AbstractDocSummary() {

    }

    protected AbstractDocSummary(long documentsCount, long filesCount, long filesSize) {
        super(filesCount, filesSize);
        this.documentsCount = documentsCount;
    }

    public long getDocumentsCount() {
        return documentsCount;
    }

    public void setDocumentsCount(long documentsCount) {
        this.documentsCount = documentsCount;
    }
}