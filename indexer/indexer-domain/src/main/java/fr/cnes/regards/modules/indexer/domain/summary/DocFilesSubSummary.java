/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * See {@link DocFilesSummary}
 * @author oroussel
 */
public class DocFilesSubSummary extends AbstractDocSummary {

    private final ConcurrentHashMap<String, FilesSummary> fileTypesSummaryMap = new ConcurrentHashMap<>();

    public DocFilesSubSummary(long documentsCount, long filesCount, long filesSize, String... fileTypes) {
        super(filesCount, filesSize, documentsCount);
        for (String fileType : fileTypes) {
            fileTypesSummaryMap.put(fileType, new FilesSummary());
        }
    }

    /**
     * Constructor
     * @param fileTypes to initialise map
     */
    public DocFilesSubSummary(String... fileTypes) {
        for (String fileType : fileTypes) {
            fileTypesSummaryMap.put(fileType, new FilesSummary());
        }
    }

    public Map<String, FilesSummary> getFileTypesSummaryMap() {
        return fileTypesSummaryMap;
    }

    @Override
    public String toString() {
        return "DocFilesSubSummary{" + "fileTypesSummaryMap=" + fileTypesSummaryMap + ", documentsCount="
                + documentsCount + ", filesCount=" + filesCount + ", filesSize=" + filesSize + '}';
    }
}
