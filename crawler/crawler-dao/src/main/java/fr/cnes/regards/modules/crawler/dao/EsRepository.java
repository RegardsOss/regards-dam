package fr.cnes.regards.modules.crawler.dao;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse.FieldMappingMetaData;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.range.Range.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.jboss.netty.handler.timeout.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import fr.cnes.regards.framework.gson.adapters.LocalDateTimeAdapter;
import fr.cnes.regards.modules.crawler.dao.builder.AggregationBuilderFacetTypeVisitor;
import fr.cnes.regards.modules.crawler.dao.builder.QueryBuilderCriterionVisitor;
import fr.cnes.regards.modules.crawler.domain.IIndexable;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.crawler.domain.facet.DateFacet;
import fr.cnes.regards.modules.crawler.domain.facet.FacetType;
import fr.cnes.regards.modules.crawler.domain.facet.IFacet;
import fr.cnes.regards.modules.crawler.domain.facet.NumericFacet;
import fr.cnes.regards.modules.crawler.domain.facet.StringFacet;

/**
 * Elasticsearch repository implementation
 */
@Repository
//@PropertySource("classpath:es.properties")
public class EsRepository implements IEsRepository {

    /**
     * Scrolling keeping alive Time in ms when searching into Elasticsearch
     */
    private static final int KEEP_ALIVE_SCROLLING_TIME_MS = 10000;

    /**
     * Default number of hits retrieved by scrolling
     */
    private static final int DEFAULT_SCROLLING_HITS_SIZE = 100;

    /**
     * Maximum number of retries after a timeout
     */
    private static final int MAX_TIMEOUT_RETRIES = 3;

    /**
     * QueryBuilder visitor used for Elasticsearch search requests
     */
    private static final QueryBuilderCriterionVisitor CRITERION_VISITOR = new QueryBuilderCriterionVisitor();

    /**
     * AggregationBuilder visitor used for Elasticsearch search requests with facets
     */
    private static final AggregationBuilderFacetTypeVisitor FACET_VISITOR = new AggregationBuilderFacetTypeVisitor();

    /**
     * Elasticsearch port
     */
    private String esClusterName;

    /**
     * Elasticsearch host
     */
    private String esHost;

    /**
     * Elasticsearch address
     */
    private String esAddress;

    /**
     * Elasticsearch TCP port
     */
    private int esPort = 9300;

    /**
     * Client to ElasticSearch base
     */
    private final TransportClient client;

    /**
     * Json mapper
     */
    private final Gson gson;

