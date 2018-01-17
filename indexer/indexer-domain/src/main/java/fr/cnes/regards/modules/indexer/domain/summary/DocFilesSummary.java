package fr.cnes.regards.modules.indexer.domain.summary;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a summary of docs count, files count and files size sum computed from an opensearch on documents of type
 * DocFiles (ie with "files" property). These properties are computed for each discriminant property (ie "tags" for
 * example) and for total
 * @author oroussel
 */
public class DocFilesSummary extends AbstractDocSummary {

    /**
     * Map of sub-summaries distributed by discriminant value
     */
    private final Map<String, DocFilesSubSummary> subSummariesMap = new HashMap<>();

    public Map<String, DocFilesSubSummary> getSubSummariesMap() {
        return subSummariesMap;
    }

    public DocFilesSummary() {

    }

    public DocFilesSummary(long documentsCount, long filesCount, long filesSize) {
        super(documentsCount, filesCount, filesSize);
    }

    @Override
    public String toString() {
        return "DocFilesSummary{" + "subSummariesMap=" + subSummariesMap + ", documentsCount=" + documentsCount
                + ", filesCount=" + filesCount + ", filesSize=" + filesSize + '}';
    }
}
