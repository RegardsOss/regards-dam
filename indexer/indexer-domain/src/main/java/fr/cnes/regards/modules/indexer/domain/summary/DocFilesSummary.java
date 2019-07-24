/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
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

    public DocFilesSummary() {
    }

    public DocFilesSummary(long documentsCount, long filesCount, long filesSize) {
        super(filesCount, filesSize, documentsCount);
    }

    public Map<String, DocFilesSubSummary> getSubSummariesMap() {
        return subSummariesMap;
    }

    @Override
    public String toString() {
        return "DocFilesSummary{" + "subSummariesMap=" + subSummariesMap + ", documentsCount=" + documentsCount
                + ", filesCount=" + filesCount + ", filesSize=" + filesSize + '}';
    }
}
