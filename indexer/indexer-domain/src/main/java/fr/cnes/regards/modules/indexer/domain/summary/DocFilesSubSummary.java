package fr.cnes.regards.modules.indexer.domain.summary;

import java.util.HashMap;
import java.util.Map;

/**
 * See {@link DocFilesSummary}
 * @author oroussel
 */
public class DocFilesSubSummary extends AbstractDocSummary {

    private Map<String, FilesSummary> fileTypesSummaryMap = new HashMap<>();

    public Map<String, FilesSummary> getFileTypesSummaryMap() {
        return fileTypesSummaryMap;
    }

    public DocFilesSubSummary() {

    }

    public DocFilesSubSummary(long documentsCount, long filesCount, long filesSize) {
        super(documentsCount, filesCount, filesSize);
    }

    @Override
    public String toString() {
        return "DocFilesSubSummary{" + "fileTypesSummaryMap=" + fileTypesSummaryMap + ", documentsCount="
                + documentsCount + ", filesCount=" + filesCount + ", filesSize=" + filesSize + '}';
    }
}
