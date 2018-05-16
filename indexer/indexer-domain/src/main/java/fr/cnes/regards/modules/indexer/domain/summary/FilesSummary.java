package fr.cnes.regards.modules.indexer.domain.summary;

/**
 * A simple POJO that contains filesCount and filesSize properties
 * @author oroussel
 */
public class FilesSummary {
    protected long filesCount;

    protected long filesSize;

    public FilesSummary() {
    }

    public long getFilesCount() {
        return filesCount;
    }

    public void addFilesCount(long filesCount) {
        this.filesCount += filesCount;
    }

    public long getFilesSize() {
        return filesSize;
    }


    public void addFilesSize(long filesSize) {
        this.filesSize += filesSize;
    }

    @Override
    public String toString() {
        return "FilesSummary{" + "filesCount=" + filesCount + ", filesSize=" + filesSize + '}';
    }
}