    /**
     * Constructor
     *
     * @param pGson
     *            JSon mapper bean
     */
    public EsRepository(@Autowired Gson pGson, @Value("${elasticsearch.host:}") String pEsHost,
            @Value("${elasticsearch.address:}") String pEsAddress, @Value("${elasticsearch.tcp.port}") int pEsPort,
            @Value("${elasticsearch.cluster.name}") String pEsClusterName) {
        this.gson = pGson;
        this.esHost = Strings.isEmpty(pEsHost) ? null : pEsHost;
        this.esAddress = Strings.isEmpty(pEsAddress) ? null : pEsAddress;
        this.esPort = pEsPort;
        this.esClusterName = pEsClusterName;
        client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", esClusterName).build());
        try {
            client.addTransportAddress(new InetSocketTransportAddress(
                    InetAddress.getByName((esHost != null) ? esHost : esAddress), esPort));
        } catch (final UnknownHostException e) {
            Throwables.propagate(e);
        }
        // Testinf availability of ES
        List<DiscoveryNode> nodes = client.connectedNodes();
        if (nodes.isEmpty()) {
            throw new NoNodeAvailableException("Elasticsearch is down");
        }
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public boolean createIndex(String pIndex) {
        return client.admin().indices().prepareCreate(pIndex.toLowerCase()).get().isAcknowledged();
    }

    @Override
    public boolean delete(String pIndex, String pType, String pId) {
        final DeleteResponse response = client.prepareDelete(pIndex.toLowerCase(), pType, pId).get();
        return ((response.getResult() == Result.DELETED) || (response.getResult() == Result.NOT_FOUND));
    }

    @Override
    public boolean deleteIndex(String pIndex) {
        return client.admin().indices().prepareDelete(pIndex.toLowerCase()).get().isAcknowledged();

    }

    @Override
    public String[] findIndices() {
        return Iterables
                .toArray(Iterables.transform(client.admin().indices().prepareGetSettings().get().getIndexToSettings(),
                                             (pSetting) -> pSetting.key),
                         String.class);
    }

    @Override
    public <T extends IIndexable> T get(String pIndex, String pType, String pId, Class<T> pClass) {
        try {
            final GetResponse response = client.prepareGet(pIndex.toLowerCase(), pType, pId).get();
            if (!response.isExists()) {
                return null;
            }
            return gson.fromJson(response.getSourceAsString(), pClass);
        } catch (final JsonSyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean indexExists(String pName) {
        return client.admin().indices().prepareExists(pName.toLowerCase()).get().isExists();
    }

    @Override
    public boolean merge(String pIndex, String pType, String pId, Map<String, Object> pMergedPropertiesMap) {
        try {
            final Map<String, Map<String, Object>> mapMap = new HashMap<>();
            try (XContentBuilder builder = XContentFactory.jsonBuilder().startObject()) {
                for (final Map.Entry<String, Object> entry : pMergedPropertiesMap.entrySet()) {
                    // Simple key = value
                    if (!entry.getKey().contains(".")) {
                        builder.field(entry.getKey(), entry.getValue());
                    } else { // Complex key => key.subKey = value
                        final String name = entry.getKey().substring(0, entry.getKey().indexOf('.'));
                        if (!mapMap.containsKey(name)) {
                            mapMap.put(name, new HashMap<>());
                        }
                        final Map<String, Object> subMap = mapMap.get(name);
                        subMap.put(entry.getKey().substring(entry.getKey().indexOf('.') + 1), entry.getValue());
                    }
                }
                // Pending sub objects ?
                if (!mapMap.isEmpty()) {
                    for (final Map.Entry<String, Map<String, Object>> entry : mapMap.entrySet()) {
                        builder.field(entry.getKey(), entry.getValue());
                    }
                }
                final UpdateResponse response = client.prepareUpdate(pIndex.toLowerCase(), pType, pId)
                        .setDoc(builder.endObject()).get();
                return (response.getResult() == Result.UPDATED);
            }
        } catch (final IOException jpe) {
            throw Throwables.propagate(jpe);
        }
    }

    private void checkDocument(IIndexable pDoc) {
        if (Strings.isNullOrEmpty(pDoc.getDocId()) || Strings.isNullOrEmpty(pDoc.getType())) {
            throw new IllegalArgumentException("docId and type are mandatory on an IIndexable object");
        }
    }

    @Override
    public boolean save(String pIndex, IIndexable pDocument) {
        checkDocument(pDocument);
        final IndexResponse response = client
                .prepareIndex(pIndex.toLowerCase(), pDocument.getType(), pDocument.getDocId())
                .setSource(gson.toJson(pDocument)).get();
        return (response.getResult() == Result.CREATED);
    }

    @Override
    public void refresh(String pIndex) {
        // To make just saved documents searchable, the associated index must be refreshed
        client.admin().indices().prepareRefresh(pIndex.toLowerCase()).get();
    }

    @Override
    public <T extends IIndexable> Map<String, Throwable> saveBulk(String pIndex,
            @SuppressWarnings("unchecked") T... pDocuments) {
        String index = pIndex.toLowerCase();
        for (T doc : pDocuments) {
            checkDocument(doc);
        }
        final BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (T doc : pDocuments) {
            bulkRequest.add(client.prepareIndex(index, doc.getType(), doc.getDocId()).setSource(gson.toJson(doc)));
        }
        final BulkResponse response = bulkRequest.get();
        Map<String, Throwable> errorMap = null;
        for (final BulkItemResponse itemResponse : response.getItems()) {
            if (itemResponse.isFailed()) {
                if (errorMap == null) {
                    errorMap = new HashMap<>();
                }
                errorMap.put(itemResponse.getId(), itemResponse.getFailure().getCause());
            }
        }
        // To make just saved documents searchable, the associated index must be refreshed
        client.admin().indices().prepareRefresh(index).get();
        return errorMap;
    }

    @Override
    public <T> void searchAll(String pIndex, Class<T> pClass, Consumer<T> pAction, ICriterion pCrit) {
        SearchResponse scrollResp = client.prepareSearch(pIndex.toLowerCase())
                .setScroll(new TimeValue(KEEP_ALIVE_SCROLLING_TIME_MS)).setQuery(pCrit.accept(CRITERION_VISITOR))
                .setSize(DEFAULT_SCROLLING_HITS_SIZE).get();
        // Scroll until no hits are returned
        do {
            for (final SearchHit hit : scrollResp.getHits().getHits()) {
                pAction.accept(gson.fromJson(hit.getSourceAsString(), pClass));
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(KEEP_ALIVE_SCROLLING_TIME_MS)).execute().actionGet();
        } while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while
                                                              // loop.
    }

    @Override
    public <T> Page<T> searchAllLimited(String pIndex, Class<T> pClass, int pPageSize) {
        return this.searchAllLimited(pIndex, pClass, new PageRequest(0, pPageSize));
    }

    @Override
    public <T> Page<T> searchAllLimited(String pIndex, Class<T> pClass, Pageable pPageRequest) {
        try {
            final List<T> results = new ArrayList<>();
            SearchRequestBuilder request = client.prepareSearch(pIndex.toLowerCase()).setFrom(pPageRequest.getOffset())
                    .setSize(pPageRequest.getPageSize());
            SearchResponse response = getWithTimeouts(request);
            SearchHits hits = response.getHits();
            for (SearchHit hit : hits) {
                results.add(gson.fromJson(hit.getSourceAsString(), pClass));
            }
            return new PageImpl<>(results, pPageRequest, response.getHits().getTotalHits());
        } catch (final JsonSyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public <T> Page<T> search(String pIndex, Class<T> pClass, int pPageSize, ICriterion criterion,
            Map<String, FacetType> pFacetsMap, LinkedHashMap<String, Boolean> pAscSortMap) {
        return this.search(pIndex, pClass, new PageRequest(0, pPageSize), criterion, pFacetsMap, pAscSortMap);
    }

    @Override
    public <T> Page<T> search(String pIndex, Class<T> pClass, Pageable pPageRequest, ICriterion criterion,
            Map<String, FacetType> pFacetsMap, LinkedHashMap<String, Boolean> pAscSortMap) {
        String index = pIndex.toLowerCase();
        try {
            final List<T> results = new ArrayList<>();
            // Use filter instead of "direct" query (in theory, quickest because no score is computed)
            QueryBuilder critBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
                    .filter(criterion.accept(CRITERION_VISITOR));
            // QueryBuilder critBuilder = criterion.accept(CRITERION_VISITOR);
            SearchRequestBuilder request = client.prepareSearch(index).setQuery(critBuilder)
                    .setFrom(pPageRequest.getOffset()).setSize(pPageRequest.getPageSize());
            if (pAscSortMap != null) {
                manageSortRequest(index, request, pAscSortMap);
            }
            // Managing aggregations if some facets are asked
            boolean twoPassRequestNeeded = manageFirstPassRequestAggregations(pFacetsMap, request);
            // Launch the request
            SearchResponse response = getWithTimeouts(request);

            // At least one numeric facet is present, we need to replace all numeric facets by associated range facets
            if (twoPassRequestNeeded) {
                // Rebuild request
                request = client.prepareSearch(index).setQuery(critBuilder).setFrom(pPageRequest.getOffset())
                        .setSize(pPageRequest.getPageSize());
                Map<String, Aggregation> aggsMap = response.getAggregations().asMap();
                manageSecondPassRequestAggregations(pFacetsMap, request, aggsMap);
                // Relaunch the request with replaced facets
                response = getWithTimeouts(request);
            }

            Map<String, IFacet<?>> facetResultsMap = null;
            if (pFacetsMap != null) {
                // Get the new aggregations result map
                Map<String, Aggregation> aggsMap = response.getAggregations().asMap();
                // Create the facet map
                facetResultsMap = new HashMap<>();
                for (Map.Entry<String, FacetType> entry : pFacetsMap.entrySet()) {
                    FacetType facetType = entry.getValue();
                    String attributeName = entry.getKey();
                    fillFacetMap(aggsMap, facetResultsMap, facetType, attributeName);
                }
            }

            SearchHits hits = response.getHits();
            for (SearchHit hit : hits) {
                results.add(gson.fromJson(hit.getSourceAsString(), pClass));
            }
            // If no facet, juste returns a "simple" Page
            if (facetResultsMap == null) {
                return new PageImpl<>(results, pPageRequest, response.getHits().getTotalHits());
            } else { // else returns a FacetPage
                return new FacetPage<>(results, facetResultsMap, pPageRequest, response.getHits().getTotalHits());
            }
        } catch (final JsonSyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Add sort to the request
     * @param request search request
     * @param pAscSortMap map(attribute name, true if ascending)
     */
    private void manageSortRequest(String pIndex, SearchRequestBuilder request,
            LinkedHashMap<String, Boolean> pAscSortMap) {
        // Because string attributes are not indexed with Elasticsearch, it is necessary to add ".keyword" at
        // end of attribute name into sort request. So we need to know string attributes
        GetFieldMappingsResponse response = client.admin().indices().prepareGetFieldMappings(pIndex)
                .setFields(pAscSortMap.keySet().toArray(new String[pAscSortMap.size()])).get();
        // map(type, map(field, metadata)) for asked index
        Map<String, Map<String, FieldMappingMetaData>> mappings = response.mappings().get(pIndex);
        // All types mappings are retrieved.
        // NOTE: in our context, attributes have same metada for all types so once we found one, we stop.
        // To do that, we create a new LinkedHashMap to KEEPS keys order !!! (crucial)
        LinkedHashMap<String, Boolean> updatedAscSortMap = new LinkedHashMap<>(pAscSortMap.size());
        for (Map.Entry<String, Boolean> sortEntry : pAscSortMap.entrySet()) {
            String attributeName = sortEntry.getKey();
            // "terminal" field name ie. for "toto.titi.tutu" => "tutu"
            String lastPathAttName = attributeName.contains(".")
                    ? attributeName.substring(attributeName.lastIndexOf('.') + 1) : attributeName;
            // For all type mappings
            boolean typeText = false;
            for (Map.Entry<String, Map<String, FieldMappingMetaData>> typeEntry : mappings.entrySet()) {
                // Once we found attribute name on one type, we stop to next attribute
                if (typeEntry.getValue().containsKey(attributeName)) {
                    FieldMappingMetaData attMetaData = typeEntry.getValue().get(attributeName);
                    // If field type is String, we must add ".keyword" to attribute name
                    Map<String, Object> metaDataMap = attMetaData.sourceAsMap();
                    if ((metaDataMap.get(lastPathAttName) != null)
                            && (metaDataMap.get(lastPathAttName) instanceof Map)) {
                        Map<?, ?> mappingMap = (Map<?, ?>) metaDataMap.get(lastPathAttName);
                        // Should contains "type" field but...
                        if (mappingMap.containsKey("type")) {
                            if (mappingMap.get("type").equals("text")) {
                                updatedAscSortMap.put(attributeName + ".keyword", sortEntry.getValue());
                                typeText = true;
                                break;
                            } else { // "type" field found => it's not "text"
                                typeText = false;
                                break;
                            }
                        }
                    }
                }
            }
            // Not "text" type => add the key/value to keep the sort order
            if (!typeText) {
                updatedAscSortMap.put(attributeName, sortEntry.getValue());
            }
        }
        // Add sort to request
        updatedAscSortMap.entrySet()
                .forEach(entry -> request.addSort(entry.getKey(), entry.getValue() ? SortOrder.ASC : SortOrder.DESC));
    }

    /**
     * Add aggregations to the search request.
     * @param pFacetsMap asked facets
     * @param request search request
     * @return true if a second pass is needed (managing range facets)
     */
    private boolean manageFirstPassRequestAggregations(Map<String, FacetType> pFacetsMap,
            SearchRequestBuilder request) {
        boolean twoPassRequestNeeded = false;
        if (pFacetsMap != null) {
            // Numeric/date facets needs :
            // First to add a percentiles aggregation to retrieved 9 values corresponding to
            // 10%, 20 %, ..., 90 % of the values
            // Secondly to add a range aggregation with these 9 values in order to retrieve 10 buckets of equal
            // size of values
            for (Map.Entry<String, FacetType> entry : pFacetsMap.entrySet()) {
                FacetType facetType = entry.getValue();
                if ((facetType == FacetType.NUMERIC) || (facetType == FacetType.DATE)) {
                    twoPassRequestNeeded = true;
                }
                request.addAggregation(facetType.accept(FACET_VISITOR, entry.getKey()));
            }
        }
        return twoPassRequestNeeded;
    }

    /**
     * Add aggregations to the search request (second pass). For range aggregations, use percentiles results
     * (from first pass request results) to create range aggregagtions.
     * @param pFacetsMap asked facets
     * @param request search request
     * @param aggsMap first pass aggregagtions results
     */
    private void manageSecondPassRequestAggregations(Map<String, FacetType> pFacetsMap, SearchRequestBuilder request,
            Map<String, Aggregation> aggsMap) {
        for (Map.Entry<String, FacetType> entry : pFacetsMap.entrySet()) {
            FacetType facetType = entry.getValue();
            String attributeName = entry.getKey();
            // Replace percentiles aggregations by range aggregagtions
            if (facetType == FacetType.NUMERIC) {
                Percentiles percentiles = (Percentiles) aggsMap
                        .get(attributeName + AggregationBuilderFacetTypeVisitor.NUMERIC_FACET_POSTFIX);
                request.addAggregation(FacetType.RANGE.accept(FACET_VISITOR, attributeName, percentiles));
            } else if (facetType == FacetType.DATE) {
                Percentiles percentiles = (Percentiles) aggsMap
                        .get(attributeName + AggregationBuilderFacetTypeVisitor.DATE_FACET_POSTFIX);
                request.addAggregation(FacetType.RANGE.accept(FACET_VISITOR, attributeName, percentiles));
            } else { // Let it as upper
                request.addAggregation(facetType.accept(FACET_VISITOR, attributeName));
            }
        }
    }

    /**
     * Compute aggregations results to fill results facet map of an attribute
     * @param aggsMap aggregation resuls map
     * @param facetMap map of results facets
     * @param facetType type of facet for given attribute
     * @param attributeName given attribute
     */
    private void fillFacetMap(Map<String, Aggregation> aggsMap, Map<String, IFacet<?>> facetMap, FacetType facetType,
            String attributeName) {
        switch (facetType) {
            case STRING: {
                Terms terms = (Terms) aggsMap
                        .get(attributeName + AggregationBuilderFacetTypeVisitor.STRING_FACET_POSTFIX);
                Map<String, Long> valueMap = new LinkedHashMap<>(terms.getBuckets().size());
                terms.getBuckets().forEach(b -> valueMap.put(b.getKeyAsString(), b.getDocCount()));
                facetMap.put(attributeName, new StringFacet(attributeName, valueMap));
                break;
            }
            case NUMERIC: {
                org.elasticsearch.search.aggregations.bucket.range.Range numRange = (org.elasticsearch.search.aggregations.bucket.range.Range) aggsMap
                        .get(attributeName + AggregationBuilderFacetTypeVisitor.RANGE_FACET_POSTFIX);
                Map<Range<Double>, Long> valueMap = new LinkedHashMap<>();
                for (Bucket bucket : numRange.getBuckets()) {
                    Range<Double> valueRange;
                    // (-∞ -> ?
                    if (bucket.getFrom() == null) {
                        // (-∞ -> +∞) (completely dumb but...who knows ?)
                        if (bucket.getTo() == null) {
                            valueRange = Range.all();
                        } else { // (-∞ -> value]
                            valueRange = Range.atMost((Double) bucket.getTo());
                        }
                    } else if (bucket.getTo() == null) { // ? -> +∞)
                        valueRange = Range.greaterThan((Double) bucket.getFrom());
                    } else { // [value -> value)
                        valueRange = Range.closedOpen((Double) bucket.getFrom(), (Double) bucket.getTo());
                    }
                    valueMap.put(valueRange, bucket.getDocCount());
                }
                facetMap.put(attributeName, new NumericFacet(attributeName, valueMap));
                break;
            }
            case DATE: {
                org.elasticsearch.search.aggregations.bucket.range.Range dateRange = (org.elasticsearch.search.aggregations.bucket.range.Range) aggsMap
                        .get(attributeName + AggregationBuilderFacetTypeVisitor.RANGE_FACET_POSTFIX);
                Map<com.google.common.collect.Range<LocalDateTime>, Long> valueMap = new LinkedHashMap<>();
                for (Bucket bucket : dateRange.getBuckets()) {
                    Range<LocalDateTime> valueRange;
                    // (-∞ -> ?
                    if (bucket.getFromAsString() == null) {
                        // (-∞ -> +∞) (completely dumb but...who knows ?)
                        if (bucket.getToAsString() == null) {
                            valueRange = Range.all();
                        } else { // (-∞ -> value]
                            valueRange = Range.atMost(LocalDateTimeAdapter.parse(bucket.getToAsString()));
                        }
                    } else if (bucket.getToAsString() == null) { // ? -> +∞)
                        valueRange = Range.greaterThan(LocalDateTimeAdapter.parse(bucket.getFromAsString()));
                    } else { // [value -> value)
                        valueRange = Range.closedOpen(LocalDateTimeAdapter.parse(bucket.getFromAsString()),
                                                      LocalDateTimeAdapter.parse(bucket.getToAsString()));
                    }
                    valueMap.put(valueRange, bucket.getDocCount());
                }
                facetMap.put(attributeName, new DateFacet(attributeName, valueMap));
                break;
            }
            default:
                break;
        }
    }

    @Override
    public <T> Page<T> multiFieldsSearch(String pIndex, Class<T> pClass, int pPageSize, Object pValue,
            String... pFields) {
        return multiFieldsSearch(pIndex, pClass, new PageRequest(0, pPageSize), pValue, pFields);
    }

    @Override
    public <T> Page<T> multiFieldsSearch(String pIndex, Class<T> pClass, Pageable pPageRequest, Object pValue,
            String... pFields) {
        try {
            final List<T> results = new ArrayList<>();
            // LocalDateTime must be formatted to be correctly used following Gson mapping
            Object value = (pValue instanceof LocalDateTime) ? LocalDateTimeAdapter.format((LocalDateTime) pValue)
                    : pValue;
            QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(value, pFields);
            SearchRequestBuilder request = client.prepareSearch(pIndex.toLowerCase()).setQuery(queryBuilder)
                    .setFrom(pPageRequest.getOffset()).setSize(pPageRequest.getPageSize());
            SearchResponse response = getWithTimeouts(request);
            SearchHits hits = response.getHits();
            for (SearchHit hit : hits) {
                results.add(gson.fromJson(hit.getSourceAsString(), pClass));
            }
            return new PageImpl<>(results, pPageRequest, response.getHits().getTotalHits());
        } catch (final JsonSyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Call specified request at least MAX_TIMEOUT_RETRIES
     * @param request
     * @return
     */
    private SearchResponse getWithTimeouts(SearchRequestBuilder request) {
        SearchResponse response;
        int errorCount = 0;
        do {
            response = request.get();
            errorCount += response.isTimedOut() ? 1 : 0;
            if (errorCount == MAX_TIMEOUT_RETRIES) {
                throw new TimeoutException(
                        String.format("Get %d timeouts while attempting to retrieve data", MAX_TIMEOUT_RETRIES));
            }
        } while (response.isTimedOut());
        return response;
    }
}
