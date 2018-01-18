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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.Document;
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

@Service
public class SearchService implements ISearchService {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private IEsRepository repository;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IIndexable> T get(final UniformResourceName urn) {
        Class<? extends IIndexable> clazz;
        switch (urn.getEntityType()) {
            case COLLECTION:
                clazz = fr.cnes.regards.modules.entities.domain.Collection.class;
                break;
            case DATA:
                clazz = DataObject.class;
                break;
            case DATASET:
                clazz = Dataset.class;
                break;
            case DOCUMENT:
                clazz = Document.class;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return (T) repository.get(urn.getTenant(), urn.getEntityType().toString(), urn.toString(), clazz);
    }

    @Override
    public <T extends IIndexable> FacetPage<T> search(final SimpleSearchKey<T> searchKey, final Pageable pPageRequest,
            final ICriterion pCriterion, final Map<String, FacetType> pFacetsMap) {
        return repository.search(searchKey, pPageRequest, pCriterion, pFacetsMap);
    }

    @Override
    public <S, T extends IIndexable> FacetPage<T> search(final JoinEntitySearchKey<S, T> searchKey,
            final Pageable pageRequest, final ICriterion pCriterion, Predicate<T> searchResultFilter) {

        // Create a new SearchKey to search on asked type but to only retrieve tags of found results
        final SearchKey<S, String[]> tagSearchKey = new SearchKey<>(searchKey.getSearchIndex(),
                searchKey.getSearchTypeMap(), String[].class);
        // Predicate to filter each tag : it must be a valid URN and this URN must concern wanted result type
        final Predicate<String> askedTypePredicate = tag -> UniformResourceName.isValidUrn(tag) && (Searches.TYPE_MAP
                .get(UniformResourceName.fromString(tag).getEntityType()) == searchKey.getResultClass());
        // Function to get Entity from its ipId (URN) (from Elasticsearch)
        final Function<String, T> toAskedEntityFct = tag -> repository
                .get(searchKey.getSearchIndex(), Searches.TYPE_MAP.inverse().get(searchKey.getResultClass()).toString(),
                     tag, searchKey.getResultClass());
        List<T> objects = repository.search(tagSearchKey, pCriterion, "tags", askedTypePredicate, toAskedEntityFct);
        if (searchResultFilter != null) {
            objects = objects.stream().filter(searchResultFilter).collect(Collectors.toList());
        }
        final int total = objects.size();
        if (!objects.isEmpty()) {
            objects = objects.subList(pageRequest.getOffset(),
                                      Math.min(pageRequest.getOffset() + pageRequest.getPageSize(), objects.size()));
        }
        return new FacetPage<>(objects, new HashSet<>(), pageRequest, total);
    }

    @Override
    public <T> Page<T> multiFieldsSearch(final SearchKey<T, T> pSearchKey, final Pageable pPageRequest,
            final Object pValue, final String... pFields) {
        return repository.multiFieldsSearch(pSearchKey, pPageRequest, pValue, pFields);
    }

    @Override
    public <T extends IIndexable & IDocFiles> DocFilesSummary computeDataFilesSummary(SearchKey<T, T> searchKey,
            ICriterion crit, String discriminantProperty, String... fileTypes) {
        return repository.computeDataFilesSummary(searchKey, crit, discriminantProperty, fileTypes);
    }
}
