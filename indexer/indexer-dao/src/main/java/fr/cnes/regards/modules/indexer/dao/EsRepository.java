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

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.range.Range.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.modules.indexer.dao.builder.AggregationBuilderFacetTypeVisitor;
import static fr.cnes.regards.modules.indexer.dao.builder.AggregationBuilderFacetTypeVisitor.DATE_FACET_SUFFIX;
import static fr.cnes.regards.modules.indexer.dao.builder.AggregationBuilderFacetTypeVisitor.NUMERIC_FACET_SUFFIX;
import fr.cnes.regards.modules.indexer.dao.builder.QueryBuilderCriterionVisitor;
import fr.cnes.regards.modules.indexer.dao.converter.SortToLinkedHashMap;
import fr.cnes.regards.modules.indexer.domain.IDocFiles;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.SearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.facet.DateFacet;
import fr.cnes.regards.modules.indexer.domain.facet.FacetType;
import fr.cnes.regards.modules.indexer.domain.facet.IFacet;
import fr.cnes.regards.modules.indexer.domain.facet.NumericFacet;
import fr.cnes.regards.modules.indexer.domain.facet.StringFacet;
import fr.cnes.regards.modules.indexer.domain.reminder.SearchAfterReminder;
import fr.cnes.regards.modules.indexer.domain.summary.DocFilesSubSummary;
import fr.cnes.regards.modules.indexer.domain.summary.DocFilesSummary;
import fr.cnes.regards.modules.indexer.domain.summary.FilesSummary;

/**
 * Elasticsearch repository implementation
 * @author oroussel
 */
