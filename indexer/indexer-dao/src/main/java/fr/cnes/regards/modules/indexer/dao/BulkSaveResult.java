/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.indexer.dao;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Object permitting to know what IEsRepository.saveBulk() method has really done.
 * @author oroussel
 */
public class BulkSaveResult {
    private Set<String> savedDocIds = ConcurrentHashMap.newKeySet();

    private Map<String, Exception> inErrorDocsMap = new ConcurrentHashMap<>();

    /**
     * Error detailed message is set by EsRespository when notify to admin. It is then used to show error message while
     * ingesting a datasource
     */
    private String detailedErrorMsg;

    public BulkSaveResult() {
    }

    public void addSavedDocId(String docId) {
        savedDocIds.add(docId);
    }

    public void addInErrorDoc(String docId, Exception e) {
        inErrorDocsMap.put(docId, e);
    }

    public int getSavedDocsCount() {
        return savedDocIds.size();
    }

    public int getInErrorDocsCount() {
        return inErrorDocsMap.size();
    }

    public Stream<String> getSavedDocIdsStream() {
        return savedDocIds.stream();
    }

    public Stream<String> getInErrorDocIdsStream() {
        return inErrorDocsMap.keySet().stream();
    }

    public Exception getInErrorDocCause(String docId) {
        return inErrorDocsMap.get(docId);
    }

    public String getDetailedErrorMsg() {
        return detailedErrorMsg;
    }

    public void setDetailedErrorMsg(String detailedErrorMsg) {
        this.detailedErrorMsg = detailedErrorMsg;
    }

    /**
     * Append another bulk save result
     * @param otherBulkSaveResult another bulk save result
     * @return this
     */
    public BulkSaveResult append(BulkSaveResult otherBulkSaveResult) {
        this.savedDocIds.addAll(otherBulkSaveResult.savedDocIds);
        this.inErrorDocsMap.putAll(otherBulkSaveResult.inErrorDocsMap);
        return this;
    }
}
