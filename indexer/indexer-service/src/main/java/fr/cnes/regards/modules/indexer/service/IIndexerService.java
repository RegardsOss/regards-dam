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
package fr.cnes.regards.modules.indexer.service;

import java.util.Collection;

import fr.cnes.regards.modules.indexer.dao.BulkSaveResult;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.IIndexable;

/**
 * Indexer interface
 * @author oroussel
 * @deprecated only used by dam and catalog tests
 */
@Deprecated
public interface IIndexerService {

    int BULK_SIZE = IEsRepository.BULK_SIZE;

    /**
     * Create index if not already exists
     * @param index index name
     * @return true if index exists after method returns, false overwise
     */
    boolean createIndex(String index);

    /**
     * Delete index if index exists
     * @param index index name
     * @return true if index doesn't exist after method returns
     */
    boolean deleteIndex(String index);

    boolean indexExists(String index);

    boolean saveEntity(String index, IIndexable entity);

    /**
     * Method only used for tests. Elasticsearch performs refreshes every second. So, il a search is called just after
     * a save, the document will not be available. A manual refresh is necessary (on saveBulkEntities, it is
     * automaticaly called)
     * @param index index to refresh
     */
    void refresh(String index);

    BulkSaveResult saveBulkEntities(String index, IIndexable... entities);

    BulkSaveResult saveBulkEntities(String index, Collection<? extends IIndexable> entities);

    boolean deleteEntity(String index, IIndexable entity);
}
