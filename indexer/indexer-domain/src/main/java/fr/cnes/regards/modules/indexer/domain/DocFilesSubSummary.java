package fr.cnes.regards.modules.indexer.domain;

/**
 * See {@link DocFilesSummary}
 * @author oroussel
 */
public class DocFilesSubSummary {
    private long documentsCount;

    private long filesCount;

    private long filesSize;

    public long getDocumentsCount() {
        return documentsCount;
    }

    public void setDocumentsCount(long documentsCount) {
        this.documentsCount = documentsCount;
    }

    public long getFilesCount() {
        return filesCount;
    }

    public void setFilesCount(long filesCount) {
        this.filesCount = filesCount;
    }

    public long getFilesSize() {
        return filesSize;
    }

    public void setFilesSize(long filesSize) {
        this.filesSize = filesSize;
    }

    @Override
    public String toString() {
        return "DocFilesSubSummary{" + "documentsCount=" + documentsCount + ", filesCount=" + filesCount
                + ", filesSize=" + filesSize + '}';
    }
}
