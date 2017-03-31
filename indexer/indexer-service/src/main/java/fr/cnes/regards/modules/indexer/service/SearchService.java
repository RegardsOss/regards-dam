package fr.cnes.regards.modules.indexer.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.JoinEntitySearchKey;
import fr.cnes.regards.modules.indexer.domain.SearchKey;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.facet.FacetType;

@Service
public class SearchService implements ISearchService {

    @Autowired
    private IEsRepository repository;

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
    public <T> Page<T> search(SimpleSearchKey<T> searchKey, Pageable pPageRequest, ICriterion pCriterion,
            Map<String, FacetType> pFacetsMap, LinkedHashMap<String, Boolean> pAscSortMap) {
        return repository.search(searchKey, pPageRequest, pCriterion, pFacetsMap, pAscSortMap);
    }

    @Override
    public <S, T extends IIndexable> Page<T> search(JoinEntitySearchKey<S, T> searchKey,
            Pageable pageRequest, ICriterion pCriterion) {
        // Create a new SearchKey to search on asked type but to only retrieve tags of found results
        SearchKey<S, String[]> tagSearchKey = new SearchKey<>(searchKey.getSearchIndex(), searchKey.getSearchTypeMap(),
                String[].class);
        // Predicate to filter each tag : it must be a valid URN and this URN must concern wanted result type
        Predicate<String> askedTypePredicate = tag -> UniformResourceName.isValidUrn(tag) && (Searches.TYPE_MAP
                .get(UniformResourceName.fromString(tag).getEntityType()) == searchKey.getResultClass());
        // Function to get Entity from its ipId (URN) (from Elasticsearch)
        Function<String, T> toAskedEntityFct = tag -> repository
                .get(searchKey.getSearchIndex(), Searches.TYPE_MAP.inverse().get(searchKey.getResultClass()).toString(),
                     tag, searchKey.getResultClass());
        List<T> objects = repository.search(tagSearchKey, pCriterion, "tags", askedTypePredicate, toAskedEntityFct);
        int total = objects.size();
        if (!objects.isEmpty()) {
            objects = objects.subList(pageRequest.getOffset(),
                                      Math.min(pageRequest.getOffset() + pageRequest.getPageSize(), objects.size()));
        }
        return new PageImpl<>(objects, pageRequest, total);
    }

    @Override
    public <T> Page<T> multiFieldsSearch(SearchKey<T, T> pSearchKey, Pageable pPageRequest, Object pValue,
            String... pFields) {
        return repository.multiFieldsSearch(pSearchKey, pPageRequest, pValue, pFields);
    }
}
