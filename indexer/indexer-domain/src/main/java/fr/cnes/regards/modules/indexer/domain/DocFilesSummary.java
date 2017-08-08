package fr.cnes.regards.modules.indexer.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a summary of docs count, files count and files size sum computed from an opensearch on documents of type
 * DocFiles (ie with "files" property). These properties are computed for each discriminant property (ie "tags" for
 * example) and for total
 * @author oroussel
 */
public class DocFilesSummary {
    private long totalDocumentsCount;

    private long totalFilesCount;

    private long totalFilesSize;

    /**
     * Map of sub-summaries distributed by disciminant value
     */
    private Map<String, DocFilesSubSummary> subSummariesMap = new HashMap<>();

    public long getTotalDocumentsCount() {
        return totalDocumentsCount;
    }

    public void setTotalDocumentsCount(long totalDocumentsCount) {
        this.totalDocumentsCount = totalDocumentsCount;
    }

    public long getTotalFilesCount() {
        return totalFilesCount;
    }

    public void setTotalFilesCount(long totalFilesCount) {
        this.totalFilesCount = totalFilesCount;
    }

    public long getTotalFilesSize() {
        return totalFilesSize;
    }

    public void setTotalFilesSize(long totalFilesSize) {
        this.totalFilesSize = totalFilesSize;
    }

    public Map<String, DocFilesSubSummary> getSubSummariesMap() {
        return subSummariesMap;
    }

    public void setSubSummariesMap(Map<String, DocFilesSubSummary> subSummariesMap) {
        this.subSummariesMap = subSummariesMap;
    }

    @Override
    public String toString() {
        return "DocFilesSummary{" + "totalDocumentsCount=" + totalDocumentsCount + ", totalFilesCount="
                + totalFilesCount + ", totalFilesSize=" + totalFilesSize + ", subSummariesMap=" + subSummariesMap + '}';
    }
}
