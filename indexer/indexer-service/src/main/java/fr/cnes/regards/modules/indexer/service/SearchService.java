/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.DataType;
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
import fr.cnes.regards.modules.indexer.domain.aggregation.QueryableAttribute;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.facet.FacetType;
import fr.cnes.regards.modules.indexer.domain.summary.DocFilesSummary;

@Service
public class SearchService implements ISearchService {

    @Autowired
    private IEsRepository repository;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IIndexable> T get(UniformResourceName urn) {
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
    public <T extends IIndexable> FacetPage<T> search(SimpleSearchKey<T> searchKey, Pageable pageRequest,
            ICriterion criterion, Map<String, FacetType> facetsMap) {
        searchKey.setSearchIndex(tenantResolver.getTenant());
        return repository.search(searchKey, pageRequest, criterion, facetsMap);
    }

    @Override
    public <S, T extends IIndexable> FacetPage<T> search(JoinEntitySearchKey<S, T> searchKey, Pageable pageRequest,
            ICriterion criterion, Predicate<T> searchResultFilter) {
        searchKey.setSearchIndex(tenantResolver.getTenant());
        // Create a new SearchKey to search on asked type but to only retrieve tags of found results
        SearchKey<S, String[]> tagSearchKey = new SearchKey<>(searchKey.getSearchTypeMap(), String[].class);
        tagSearchKey.setSearchIndex(searchKey.getSearchIndex());
        // Predicate to filter each tag : it must be a valid URN and this URN must concern wanted result type
        Predicate<String> askedTypePredicate = tag -> UniformResourceName.isValidUrn(tag) && (Searches.TYPE_MAP
                .get(UniformResourceName.fromString(tag).getEntityType()) == searchKey.getResultClass());
        // Function to get Entity from its ipId (URN) (from Elasticsearch)
        Function<String, T> toAskedEntityFct = tag -> repository
                .get(searchKey.getSearchIndex(), Searches.TYPE_MAP.inverse().get(searchKey.getResultClass()).toString(),
                     tag, searchKey.getResultClass());
        List<T> objects = repository.search(tagSearchKey, criterion, "tags", askedTypePredicate, toAskedEntityFct);
        if (searchResultFilter != null) {
            objects = objects.stream().filter(searchResultFilter).collect(Collectors.toList());
        }
        int total = objects.size();
        if (!objects.isEmpty()) {
            objects = objects.subList(pageRequest.getOffset(),
                                      Math.min(pageRequest.getOffset() + pageRequest.getPageSize(), objects.size()));
        }
        return new FacetPage<>(objects, new HashSet<>(), pageRequest, total);
    }

    @Override
    public <T> Page<T> multiFieldsSearch(SearchKey<T, T> searchKey, Pageable pageRequest, Object value,
            String... fields) {
        searchKey.setSearchIndex(tenantResolver.getTenant());
        return repository.multiFieldsSearch(searchKey, pageRequest, value, fields);
    }

    @Override
    public <T extends IIndexable & IDocFiles> DocFilesSummary computeDataFilesSummary(SearchKey<T, T> searchKey,
            ICriterion criterion, String discriminantProperty, List<DataType> dataTypes) {

        String[] fileTypes = new String[dataTypes.size()];
        for (int i = 0; i < dataTypes.size(); i++) {
            fileTypes[i] = dataTypes.get(i).toString();
        }

        searchKey.setSearchIndex(tenantResolver.getTenant());
        DocFilesSummary summary = new DocFilesSummary();
        // Adjust criterion to search for internal data
        ICriterion internalCrit = ICriterion.and(criterion.copy(), ICriterion.eq("internal", true));
        repository.computeInternalDataFilesSummary(searchKey, internalCrit, discriminantProperty, summary, fileTypes);
        // Adjust criterion to search for external data (=> internal is false and all at least one searched file type
        // has an uri starting with http or https
        ICriterion filterUriCrit = ICriterion.or(Arrays.stream(fileTypes)
                .map(fileType -> ICriterion.likes("files." + fileType + ".uri", "https?://.*"))
                .collect(Collectors.toList()));
        ICriterion externalCrit = ICriterion.and(criterion.copy(), ICriterion.eq("internal", false), filterUriCrit);
        repository.computeExternalDataFilesSummary(searchKey, externalCrit, discriminantProperty, summary, fileTypes);
        return summary;
    }

    @Override
    public <T extends IIndexable> List<String> searchUniqueTopValues(SearchKey<T, T> searchKey, ICriterion criterion,
            String attName, int maxCount) {
        searchKey.setSearchIndex(tenantResolver.getTenant());
        SortedSet<String> values = repository.uniqueAlphaSorted(searchKey, criterion, attName, maxCount);
        return values.stream().limit(maxCount).collect(Collectors.toList());
    }

    @Override
    public <T extends IIndexable> Aggregations getAggregations(SimpleSearchKey<T> searchKey, ICriterion criterion,
            Collection<QueryableAttribute> attributes) {
        searchKey.setSearchIndex(tenantResolver.getTenant());
        return repository.getAggregations(searchKey, criterion, attributes);
    }
}
