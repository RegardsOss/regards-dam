package fr.cnes.regards.modules.indexer.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.dao.FacetPage;
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
            final Pageable pageRequest, final ICriterion pCriterion) {
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
}
