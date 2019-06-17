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
package fr.cnes.regards.modules.crawler.service.exception;

import fr.cnes.regards.modules.indexer.dao.BulkSaveLightResult;

/**
 * Exception used to manage a problem with datasource plugin or Elasticsearch during an ingestion by permitting
 * throwing some useful data upper.
 * @author Olivier Rousselot
 */
public class NotFinishedException extends Exception {

    private final BulkSaveLightResult saveResult;

    private final int pageNumber;

    public NotFinishedException(Throwable cause, BulkSaveLightResult saveResult, int pageNumber) {
        super(cause);
        this.saveResult = saveResult;
        this.pageNumber = pageNumber;
    }

    public BulkSaveLightResult getSaveResult() {
        return saveResult;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
