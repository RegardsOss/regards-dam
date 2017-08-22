package fr.cnes.regards.modules.indexer.domain.summary;

/**
 * A simple POJO that contains filesCount and filesSize prop)erties
 * @author oroussel
 */
public class FilesSummary {
    protected long filesCount;

    protected long filesSize;

    public FilesSummary() {
    }

    public FilesSummary(long filesCount, long filesSize) {
        this.filesCount = filesCount;
        this.filesSize = filesSize;
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
        return "FilesSummary{" + "filesCount=" + filesCount + ", filesSize=" + filesSize + '}';
    }
}
