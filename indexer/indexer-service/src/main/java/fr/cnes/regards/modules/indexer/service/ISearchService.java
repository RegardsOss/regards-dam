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
package fr.cnes.regards.modules.indexer.service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.dao.FacetPage;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.IDocFiles;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.JoinEntitySearchKey;
import fr.cnes.regards.modules.indexer.domain.SearchKey;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.facet.FacetType;
import fr.cnes.regards.modules.indexer.domain.summary.DocFilesSummary;

/**
 * Elasticsearch search service. This service contains all search and get methods. For other methods, check crawler-service and IndexerService class.
 * @author oroussel
 */
public interface ISearchService {

    /**
     * Maximum page size (Elasticsearch constraint)
     */
    int MAX_PAGE_SIZE = IEsRepository.MAX_RESULT_WINDOW;

    <T extends IIndexable> T get(UniformResourceName urn);

    /**
     * Search ordered documents into index following criterion. Some facets are asked for.
     * @param searchKey identity search key
     * @param pageRequest pagination information ({@link PageRequest}
     * @param criterion search criterion
     * @param facetsMap a map of { document property name, facet type }
     * @return a simple page of documents if facet are not asked for, a {@link FacetPage} else
     */
    <T extends IIndexable> FacetPage<T> search(SimpleSearchKey<T> searchKey, Pageable pageRequest, ICriterion criterion,
            Map<String, FacetType> facetsMap);

    /**
     * Search documents as usual BUT return joined entity whom type is specified into searchKey
     * @param searchKey the search key. <b>Be careful, the search type must be the type concerned by criterion, result class must be joined entity class </b>
     * @param pageRequest pagination information ({@link PageRequest}
     * @param criterion search criterion on document
     * @param <S> entity class on which request is done
     * @param <R> Joined entity class ("result" type)
     * @return a page of joined entities
     */
    default <S, R extends IIndexable> FacetPage<R> search(JoinEntitySearchKey<S, R> searchKey, Pageable pageRequest,
            ICriterion criterion) {
        return search(searchKey, pageRequest, criterion, null);
    }

    /**
     * Search documents as usual BUT return joined entity whom type is specified into searchKey
     * @param searchKey the search key. <b>Be careful, the search type must be the type concerned by criterion, result class must be joined entity class </b>
     * @param pageRequest pagination information ({@link PageRequest}
     * @param criterion search criterion on document
     * @param searchResultFilter a result filter to be used before result pagination. Can be null.
     * @param <S> entity class on which request is done
     * @param <R> Joined entity class ("result" type)
     * @return a page of joined entities
     */
    <S, R extends IIndexable> FacetPage<R> search(JoinEntitySearchKey<S, R> searchKey, Pageable pageRequest,
            ICriterion criterion, Predicate<R> searchResultFilter);

    /**
     * Searching specified page of elements from index giving page size
     * @param searchKey the search key
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param pValue value to search
     * @param pFields fields to search on (use '.' for inner objects, ie "attributes.tags"). Wildcards '*' can be used
     * too (ie attributes.dataRange.*). <b>Fields types must be consistent with given value type</b>
     * @param <T> document type
     * @return specified result page
     */
    <T> Page<T> multiFieldsSearch(SearchKey<T, T> searchKey, Pageable pPageRequest, Object pValue, String... pFields);

    default <T> Page<T> multiFieldsSearch(final SearchKey<T, T> searchKey, final int pageSize, final Object pValue,
            final String... pFields) {
        return multiFieldsSearch(searchKey, new PageRequest(0, pageSize), pValue, pFields);
    }

    default <S, R extends IIndexable> Page<R> search(final JoinEntitySearchKey<S, R> searchKey, final int pageSize,
            final ICriterion pCriterion) {
        return this.search(searchKey, new PageRequest(0, pageSize), pCriterion);
    }

    default <T extends IIndexable> Page<T> search(final SimpleSearchKey<T> searchKey, final int pPageSize,
            final ICriterion criterion) {
        return search(searchKey, new PageRequest(0, pPageSize), criterion);
    }

    default <T extends IIndexable> Page<T> search(final SimpleSearchKey<T> searchKey, final Pageable pPageRequest,
            final ICriterion criterion) {
        return search(searchKey, pPageRequest, criterion, null);
    }

    /**
     * Compute a DocFilesSummary for given request distributing results based on disciminantProperty for given file
     * types
     * @param <T> document type (must be of type IIndexable to be searched and IDocFiles to provide "files" property)
     * @return the compmuted summary
     */
    <T extends IIndexable & IDocFiles> DocFilesSummary computeDataFilesSummary(SearchKey<T, T> searchKey,
            ICriterion crit, String discriminantProperty, String... fileTypes);

    /**
     * Search for alphabeticly sorted top maxCount values of given attribute following given request
     */
    <T extends IIndexable> List<String> searchUniqueTopValues(SearchKey<T, T> searchKey, ICriterion crit,
            String attName, int maxCount);
}
