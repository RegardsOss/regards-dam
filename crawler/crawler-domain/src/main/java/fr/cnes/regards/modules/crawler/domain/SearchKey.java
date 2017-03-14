package fr.cnes.regards.modules.crawler.domain;

/**
 * Search key to be used with all search methods of IndexerService.
 * A SearchKey object identifies the tenant (ie ES index), the type of documents to be searched for and the
 * class of result objects (ie Dataset if search return Dataset objects)
 * @param <T> Parameterized type of result objects
 * @author oroussel
 */
public class SearchKey<T> {

    private String searchIndex;

    private String searchType;

    private Class<T> resultClass;

    /**
     * Constructor
     * @param pSearchIndex index to search on (or tenant)
     * @param pSearchType type of document to search. This parameter can be null.
     * @param pResultClass class of result document search. In case search type is null, this class must be compatible
     * with all sorts of result objects (ie AbstractEntity for Regards entity types)
     */
    public SearchKey(String pSearchIndex, String pSearchType, Class<T> pResultClass) {
        super();
        searchIndex = pSearchIndex;
        searchType = pSearchType;
        resultClass = pResultClass;
    }

    public String getSearchIndex() {
        return searchIndex;
    }

    public String getSearchType() {
        return searchType;
    }

    public Class<T> getResultClass() {
        return resultClass;
    }

}