@Repository
public class EsRepository implements IEsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsRepository.class);

    /**
     * Scrolling keeping alive Time in ms when searching into Elasticsearch
     * Set it to 10 minutes to avoid timeouts while scrolling.
     */
    private static final int KEEP_ALIVE_SCROLLING_TIME_MS = 60000 * 10;

    /**
     * Default number of hits retrieved by scrolling (10 is the default value and according to doc is the best value)
     */
    private static final int DEFAULT_SCROLLING_HITS_SIZE = 100;

    /**
     * Maximum number of retries after a timeout
     */
    private static final int MAX_TIMEOUT_RETRIES = 3;

    /**
     * Target forwarding search {@link EsRepository#searchAll} need to put in cache search because of pagination restrictions. This constant specifies duration cache time in minutes (from last access)
     */
    private static final int TARGET_FORWARDING_CACHE_MN = 3;

    /**
     * QueryBuilder visitor used for Elasticsearch search requests
     */
    private static final QueryBuilderCriterionVisitor CRITERION_VISITOR = new QueryBuilderCriterionVisitor();

    public static final String REMINDER_IDX = "reminder";

    /**
     * Single scheduled executor service to clean reminder tasks once expiration date is reached
     */
    private ScheduledExecutorService reminderCleanExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * AggregationBuilder visitor used for Elasticsearch search requests with facets
     */
    @Autowired
    private final AggregationBuilderFacetTypeVisitor aggBuilderFacetTypeVisitor;

    /**
     * Empty JSon object
     */
    private static final String EMPTY_JSON = "{}";

    /**
     * Geometry field name
     */
    private static final String GEOM_NAME = "geometry";

    /**
     * Geometry field mapping properties
     */
    private static final String GEOM_TYPE_PROP = "type=geo_shape";

    /**
     * Elasticsearch host
     */
    private final String esHost;

    /**
     * Elasticsearch port
     */
    private int esPort;

    /**
     * Low level Rest API client
     */
    private RestClient restClient;

    /**
     * High level Rest API client
     */
    private RestHighLevelClient client;

    /**
     * Json mapper
     */
    private final Gson gson;

    /**
     * Constructor
     * @param pGson JSon mapper bean
     */
    public EsRepository(@Autowired Gson pGson, @Value("${regards.elasticsearch.host:}") String inEsHost,
            @Value("${regards.elasticsearch.address:}") String inEsAddress,
            @Value("${regards.elasticsearch.http.port}") int esPort,
            AggregationBuilderFacetTypeVisitor aggBuilderFacetTypeVisitor) throws UnknownHostException {

        gson = pGson;
        this.esHost = Strings.isEmpty(inEsHost) ? inEsAddress : inEsHost;
        this.esPort = esPort;
        this.aggBuilderFacetTypeVisitor = aggBuilderFacetTypeVisitor;

        String connectionInfoMessage = String
                .format("Elastic search connection properties : host \"%s\", port \"%d\"", this.esHost, this.esPort);
        LOGGER.info(connectionInfoMessage);

        restClient = RestClient.builder(new HttpHost(this.esHost, this.esPort)).build();
        client = new RestHighLevelClient(restClient);

        try {
            // Testing availability of ES
            if (!client.ping()) {
                throw new NoNodeAvailableException("Elasticsearch is down. " + connectionInfoMessage);
            }
        } catch (Exception e) {
            throw new NoNodeAvailableException("Error while pinging Elasticsearch (" + connectionInfoMessage + ")", e);
        }
    }

    @Override
    public void close() {
        LOGGER.info("Closing connection");
        try {
            restClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    @Override
    public boolean createIndex(String index) {
        try {
            Response response = restClient.performRequest("PUT", index.toLowerCase());
            return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean setAutomaticDoubleMapping(String index, String... types) {
        try (XContentBuilder builder = XContentFactory.jsonBuilder()) {
            String mapping = builder.startObject().startArray("dynamic_templates").startObject()
                    .startObject("doubles").field("match_mapping_type", "double").startObject("mapping")
                    .field("type", "double").endObject().endObject().endObject().endArray().endObject().string();

            try (NStringEntity entity = new NStringEntity(mapping, ContentType.APPLICATION_JSON)) {

                for (String type : types) {
                    Response response = restClient.performRequest("PUT", index.toLowerCase() + "/" + type + "/_mapping",
                                                                  Collections.emptyMap(), entity);
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        return false;
                    }
                }
                return true;
            }
        } catch (IOException ioe) { // NOSONAR
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public boolean setGeometryMapping(String index, String... types) {
        try {
            for (String type : types) {
                try (XContentBuilder builder = XContentFactory.jsonBuilder()) {
                    String mapping = builder.startObject().startObject(type).startObject("properties")
                            .startObject("geometry").field("type", "geo_shape").endObject().endObject().endObject()
                            .endObject().string();

                    HttpEntity entity = new NStringEntity(mapping, ContentType.APPLICATION_JSON);
                    Response response = restClient.performRequest("PUT", index.toLowerCase() + "/" + type + "/_mapping",
                                                                  Collections.emptyMap(), entity);
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        return false;
                    }
                }
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(String index, String type, String id) {
        DeleteRequest request = new DeleteRequest(index.toLowerCase(), type, id);
        try {
            DeleteResponse response = client.delete(request);
            return ((response.getResult() == Result.DELETED) || (response.getResult() == Result.NOT_FOUND));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long deleteAll(String inIndex) {
        return this.deleteByQuery(inIndex.toLowerCase(), ICriterion.all());
    }

    @Override
    public long deleteByQuery(String index, ICriterion criterion) {
        try {
            HttpEntity entity = new NStringEntity("{ \"query\":" + criterion.accept(CRITERION_VISITOR).toString() + "}",
                                                  ContentType.APPLICATION_JSON);
            Response response = restClient
                    .performRequest("POST", index.toLowerCase() + "/_delete_by_query", Collections.emptyMap(), entity);

            try (InputStream is = response.getEntity().getContent()) {
                Map<String, Object> map = XContentHelper.convertToMap(XContentType.JSON.xContent(), is, true);
                return ((Number) map.get("deleted")).longValue();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteIndex(String index) throws IndexNotFoundException {
        try {
            Response response = restClient.performRequest("DELETE", index.toLowerCase());
            return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new IndexNotFoundException(index.toLowerCase());
            }
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    @Override
    public <T extends IIndexable> T get(String pIndex, String pType, String pId, Class<T> pClass) {
        GetRequest request = new GetRequest(pIndex.toLowerCase(), pType, pId);
        try {
            GetResponse response = client.get(request);
            if (!response.isExists()) {
                return null;
            }
            return gson.fromJson(response.getSourceAsString(), pClass);
        } catch (final JsonSyntaxException | IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    @Override
    public boolean indexExists(String name) {
        try {
            Response response = restClient.performRequest("HEAD", name.toLowerCase());
            return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        } catch (IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    private void checkDocument(IIndexable pDoc) {
        if (Strings.isNullOrEmpty(pDoc.getDocId()) || Strings.isNullOrEmpty(pDoc.getType())) {
            throw new IllegalArgumentException("docId and type are mandatory on an IIndexable object");
        }
    }

    @Override
    public boolean save(String index, IIndexable doc) {
        checkDocument(doc);
        try {
            IndexResponse response = client.index(new IndexRequest(index.toLowerCase(), doc.getType(), doc.getDocId())
                                                          .source(gson.toJson(doc), XContentType.JSON));
            return (response.getResult() == Result.CREATED);
        } catch (IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    @Override
    public void refresh(String index) {
        // To make just saved documents searchable, the associated index must be refreshed
        try {
            restClient.performRequest("GET", index.toLowerCase() + "/_refresh");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends IIndexable> int saveBulk(String inIndex, @SuppressWarnings("unchecked") T... documents) {
        try {
            if (documents.length == 0) {
                return 0;
            }
            String index = inIndex.toLowerCase();
            for (T doc : documents) {
                checkDocument(doc);
            }
            int savedDocCount = 0;
            BulkRequest bulkRequest = new BulkRequest();
            for (T doc : documents) {
                bulkRequest.add(new IndexRequest(index, doc.getType(), doc.getDocId())
                                        .source(gson.toJson(doc), XContentType.JSON));
            }

            BulkResponse response = client.bulk(bulkRequest);
            for (final BulkItemResponse itemResponse : response.getItems()) {
                if (itemResponse.isFailed()) {
                    LOGGER.warn(String.format("Document of type %s of id %s cannot be saved", documents[0].getClass(),
                                              itemResponse.getId()), itemResponse.getFailure().getCause());
                } else {
                    savedDocCount++;
                }
            }
            // To make just saved documents searchable, the associated index must be refreshed
            this.refresh(index);
            return savedDocCount;
        } catch (IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    @Override
    public <T> void searchAll(SearchKey<T, T> searchKey, Consumer<T> action, ICriterion inCrit) {
        try {
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex());
            request.types(searchKey.getSearchTypes());
            ICriterion crit = (inCrit == null) ? ICriterion.all() : inCrit;
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(crit.accept(CRITERION_VISITOR)).size(DEFAULT_SCROLLING_HITS_SIZE);
            request.source(builder);
            request.scroll(TimeValue.timeValueMillis(KEEP_ALIVE_SCROLLING_TIME_MS));
            SearchResponse scrollResp = client.search(request);

            // Scroll until no hits are returned
            do {
                for (final SearchHit hit : scrollResp.getHits().getHits()) {
                    action.accept(gson.fromJson(hit.getSourceAsString(), searchKey.fromType(hit.getType())));
                }

                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollResp.getScrollId());
                scrollRequest.scroll(TimeValue.timeValueMillis(KEEP_ALIVE_SCROLLING_TIME_MS));
                scrollResp = client.searchScroll(scrollRequest);
            } while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while
            // loop.
        } catch (IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    /**
     * Returns a set of attribute values from search documents
     * @param searchKey search key for documents of type R
     * @param pCrit search criterion on documents of type R
     * @param attributeSource document attribute to return
     * @param <R> Type of document to apply search
     * @return a set of unique attribute values
     */
    private <R> Set<Object> searchJoined(SearchKey<?, R> searchKey, ICriterion pCrit, String attributeSource) {
        try {
            // Only first type is chosen, this case is too complex to permit a multi-type search
            // Add ".keyword" if attribute mapping type is of type text
            String attribute = isTextMapping(searchKey.getSearchIndex(), searchKey.getSearchTypes()[0],
                                             attributeSource) ? attributeSource + ".keyword" : attributeSource;
            return this.unique(searchKey, pCrit, attribute);
        } catch (IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    @Override
    public <T> Page<T> searchAllLimited(String index, Class<T> clazz, Pageable pageRequest) {
        try {
            final List<T> results = new ArrayList<>();
            SearchRequest request = new SearchRequest(index.toLowerCase());

            SearchSourceBuilder builder = new SearchSourceBuilder().from(pageRequest.getOffset())
                    .size(pageRequest.getPageSize());
            request.source(builder);

            SearchResponse response = client.search(request);
            SearchHits hits = response.getHits();
            for (SearchHit hit : hits) {
                results.add(gson.fromJson(hit.getSourceAsString(), clazz));
            }
            return new PageImpl<>(results, pageRequest, response.getHits().getTotalHits());
        } catch (final JsonSyntaxException | IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    @Override
    public <T extends IIndexable> FacetPage<T> search(SearchKey<T, T> searchKey, Pageable pageRequest, ICriterion crit,
            Map<String, FacetType> facetsMap) {
        String index = searchKey.getSearchIndex();
        try {
            final List<T> results = new ArrayList<>();
            ICriterion criterion = (crit == null) ? ICriterion.all() : crit;
            // Use filter instead of "direct" query (in theory, quickest because no score is computed)
            QueryBuilder critBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
                    .filter(criterion.accept(CRITERION_VISITOR));

            Sort sort = pageRequest.getSort();
            // page size is max or page offset is > max page size, prepare sort for search_after
            if ((pageRequest.getOffset() >= MAX_RESULT_WINDOW) || (pageRequest.getPageSize() == MAX_RESULT_WINDOW)) {
                // A sort is mandatory to permit use of searchAfter (id by default if none provided)
                sort = (sort == null) ? new Sort("docId") : pageRequest.getSort();
                // To assure unicity, always add "docId" as a sort parameter
                if (sort.getOrderFor("docId") == null) {
                    sort = sort.and(new Sort("docId"));
                }
            }
            Object[] lastSearchAfterSortValues = null;
            // If page starts over index 10 000, advance with searchAfter just before last request
            if (pageRequest.getOffset() >= MAX_RESULT_WINDOW) {
                lastSearchAfterSortValues = advanceWithSearchAfter(criterion, searchKey, pageRequest, index, sort,
                                                                   critBuilder);
            }

            SearchRequest request = new SearchRequest(index).types(searchKey.getSearchTypes());
            SearchSourceBuilder builder = new SearchSourceBuilder().query(critBuilder).from(pageRequest.getOffset())
                    .size(pageRequest.getPageSize());

            // If searchAfter has been executed (in that case manageSortRequest() has already been called)
            if (lastSearchAfterSortValues != null) {
                builder.searchAfter(lastSearchAfterSortValues).from(0);
                manageSortRequest(index, builder, sort);
            } else if (pageRequest.getSort() != null) { // Don't forget to manage sort if one is provided
                manageSortRequest(index, builder, sort);
            }

            // Managing aggregations if some facets are asked
            boolean twoPassRequestNeeded = manageFirstPassRequestAggregations(facetsMap, builder);
            request.source(builder);
            // Launch the request
            SearchResponse response = client.search(request);

            Set<IFacet<?>> facetResults = new HashSet<>();
            if (response.getHits().getTotalHits() != 0) {
                // At least one numeric facet is present, we need to replace all numeric facets by associated range facets
                if (twoPassRequestNeeded) {
                    // Rebuild request
                    request = new SearchRequest(index).types(searchKey.getSearchTypes());
                    builder = new SearchSourceBuilder().query(critBuilder).from(pageRequest.getOffset())
                            .size(pageRequest.getPageSize());
                    if (lastSearchAfterSortValues != null) {
                        builder.searchAfter(lastSearchAfterSortValues).from(0); // needed by searchAfter
                        manageSortRequest(index, builder, sort);
                    } else if (pageRequest.getSort() != null) { // Don't forget to manage sort if one is provided
                        manageSortRequest(index, builder, pageRequest.getSort());
                    }
                    Map<String, Aggregation> aggsMap = response.getAggregations().asMap();
                    manageSecondPassRequestAggregations(facetsMap, builder, aggsMap);
                    // Relaunch the request with replaced facets
                    request.source(builder);
                    response = client.search(request);
                }

                // If offset >= MAX_RESULT_WINDOW or page size = MAX_RESULT_WINDOW, this means a next page should exist
                // (not necessarly)
                if ((pageRequest.getOffset() >= MAX_RESULT_WINDOW) || (pageRequest.getPageSize()
                        == MAX_RESULT_WINDOW)) {
                    saveReminder(searchKey, pageRequest, crit, sort, response);
                }

                if (response.getAggregations() != null) {
                    // Get the new aggregations result map
                    Map<String, Aggregation> aggsMap = response.getAggregations().asMap();
                    // Fill the facet set
                    for (Map.Entry<String, FacetType> entry : facetsMap.entrySet()) {
                        FacetType facetType = entry.getValue();
                        String attributeName = entry.getKey();
                        fillFacets(aggsMap, facetResults, facetType, attributeName);
                    }
                }
            }

            SearchHits hits = response.getHits();
            for (SearchHit hit : hits) {
                results.add(gson.fromJson(hit.getSourceAsString(), searchKey.fromType(hit.getType())));
            }
            return new FacetPage<>(results, facetResults, pageRequest, response.getHits().getTotalHits());
        } catch (final JsonSyntaxException | IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    private <T extends IIndexable> void saveReminder(SearchKey<T, T> searchKey, Pageable pageRequest, ICriterion crit,
            Sort sort, SearchResponse response) {
        if (response.getHits().getHits().length != 0) {
            // Store last sort value in order to use searchAfter next time
            Object[] sortValues = response.getHits().getAt(response.getHits().getHits().length - 1).getSortValues();
            OffsetDateTime expirationDate = OffsetDateTime.now().plus(KEEP_ALIVE_SCROLLING_TIME_MS, ChronoUnit.MILLIS);
            // Create a Reminder and save it into ES for next page
            SearchAfterReminder reminder = new SearchAfterReminder(crit, searchKey, sort, pageRequest.next());
            reminder.setExpirationDate(expirationDate);
            reminder.setSearchAfterSortValues(sortValues);

            save(REMINDER_IDX, reminder);
            // Create a task to be executed after KEEP_ALIVE_SCROLLING_TIME_MS that delete all reminders whom
            // expiration date has been reached
            reminderCleanExecutor
                    .schedule(() -> deleteByQuery(REMINDER_IDX, ICriterion.le("expirationDate", OffsetDateTime.now())),
                              KEEP_ALIVE_SCROLLING_TIME_MS, TimeUnit.MILLISECONDS);
        }
    }

    private <T extends IIndexable> Object[] advanceWithSearchAfter(ICriterion crit, SearchKey<T, T> searchKey,
            Pageable pageRequest, String index, Sort sort, QueryBuilder critBuilder) {
        try {
            Object[] sortValues = null;
            int searchPageNumber = 0;
            Pageable searchReminderPageRequest;
            if (indexExists(REMINDER_IDX)) {
                // First check existence of Reminder for exact given pageRequest from ES
                SearchAfterReminder reminder = new SearchAfterReminder(crit, searchKey, sort, pageRequest);
                reminder = get(REMINDER_IDX, reminder);
                if (reminder != null) {
                    LOGGER.debug("Found search after for offset {}", pageRequest.getOffset());
                    return reminder.getSearchAfterSortValues();
                }
                // Then check if a closer one exists (advance is done by MAX_RESULT_WINDOW steps so we must take this into
                // account)
                searchPageNumber =
                        (pageRequest.getOffset() - pageRequest.getOffset() % MAX_RESULT_WINDOW) / MAX_RESULT_WINDOW;
                while (searchPageNumber > 0) {
                    searchReminderPageRequest = new PageRequest(searchPageNumber, MAX_RESULT_WINDOW);
                    reminder = new SearchAfterReminder(crit, searchKey, sort, searchReminderPageRequest);
                    reminder = get(REMINDER_IDX, reminder);
                    // A reminder has been found ! Let's start from it
                    if (reminder != null) {
                        LOGGER.debug("Found search after for offset {}", searchReminderPageRequest.getOffset());
                        sortValues = reminder.getSearchAfterSortValues();
                        break;
                    }
                    searchPageNumber--;
                }
            }

            // No reminder found (first request or last one is too old) => advance to next to last page
            SearchRequest request = new SearchRequest(index).types(searchKey.getSearchTypes());
            // By default, launch request from 0 to 10_000 (without aggregations)...
            int offset = 0;
            int pageSize = MAX_RESULT_WINDOW;
            SearchSourceBuilder builder = new SearchSourceBuilder().query(critBuilder).from(offset).size(pageSize);
            manageSortRequest(index, builder, sort);
            request.source(builder);
            // ...Except if a closer reminder has already been found
            if (sortValues != null) {
                offset = searchPageNumber * MAX_RESULT_WINDOW;
            } else {
                LOGGER.debug("Search (after) : offset {}", offset);
                SearchResponse response = client.search(request);
                sortValues = response.getHits().getAt(response.getHits().getHits().length - 1).getSortValues();
                offset += MAX_RESULT_WINDOW;
            }
            OffsetDateTime expirationDate = OffsetDateTime.now().plus(KEEP_ALIVE_SCROLLING_TIME_MS, ChronoUnit.MILLIS);

            int nextToLastOffset = pageRequest.getOffset() - pageRequest.getOffset() % MAX_RESULT_WINDOW;
            // Execute as many request with search after as necessary to advance to next to last page of
            // MAX_RESULT_WINDOW size until offset
            while (offset < nextToLastOffset) {
                // Change offset
                LOGGER.debug("Search after : offset {}", offset);
                builder.from(0).searchAfter(sortValues);
                SearchResponse response = client.search(request);
                sortValues = response.getHits().getAt(response.getHits().getHits().length - 1).getSortValues();
                // Create a Reminder and save it into ES for next page
                SearchAfterReminder reminder = new SearchAfterReminder(crit, searchKey, sort,
                                                                       new PageRequest(offset / MAX_RESULT_WINDOW,
                                                                                       MAX_RESULT_WINDOW).next());
                reminder.setExpirationDate(expirationDate);
                reminder.setSearchAfterSortValues(
                        response.getHits().getAt(response.getHits().getHits().length - 1).getSortValues());

                save(REMINDER_IDX, reminder);
                offset += MAX_RESULT_WINDOW;
            }
            // Beware of offset that is a multiple of MAX_RESULT_WINDOW
            if (pageRequest.getOffset() != offset) {
                int size = pageRequest.getOffset() - offset;
                LOGGER.debug("Search after : offset {}, size {}", offset, size);
                builder.size(size).searchAfter(sortValues).from(0); // needed by searchAfter
                SearchResponse response = client.search(request);
                sortValues = response.getHits().getAt(response.getHits().getHits().length - 1).getSortValues();
            }

            // Create a task to be executed after KEEP_ALIVE_SCROLLING_TIME_MS that delete all reminders whom
            // expiration date has been reached
            reminderCleanExecutor
                    .schedule(() -> deleteByQuery(REMINDER_IDX, ICriterion.le("expirationDate", OffsetDateTime.now())),
                              KEEP_ALIVE_SCROLLING_TIME_MS, TimeUnit.MILLISECONDS);
            return sortValues;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build a SearchSourceBuilder following given ICriterion on searchKey with a result size of 0
     */
    private <T> SearchSourceBuilder createSourceBuilder4Agg(SearchKey<?, T> searchKey, ICriterion pCrit) {
        // Use filter instead of "direct" query (in theory, quickest because no score is computed)
        ICriterion crit = (pCrit == null) ? ICriterion.all() : pCrit;
        QueryBuilder critBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
                .filter(crit.accept(CRITERION_VISITOR));
        // Only return hits information
        return new SearchSourceBuilder().query(critBuilder).size(0);
    }

    @Override
    public <T extends IIndexable> Long count(SearchKey<?, T> searchKey, ICriterion pCrit) {
        try {
            SearchSourceBuilder builder = createSourceBuilder4Agg(searchKey, pCrit);
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(searchKey.getSearchTypes())
                    .source(builder);
            // Launch the request
            SearchResponse response = client.search(request);
            return response.getHits().getTotalHits();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends IIndexable> double sum(SearchKey<?, T> searchKey, ICriterion pCrit, String attName) {
        try {
            SearchSourceBuilder builder = createSourceBuilder4Agg(searchKey, pCrit);
            builder.aggregation(AggregationBuilders.sum(attName).field(attName));
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(searchKey.getSearchTypes())
                    .source(builder);
            // Launch the request
            SearchResponse response = client.search(request);
            return ((Sum) response.getAggregations().get(attName)).getValue();
        } catch (IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    @Override
    public <T extends IIndexable> OffsetDateTime minDate(SearchKey<?, T> searchKey, ICriterion pCrit, String attName) {
        try {
            SearchSourceBuilder builder = createSourceBuilder4Agg(searchKey, pCrit);
            builder.aggregation(AggregationBuilders.min(attName).field(attName));
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(searchKey.getSearchTypes())
                    .source(builder);
            // Launch the request
            SearchResponse response = client.search(request);
            Min min = response.getAggregations().get(attName);
            if ((min == null) || !Double.isFinite(min.getValue())) {
                return null;
            }
            return OffsetDateTimeAdapter.parse(min.getValueAsString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends IIndexable> OffsetDateTime maxDate(SearchKey<?, T> searchKey, ICriterion pCrit, String attName) {
        try {
            SearchSourceBuilder builder = createSourceBuilder4Agg(searchKey, pCrit);
            builder.aggregation(AggregationBuilders.max(attName).field(attName));
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(searchKey.getSearchTypes())
                    .source(builder);
            // Launch the request
            SearchResponse response = client.search(request);
            Max max = response.getAggregations().get(attName);
            if ((max == null) || !Double.isFinite(max.getValue())) {
                return null;
            }
            return OffsetDateTimeAdapter.parse(max.getValueAsString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Set<Object> unique(SearchKey<?, T> searchKey, ICriterion crit, String attName) {
        try {
            SearchSourceBuilder builder = createSourceBuilder4Agg(searchKey, crit);
            // Assuming no more than Integer.MAX_SIZE results will be returned
            builder.aggregation(AggregationBuilders.terms(attName).field(attName).size(Integer.MAX_VALUE));
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(searchKey.getSearchTypes())
                    .source(builder);
            // Launch the request
            SearchResponse response = client.search(request);
            Terms terms = response.getAggregations().get(attName);
            if (terms == null) {
                return Collections.emptySet();
            }
            Set<Object> results = new HashSet<>();
            for (Terms.Bucket bucket : terms.getBuckets()) {
                results.add(bucket.getKey());
            }
            return results;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Utility class to create a quadruple key (a pair of pairs) used by loading cache mechanism
     */
    private static class CacheKey extends Tuple<SearchKey<?, ?>, Tuple<ICriterion, String>> {

        public CacheKey(SearchKey<?, ?> searchKey, ICriterion v2, String v3) {
            super(searchKey, new Tuple<>(v2, v3));
        }

        public SearchKey<?, ?> getV1() {
            return v1();
        }

        public ICriterion getV2() {
            return v2().v1();
        }

        public String getV3() {
            return v2().v2();
        }
    }

    /**
     * SearchAll cache used by {@link EsRepository#searchAll} to avoid redo same ES request while changing page.
     * SortedSet is necessary to be sure several consecutive calls return same ordered set
     */
    private final LoadingCache<CacheKey, SortedSet<Object>> searchAllCache = CacheBuilder.newBuilder()
            .expireAfterAccess(TARGET_FORWARDING_CACHE_MN, TimeUnit.MINUTES)
            .build(new CacheLoader<CacheKey, SortedSet<Object>>() {

                @Override
                public SortedSet<Object> load(CacheKey key) throws Exception {
                    // Using method Objects.hashCode(Object) to compare to be sure that the set will always be returned
                    // with same order
                    SortedSet<Object> results = new TreeSet<>(Comparator.comparing(Objects::hashCode));
                    results.addAll(searchJoined(key.getV1(), key.getV2(), key.getV3()));
                    return results;
                }

                ;
            });

    @SuppressWarnings("unchecked")
    @Override
    public <R> List<R> search(SearchKey<?, R> searchKey, ICriterion criterion, String sourceAttribute) {
        try {
            SortedSet<Object> objects = searchAllCache
                    .getUnchecked(new CacheKey(searchKey, criterion, sourceAttribute));
            return objects.stream().map(o -> (R) o).collect(Collectors.toList());
        } catch (final JsonSyntaxException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, U> List<U> search(SearchKey<?, R> searchKey, ICriterion criterion, String sourceAttribute,
            Function<R, U> transformFct) {
        try {
            SortedSet<Object> objects = searchAllCache
                    .getUnchecked(new CacheKey(searchKey, criterion, sourceAttribute));
            return objects.stream().map(o -> (R) o).map(transformFct).collect(Collectors.toList());
        } catch (final JsonSyntaxException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> List<U> search(SearchKey<?, R[]> searchKey, ICriterion criterion, String sourceAttribute,
            Predicate<R> filterPredicate, Function<R, U> transformFct) {
        try {
            SortedSet<Object> objects = searchAllCache
                    .getUnchecked(new CacheKey(searchKey, criterion, sourceAttribute));
            return objects.stream().map(o -> (R) o).distinct().filter(filterPredicate).map(transformFct)
                    .collect(Collectors.toList());
        } catch (final JsonSyntaxException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    private static Map<String, Object> toMap(Object o) {
        return (Map<String, Object>) o;
    }

    /**
     * Is given attribute (can be a composite attribute like toto.titi) of type text from ES mapping ?
     * @param inIndex concerned index
     * @param type concerned type
     * @param attribute attribute from type
     * @return true or false
     */
    private boolean isTextMapping(String inIndex, String type, String attribute) throws IOException {
        String index = inIndex.toLowerCase();
        try {
            Response response = restClient
                    .performRequest("GET", index + "/_mapping/" + type + "/field/" + attribute, Collections.emptyMap());
            try (InputStream is = response.getEntity().getContent()) {
                Map<String, Object> map = XContentHelper.convertToMap(XContentType.JSON.xContent(), is, true);
                // If attribute exists, response should contain this chain of several maps :
                // <index>."mappings".<type>.<attribute>."mapping".<attribute_last_path>."type"
                if ((map != null) && !map.isEmpty()) {
                    // In cas attribute is toto.titi.tutu, we will need "tutu" further
                    String lastPathAtt = (attribute.contains(".") ?
                            attribute.substring(attribute.lastIndexOf('.') + 1) :
                            attribute);
                    return toMap(
                            toMap(toMap(toMap(toMap(toMap(map.get(index)).get("mappings")).get(type)).get(attribute))
                                          .get("mapping")).get(lastPathAtt)).get("type").equals("text");

                }
            }
        } catch (ResponseException e) {
            // In case index does not exist and/or mapping not available
            if (e.getResponse().getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return false;
            }
            throw e;
        }
        return false;
    }

    /**
     * Tell if given attribute is of type "text" from all types mappings of specified index.
     * @param map response of "mappings" rest request
     * @return true is first type mapping found fro given attribute is of type "text"
     */
    private static boolean isTextMapping(String index, Map<String, Object> map, String attribute) {
        String lastPathAttName = attribute.contains(".") ?
                attribute.substring(attribute.lastIndexOf('.') + 1) :
                attribute;
        try {
            // Mapping map contain only one value, the concerned index mapping BUT in case index is an alias, map key
            // is true index name, not alias one so DON'T retrieve mappinh from its name !!!
            Iterator<Object> i = map.values().iterator();
            if (i.hasNext()) {
                Map<String, Object> allTypesMapping = toMap(toMap(i.next()).get("mappings"));
                // Search from all types mapping if one contains asked attribute (frankly, all must contain it but maybe
                // automatic mapping isn't present for all
                for (Object oTypeMap : allTypesMapping.values()) {
                    Map<String, Object> typeMap = toMap(oTypeMap);
                    if (typeMap.containsKey(attribute)) {
                        return toMap(toMap(toMap(typeMap.get(attribute)).get("mapping")).get(lastPathAttName)).get("type")
                                .equals("text");
                    }
                }
            }
            return false; // NOSONAR
        } catch (NullPointerException e) { // NOSONAR (better catch a NPE than testing all imbricated maps)
            return false;
        }
    }

    /**
     * Add sort to the request
     * @param builder search request
     * @param sort map(attribute name, true if ascending)
     */
    private void manageSortRequest(String index, SearchSourceBuilder builder, Sort sort) throws IOException {
        // Convert Sort into linked hash map
        LinkedHashMap<String, Boolean> ascSortMap = new SortToLinkedHashMap().convert(sort);

        // Because string attributes are not indexed with Elasticsearch, it is necessary to add ".keyword" at
        // end of attribute name into sort request. So we need to know string attributes
        Response response = restClient
                .performRequest("GET", index + "/_mapping" + "/field/" + Joiner.on(",").join(ascSortMap.keySet()),
                                Collections.emptyMap());
        try (InputStream is = response.getEntity().getContent()) {
            Map<String, Object> map = XContentHelper.convertToMap(XContentType.JSON.xContent(), is, true);
            if ((map != null) && !map.isEmpty()) {
                // All types mappings are retrieved.
                // NOTE: in our context, attributes have same metada for all types so once we found one, we stop.
                // To do that, we create a new LinkedHashMap to KEEPS keys order !!! (crucial)
                LinkedHashMap<String, Boolean> updatedAscSortMap = new LinkedHashMap<>(ascSortMap.size());
                for (Map.Entry<String, Boolean> sortEntry : ascSortMap.entrySet()) {
                    String attribute = sortEntry.getKey();
                    if (isTextMapping(index, map, attribute)) {
                        updatedAscSortMap.put(attribute + ".keyword", sortEntry.getValue());
                    } else {
                        updatedAscSortMap.put(attribute, sortEntry.getValue());
                    }
                }

                // Add sort to request
                updatedAscSortMap.entrySet().forEach(
                        entry -> builder.sort(entry.getKey(), entry.getValue() ? SortOrder.ASC : SortOrder.DESC));
            }
        }
    }

    /**
     * Add aggregations to the search request.
     * @param pFacetsMap asked facets
     * @param builder search request
     * @return true if a second pass is needed (managing range facets)
     */
    private boolean manageFirstPassRequestAggregations(Map<String, FacetType> pFacetsMap, SearchSourceBuilder builder) {
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
                    // Add min aggregation and max aggregagtion when a range aggregagtion is asked for (NUMERIC and DATE
                    // facets leed to range aggregagtion at second pass) to avoid ranges with Infinties values
                    builder.aggregation(FacetType.MIN.accept(aggBuilderFacetTypeVisitor, entry.getKey()));
                    builder.aggregation(FacetType.MAX.accept(aggBuilderFacetTypeVisitor, entry.getKey()));

                    twoPassRequestNeeded = true;
                }
                builder.aggregation(facetType.accept(aggBuilderFacetTypeVisitor, entry.getKey()));
            }
        }
        return twoPassRequestNeeded;
    }

    /**
     * Add aggregations to the search request (second pass). For range aggregations, use percentiles results (from first pass request results) to create range aggregagtions.
     * @param pFacetsMap asked facets
     * @param builder search request
     * @param aggsMap first pass aggregagtions results
     */
    private void manageSecondPassRequestAggregations(Map<String, FacetType> pFacetsMap, SearchSourceBuilder builder,
            Map<String, Aggregation> aggsMap) {
        for (Map.Entry<String, FacetType> entry : pFacetsMap.entrySet()) {
            FacetType facetType = entry.getValue();
            String attributeName = entry.getKey();
            String attName;
            // Replace percentiles aggregations by range aggregations
            if ((facetType == FacetType.NUMERIC) || (facetType == FacetType.DATE)) {
                attName = (facetType == FacetType.NUMERIC) ?
                        attributeName + NUMERIC_FACET_SUFFIX :
                        attributeName + DATE_FACET_SUFFIX;
                Percentiles percentiles = (Percentiles) aggsMap.get(attName);
                // No percentile values for this property => skip aggregation
                if (Iterables.all(percentiles, p -> Double.isNaN(p.getValue()))) {
                    continue;
                }
                AggregationBuilder aggBuilder = (facetType == FacetType.NUMERIC) ?
                        FacetType.RANGE_DOUBLE.accept(aggBuilderFacetTypeVisitor, attributeName, percentiles) :
                        FacetType.RANGE_DATE.accept(aggBuilderFacetTypeVisitor, attributeName, percentiles);
                // In case range contains only one value, better remove facet
                if (aggBuilder != null) {
                    // And add max and min aggregagtions
                    builder.aggregation(aggBuilder);
                    // And add max and min aggregagtions
                    builder.aggregation(FacetType.MIN.accept(aggBuilderFacetTypeVisitor, attributeName));
                    builder.aggregation(FacetType.MAX.accept(aggBuilderFacetTypeVisitor, attributeName));
                }
            } else { // Let others as it
                builder.aggregation(facetType.accept(aggBuilderFacetTypeVisitor, attributeName));
            }
        }
    }

    /**
     * Compute aggregations results to fill results facet map of an attribute
     * @param aggsMap aggregation resuls map
     * @param facets map of results facets
     * @param facetType type of facet for given attribute
     * @param attributeName given attribute
     */

    private void fillFacets(Map<String, Aggregation> aggsMap, Set<IFacet<?>> facets, FacetType facetType,
            String attributeName) {
        switch (facetType) {
            case STRING: {
                Terms terms = (Terms) aggsMap
                        .get(attributeName + AggregationBuilderFacetTypeVisitor.STRING_FACET_SUFFIX);
                if (terms.getBuckets().isEmpty()) {
                    return;
                }
                Map<String, Long> valueMap = new LinkedHashMap<>(terms.getBuckets().size());
                terms.getBuckets().forEach(b -> valueMap.put(b.getKeyAsString(), b.getDocCount()));
                facets.add(new StringFacet(attributeName, valueMap));
                break;
            }
            case NUMERIC: {
                org.elasticsearch.search.aggregations.bucket.range.Range numRange = (org.elasticsearch.search.aggregations.bucket.range.Range) aggsMap
                        .get(attributeName + AggregationBuilderFacetTypeVisitor.RANGE_FACET_SUFFIX);
                if (numRange != null) {
                    // Retrieve min and max aggregagtions to replace -Infinity and +Infinity
                    Min min = (Min) aggsMap.get(attributeName + AggregationBuilderFacetTypeVisitor.MIN_FACET_SUFFIX);
                    Max max = (Max) aggsMap.get(attributeName + AggregationBuilderFacetTypeVisitor.MAX_FACET_SUFFIX);
                    // Parsing ranges
                    Map<Range<Double>, Long> valueMap = new LinkedHashMap<>();
                    for (Bucket bucket : numRange.getBuckets()) {
                        // Case with no value : every bucket has a NaN value (as from, to or both)
                        if (Objects.equals(bucket.getTo(), Double.NaN) || Objects
                                .equals(bucket.getFrom(), Double.NaN)) {
                            // If first bucket contains NaN value, it means there are no value at all
                            return;
                        }
                        Range<Double> valueRange;
                        // (-∞ -> ?
                        if (Objects.equals(bucket.getFrom(), Double.NEGATIVE_INFINITY)) {
                            // (-∞ -> +∞) (completely dumb but...who knows ?)
                            if (Objects.equals(bucket.getTo(), Double.POSITIVE_INFINITY)) {
                                // Better not return a facet
                                return;
                            } // (-∞ -> value [
                            // range is then [min -> value [
                            valueRange = Range.closedOpen(EsHelper.scaled(min.getValue()), (Double) bucket.getTo());
                        } else if (Objects.equals(bucket.getTo(), Double.POSITIVE_INFINITY)) { // [value -> +∞)
                            // range is then [value, max]
                            valueRange = Range.closed((Double) bucket.getFrom(), EsHelper.scaled(max.getValue()));
                        } else { // [value -> value [
                            valueRange = Range.closedOpen((Double) bucket.getFrom(), (Double) bucket.getTo());
                        }
                        valueMap.put(valueRange, bucket.getDocCount());
                    }
                    facets.add(new NumericFacet(attributeName, valueMap));
                }
                break;
            }
            case DATE: {
                org.elasticsearch.search.aggregations.bucket.range.Range dateRange = (org.elasticsearch.search.aggregations.bucket.range.Range) aggsMap
                        .get(attributeName + AggregationBuilderFacetTypeVisitor.RANGE_FACET_SUFFIX);
                if (dateRange != null) {
                    Map<com.google.common.collect.Range<OffsetDateTime>, Long> valueMap = new LinkedHashMap<>();
                    for (Bucket bucket : dateRange.getBuckets()) {
                        // Retrieve min and max aggregagtions to replace -Infinity and +Infinity
                        Min min = (Min) aggsMap
                                .get(attributeName + AggregationBuilderFacetTypeVisitor.MIN_FACET_SUFFIX);
                        Max max = (Max) aggsMap
                                .get(attributeName + AggregationBuilderFacetTypeVisitor.MAX_FACET_SUFFIX);
                        // Parsing ranges
                        Range<OffsetDateTime> valueRange;
                        // Case with no value : every bucket has a NaN value (as from, to or both)
                        if (Objects.equals(bucket.getTo(), Double.NaN) || Objects
                                .equals(bucket.getFrom(), Double.NaN)) {
                            // If first bucket contains NaN value, it means there are no value at all
                            return;
                        }
                        // (-∞ -> ?
                        if (bucket.getFromAsString() == null) {
                            // (-∞ -> +∞) (completely dumb but...who knows ?)
                            if (bucket.getToAsString() == null) {
                                // range is then [min, max]
                                valueRange = Range.closed(OffsetDateTimeAdapter.parse(min.getValueAsString()),
                                                          OffsetDateTimeAdapter.parse(max.getValueAsString()));
                            } else { // (-∞ -> value[
                                // range is then [min, value[
                                valueRange = Range.closedOpen(OffsetDateTimeAdapter.parse(min.getValueAsString()),
                                                              OffsetDateTimeAdapter.parse(bucket.getToAsString()));
                            }
                        } else if (bucket.getToAsString() == null) { // [value -> +∞)
                            // range is then [value, max ]
                            valueRange = Range.closed(OffsetDateTimeAdapter.parse(bucket.getFromAsString()),
                                                      OffsetDateTimeAdapter.parse(max.getValueAsString()));
                        } else { // [value -> value[
                            valueRange = Range.closedOpen(OffsetDateTimeAdapter.parse(bucket.getFromAsString()),
                                                          OffsetDateTimeAdapter.parse(bucket.getToAsString()));
                        }
                        valueMap.put(valueRange, bucket.getDocCount());
                    }
                    facets.add(new DateFacet(attributeName, valueMap));
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public <T> Page<T> multiFieldsSearch(SearchKey<T, T> searchKey, Pageable pPageRequest, Object pValue,
            String... pFields) {
        try {
            final List<T> results = new ArrayList<>();
            // OffsetDateTime must be formatted to be correctly used following Gson mapping
            Object value = (pValue instanceof OffsetDateTime) ?
                    OffsetDateTimeAdapter.format((OffsetDateTime) pValue) :
                    pValue;
            QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
                    .filter(QueryBuilders.multiMatchQuery(value, pFields));
            SearchSourceBuilder builder = new SearchSourceBuilder().query(queryBuilder).from(pPageRequest.getOffset())
                    .size(pPageRequest.getPageSize());
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(searchKey.getSearchTypes())
                    .source(builder);

            SearchResponse response = client.search(request);
            SearchHits hits = response.getHits();
            for (SearchHit hit : hits) {
                results.add(gson.fromJson(hit.getSourceAsString(), searchKey.fromType(hit.getType())));
            }
            return new PageImpl<>(results, pPageRequest, response.getHits().getTotalHits());
        } catch (final JsonSyntaxException | IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    @Override
    public <T extends IIndexable & IDocFiles> DocFilesSummary computeDataFilesSummary(SearchKey<T, T> searchKey,
            ICriterion crit, String discriminantProperty, String... fileTypes) {
        try {
            if ((fileTypes == null) || (fileTypes.length == 0)) {
                throw new IllegalArgumentException("At least on file type must be provided");
            }
            SearchSourceBuilder builder = createSourceBuilder4Agg(searchKey, crit);
            // Add aggregations to manage compute summary
            // First "global" aggregations on each asked file types
            for (String fileType : fileTypes) {
                // file count
                builder.aggregation(AggregationBuilders.count("total_" + fileType + "_files_count")
                                            .field("files." + fileType + ".size")); // Only count files with a size
                // file size sum
                builder.aggregation(AggregationBuilders.sum("total_" + fileType + "_files_size")
                                            .field("files." + fileType + ".size"));
            }
            // Then bucket aggregation by discriminants
            String termsFieldProperty = discriminantProperty;
            if (isTextMapping(searchKey.getSearchIndex(), searchKey.getSearchTypes()[0], discriminantProperty)) {
                termsFieldProperty += ".keyword";
            }
            // Discriminant distribution aggregator
            TermsAggregationBuilder termsAggBuilder = AggregationBuilders.terms(discriminantProperty)
                    .field(termsFieldProperty).size(Integer.MAX_VALUE);
            // and "total" aggregations on each asked file types
            for (String fileType : fileTypes) {
                // files count
                termsAggBuilder.subAggregation(AggregationBuilders.count(fileType + "_files_count")
                                                       .field("files." + fileType + ".size"));
                // file size sum
                termsAggBuilder.subAggregation(
                        AggregationBuilders.sum(fileType + "_files_size").field("files." + fileType + ".size"));
            }
            builder.aggregation(termsAggBuilder);

            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(searchKey.getSearchTypes())
                    .source(builder);
            // Launch the request
            SearchResponse response = client.search(request);
            DocFilesSummary summary = new DocFilesSummary();
            // First "global" aggregations results
            summary.setDocumentsCount(response.getHits().getTotalHits());
            Aggregations aggs = response.getAggregations();
            long totalFileCount = 0;
            long totalFileSize = 0;
            for (String fileType : fileTypes) {
                ValueCount valueCount = aggs.get("total_" + fileType + "_files_count");
                totalFileCount += valueCount.getValue();
                Sum sum = aggs.get("total_" + fileType + "_files_size");
                totalFileSize += sum.getValue();
            }
            summary.setFilesCount(totalFileCount);
            summary.setFilesSize(totalFileSize);
            // Then discriminants buckets aggregations results
            Terms buckets = aggs.get(discriminantProperty);
            for (Terms.Bucket bucket : buckets.getBuckets()) {
                String discriminant = bucket.getKeyAsString();
                DocFilesSubSummary discSummary = new DocFilesSubSummary();
                discSummary.setDocumentsCount(bucket.getDocCount());
                Aggregations discAggs = bucket.getAggregations();
                long filesCount = 0;
                long filesSize = 0;
                for (String fileType : fileTypes) {
                    ValueCount valueCount = discAggs.get(fileType + "_files_count");
                    filesCount += valueCount.getValue();
                    Sum sum = discAggs.get(fileType + "_files_size");
                    filesSize += sum.getValue();
                    discSummary.getFileTypesSummaryMap()
                            .put(fileType, new FilesSummary(valueCount.getValue(), (long) sum.getValue()));
                }
                discSummary.setFilesCount(filesCount);
                discSummary.setFilesSize(filesSize);
                summary.getSubSummariesMap().put(discriminant, discSummary);
            }
            return summary;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
