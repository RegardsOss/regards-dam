/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import static fr.cnes.regards.modules.indexer.dao.builder.AggregationBuilderFacetTypeVisitor.DATE_FACET_SUFFIX;
import static fr.cnes.regards.modules.indexer.dao.builder.AggregationBuilderFacetTypeVisitor.NUMERIC_FACET_SUFFIX;
import static fr.cnes.regards.modules.indexer.dao.spatial.GeoHelper.AUTHALIC_SPHERE_RADIUS;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions.Type;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
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
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.range.Range.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.hipparchus.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.framework.module.rest.exception.TooManyResultsException;
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.feature.DataObjectFeature;
import fr.cnes.regards.modules.indexer.dao.builder.AggregationBuilderFacetTypeVisitor;
import fr.cnes.regards.modules.indexer.dao.builder.GeoCriterionWithCircleVisitor;
import fr.cnes.regards.modules.indexer.dao.builder.GeoCriterionWithPolygonOrBboxVisitor;
import fr.cnes.regards.modules.indexer.dao.builder.QueryBuilderCriterionVisitor;
import fr.cnes.regards.modules.indexer.dao.converter.SortToLinkedHashMap;
import fr.cnes.regards.modules.indexer.dao.spatial.GeoHelper;
import fr.cnes.regards.modules.indexer.domain.IDocFiles;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.SearchKey;
import fr.cnes.regards.modules.indexer.domain.aggregation.QueryableAttribute;
import fr.cnes.regards.modules.indexer.domain.criterion.CircleCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.PolygonCriterion;
import fr.cnes.regards.modules.indexer.domain.facet.BooleanFacet;
import fr.cnes.regards.modules.indexer.domain.facet.DateFacet;
import fr.cnes.regards.modules.indexer.domain.facet.FacetType;
import fr.cnes.regards.modules.indexer.domain.facet.IFacet;
import fr.cnes.regards.modules.indexer.domain.facet.NumericFacet;
import fr.cnes.regards.modules.indexer.domain.facet.StringFacet;
import fr.cnes.regards.modules.indexer.domain.reminder.SearchAfterReminder;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;
import fr.cnes.regards.modules.indexer.domain.spatial.ILocalizable;
import fr.cnes.regards.modules.indexer.domain.summary.DocFilesSubSummary;
import fr.cnes.regards.modules.indexer.domain.summary.DocFilesSummary;
import fr.cnes.regards.modules.indexer.domain.summary.FilesSummary;

/**
 * Elasticsearch repository implementation
 * @author oroussel
 */
@Repository
public class EsRepository implements IEsRepository {

    /**
     * Utility class to create a quadruple key used by loading cache mechanism (yes, it's a lazy how to)
     */
    private static class CacheKey
            extends Tuple<Tuple<SearchKey<?, ?>, Map<String, FacetType>>, Tuple<ICriterion, String>> {

        public CacheKey(SearchKey<?, ?> searchKey, ICriterion v2, String v3) {
            super(new Tuple<>(searchKey, new HashMap<>()), new Tuple<>(v2, v3));
        }

        public CacheKey(SearchKey<?, ?> searchKey, ICriterion v2, String v3, Map<String, FacetType> facetsMap) {
            super(new Tuple<>(searchKey, facetsMap), new Tuple<>(v2, v3));
        }

        public SearchKey<?, ?> getSearchKey() {
            return v1().v1();
        }

        public ICriterion getCriterion() {
            return v2().v1();
        }

        public String getSourceAttribute() {
            return v2().v2();
        }

        public Map<String, FacetType> getFacetsMap() {
            return v1().v2();
        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EsRepository.class);

    /**
     * Scrolling keeping alive Time in ms when searching into Elasticsearch
     * Set it to 10 minutes to avoid timeouts while scrolling.
     */
    private static final int KEEP_ALIVE_SCROLLING_TIME_MN = 10;

    /**
     * Default number of hits retrieved by scrolling (10 is the default value and according to doc is the best value)
     */
    private static final int DEFAULT_SCROLLING_HITS_SIZE = 100;

    /**
     * Target forwarding search {@link EsRepository#searchAll} need to put in cache search because of pagination
     * restrictions. This constant specifies duration cache time in minutes (from last access)
     */
    private static final int TARGET_FORWARDING_CACHE_MN = 3;

    /**
     * QueryBuilder visitor used for Elasticsearch search requests
     */
    private static final QueryBuilderCriterionVisitor CRITERION_VISITOR = new QueryBuilderCriterionVisitor();

    /**
     * Index used to store search after cursors
     */
    private static final String REMINDER_IDX = "reminder";

    /**
     * Suffix for text attributes
     */
    private static final String KEYWORD_SUFFIX = ".keyword";

    /**
     * From Elasticsearch 6.0, a single type is allowed, its name is (by now) "_doc"
     */
    private static final String TYPE = "_doc";

    /**
     * Single scheduled executor service to clean reminder tasks once expiration date is reached
     */
    private final ScheduledExecutorService reminderCleanExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * AggregationBuilder visitor used for Elasticsearch search requests with facets
     */
    @Autowired
    private final AggregationBuilderFacetTypeVisitor aggBuilderFacetTypeVisitor;

    /**
     * High level Rest API client
     */
    private final RestHighLevelClient client;

    /**
     * Json mapper
     */
    private final Gson gson;

    /**
     * SearchAll cache used by {@link EsRepository#searchAll} to avoid redo same ES request while changing page.
     * SortedSet is necessary to be sure several consecutive calls return same ordered set
     */
    private final LoadingCache<CacheKey, Tuple<SortedSet<Object>, Set<IFacet<?>>>> searchAllCache = CacheBuilder
            .newBuilder().expireAfterAccess(TARGET_FORWARDING_CACHE_MN, TimeUnit.MINUTES)
            .build(new CacheLoader<CacheKey, Tuple<SortedSet<Object>, Set<IFacet<?>>>>() {

                @Override
                public Tuple<SortedSet<Object>, Set<IFacet<?>>> load(CacheKey key) throws Exception {
                    // Using method Objects.hashCode(Object) to compare to be sure that the set will always be returned
                    // with same order
                    //                    Tuple<SortedSet<Object>, Set<IFacet<?>>> results = new TreeSet<>(Comparator.comparing(Objects::hashCode));
                    //                    results.addAll(searchJoined(key.getSearchKey(),
                    //                                                key.getCriterion(),
                    //                                                key.getSourceAttribute(),
                    //                                                key.getFacetsMap()));
                    //                    return results;
                    return searchJoined(key.getSearchKey(), key.getCriterion(), key.getSourceAttribute(),
                                        key.getFacetsMap());
                }
            });

    /**
     * Constructor
     * @param gson JSon mapper bean
     */
    public EsRepository(@Autowired Gson gson, @Value("${regards.elasticsearch.host:}") String inEsHost,
            @Value("${regards.elasticsearch.address:}") String inEsAddress,
            @Value("${regards.elasticsearch.http.port}") int esPort,
            AggregationBuilderFacetTypeVisitor aggBuilderFacetTypeVisitor) {

        this.gson = gson;
        String esHost = Strings.isEmpty(inEsHost) ? inEsAddress : inEsHost;
        this.aggBuilderFacetTypeVisitor = aggBuilderFacetTypeVisitor;

        String connectionInfoMessage = String.format("Elastic search connection properties : host \"%s\", port \"%d\"",
                                                     esHost, esPort);
        LOGGER.info(connectionInfoMessage);

        // Timeouts are set to 20 minutes particulary for bulk save containing geo_shape
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(esHost, esPort))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setSocketTimeout(1_200_000))
                .setMaxRetryTimeoutMillis(1_200_000);

        client = new RestHighLevelClient(restClientBuilder);

        try {
            // Testing availability of ES
            if (!client.ping(RequestOptions.DEFAULT)) {
                throw new NoNodeAvailableException("Elasticsearch is down. " + connectionInfoMessage);
            }
        } catch (IOException | RuntimeException e) {
            throw new NoNodeAvailableException("Error while pinging Elasticsearch (" + connectionInfoMessage + ")", e);
        }
    }

    /**
     * Add document type (Elasticsearch prior to version 6 type) into criterion
     */
    private static ICriterion addTypes(ICriterion criterion, String... types) {
        // Beware if crit is null
        criterion = criterion == null ? ICriterion.all() : criterion;
        // Then add type
        switch (types.length) {
            case 0:
                return criterion;
            case 1:
                return ICriterion.and(ICriterion.eq("type", types[0]), criterion);
            default:
                ICriterion orCrit = ICriterion
                        .or(Arrays.stream(types).map(type -> ICriterion.eq("type", type)).toArray(ICriterion[]::new));
                return ICriterion.and(orCrit, criterion);
        }

    }

    /**
     * If you think this method is horrible and completely useless (and bordel line) look at next method...
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> toMap(Object o) {
        return (Map<String, Object>) o;
    }

    /**
     * Tell if given attribute is of type "text" from all types mappings of specified index.
     * @param map response of "mappings" rest request
     * @return true is first type mapping found fro given attribute is of type "text"
     */
    private static boolean isTextMapping(Map<String, Object> map, String attribute) {
        String lastPathAttName = attribute.contains(".") ? attribute.substring(attribute.lastIndexOf('.') + 1)
                : attribute;
        try {
            // Mapping map contain only one value, the concerned index mapping BUT in case index is an alias, map key
            // is true index name, not alias one so DON'T retrieve mapping from its name !!!
            Iterator<Object> i = map.values().iterator();
            if (i.hasNext()) {
                Map<String, Object> allTypesMapping = toMap(toMap(i.next()).get("mappings"));
                // Search from all types mapping if one contains asked attribute (frankly, all must contain it but maybe
                // automatic mapping isn't present for all
                // Since ES6, single type so this loop isn't really one
                for (Object oTypeMap : allTypesMapping.values()) {
                    Map<String, Object> typeMap = toMap(oTypeMap);
                    if (typeMap.containsKey(attribute)) {
                        return toMap(toMap(toMap(typeMap.get(attribute)).get("mapping")).get(lastPathAttName))
                                .get("type").equals("text");
                    }
                }
            }
            return false;
        } catch (NullPointerException e) { // NOSONAR (better catch a NPE than testing all imbricated maps)
            return false;
        }
    }

    @Override
    public void close() {
        LOGGER.info("Closing connection");
        try {
            client.close();
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public boolean createIndex(String index) {
        try {
            CreateIndexRequest request = Requests.createIndexRequest(index.toLowerCase());
            // mapping (properties)
            request.mapping(TYPE, XContentFactory.jsonBuilder().startObject() // NOSONAR
                    // XContentFactory.jsonBuilder() of type XContentBuilder is closed by request.mapping()
                    // Automatic double mapping (dynamic_templates)
                    .startArray("dynamic_templates").startObject().startObject("doubles")
                    .field("match_mapping_type", "double").startObject("mapping").field("type", "double").endObject()
                    .endObject().endObject().endArray()
                    // Properties mapping for geometry and type
                    .startObject("properties")
                    // _doc is now this unique type => add a "type" property containing previous type
                    .startObject("type").field("type", "keyword").endObject()
                    // Geometry mapping (field is wgs84 even if two over fields contain geometry, they
                    // are not mapped as geo_shape, they only bring informations)
                    .startObject("wgs84").field("type", "geo_shape") // default
                    // With geohash
                    .field("tree", "geohash") // precison = 11 km Astro test 13s to fill constellations
                    // .field("tree_levels", "5") // precision = 3.5 km Astro test 19 s to fill constellations
                    // .field("tree_levels", "6") // precision = 3.5 km Astro test 41 s to fill constellations
                    // .field("tree_levels", "7") // precision = 111 m Astro test 2 mn to fill constellations
                    // .field("tree_levels", "8") // precision = 111 m Astro test 13 mn to fill constellations

                    // With quadtree
                    //                                    .field("tree", "quadtree") // precison = 11 km Astro test 10s to fill constellations
                    // .field("tree_levels", "20") // precison = 16 m Astro test 7 mn to fill constellations
                    // .field("tree_levels", "21") // precision = 7m Astro test 17mn to fill constellations

                    .endObject().endObject().endObject());
            CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public boolean createAlias(String index, String alias) {
        try {
            IndicesAliasesRequest request = new IndicesAliasesRequest();
            AliasActions createAliasAction = new AliasActions(Type.ADD).index(index.toLowerCase())
                    .alias(alias.toLowerCase());
            request.addAliasAction(createAliasAction);
            AcknowledgedResponse response = client.indices().updateAliases(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public boolean setSettingsForBulk(String index) {
        try {
            UpdateSettingsRequest request = Requests.updateSettingsRequest(index.toLowerCase());
            Settings.Builder builder = Settings.builder().put("index.refresh_interval", -1)
                    .put("index.number_of_replicas", 0);
            request.settings(builder);
            AcknowledgedResponse response = client.indices().putSettings(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }

    }

    @Override
    public boolean unsetSettingsForBulk(String index) {
        try {
            UpdateSettingsRequest request = Requests.updateSettingsRequest(index.toLowerCase());
            Settings.Builder builder = Settings.builder().put("index.refresh_interval", "1s")
                    .put("index.number_of_replicas", 1);
            request.settings(builder);
            AcknowledgedResponse response = client.indices().putSettings(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }

    }

    @Override
    public boolean indexExists(String name) {
        try {
            GetIndexRequest request = new GetIndexRequest();
            request.indices(name.toLowerCase());
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public boolean deleteIndex(String inIndex) throws IndexNotFoundException {
        String index = inIndex.toLowerCase();
        try {
            return deleteIndex0(index);
        } catch (RsRuntimeException e) {
            if (Throwables.getRootCause(e) instanceof ElasticsearchException) {
                ElasticsearchException ee = (ElasticsearchException) Throwables.getRootCause(e);
                // It's probably because index is an alias
                if (ee.status() == RestStatus.BAD_REQUEST) {
                    try {
                        GetAliasesRequest request = new GetAliasesRequest(index);
                        GetAliasesResponse response = client.indices().getAlias(request, RequestOptions.DEFAULT);
                        // There should be only one index (an alias cannot be multi-index...i assume it)
                        if (response.getAliases().size() == 1) {
                            Map.Entry<String, Set<AliasMetaData>> entry = response.getAliases().entrySet().iterator()
                                    .next();
                            // Given alias must be the only one for this index
                            if (entry.getValue().size() == 1) {
                                // Now it is ok to delete index (concerned alias is necessarily asked one)
                                return deleteIndex0(entry.getKey());
                            }
                        }
                        return false;
                    } catch (Exception e1) { // NOSONAR (let's focus on first exception)
                        // Ok, let it go, go back to first exception
                        throw e;
                    }
                } else {
                    throw e;
                }
            } else {
                throw e;
            }
        }
    }

    private boolean deleteIndex0(String index) throws IndexNotFoundException {
        try {
            DeleteIndexRequest request = Requests.deleteIndexRequest(index);
            AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                throw new IndexNotFoundException(index.toLowerCase());
            }
            throw new RsRuntimeException(e);
        } catch (IOException e) {
            throw new RsRuntimeException(e);
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
            try {
                Request request = new Request("POST", "/" + index.toLowerCase() + "/_delete_by_query");
                request.setEntity(entity);
                Response response = client.getLowLevelClient().performRequest(request);
                try (InputStream is = response.getEntity().getContent()) {
                    Map<String, Object> map = XContentHelper.convertToMap(XContentType.JSON.xContent(), is, true);
                    return ((Number) map.get("deleted")).longValue();
                }
            } finally {
                ((NStringEntity) entity).close();
            }
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public Collection<String> upgradeAllIndices4SingleType() {
        List<String> newIndices = new ArrayList<>();
        try {
            GetIndexRequest request = new GetIndexRequest();
            request.indices("*");
            GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
            for (ObjectObjectCursor<String, Settings> settings : response.getSettings()) {
                // index starting with . are kibana ones (don't give a shit)
                if (!settings.key.startsWith(".")) {
                    String ver = settings.value.getAsSettings("index").getAsSettings("version").get("created");
                    Version version = Version.fromId(Integer.parseInt(ver));
                    if (version.before(Version.V_6_0_0)) {
                        long start = System.currentTimeMillis();
                        LOGGER.info("Upgrading index {}:v{}...", settings.key, version);
                        try {
                            // Reindex
                            LOGGER.info("Reindexing...");
                            String newIndex = reindex(settings.key);
                            if (newIndex != null) {
                                LOGGER.info("...Reindexing from {} to {} OK", settings.key, newIndex);
                                newIndices.add(newIndex);
                                // Delete old index
                                LOGGER.info("Deleting old index {}...", settings.key);
                                if (deleteIndex(settings.key)) {
                                    LOGGER.info("...Deletion OK");
                                } else {
                                    LOGGER.warn("...Deletion NOK, still try to create alias");
                                }
                                // Create alias with old name for new index
                                LOGGER.error("Creating alias {} for {}...", newIndex, settings.key);
                                if (createAlias(newIndex, settings.key)) {
                                    LOGGER.info("...Alias created");
                                } else {
                                    LOGGER.warn("...Alias not created, please analyze the problem and try by hand");
                                }
                            }
                            LOGGER.info("...Upgrade OK ({} ms)", System.currentTimeMillis() - start);
                        } catch (Exception e) { // NOSONAR (let's continue on next index whatever problem occured)
                            LOGGER.error(String.format("Cannot upgrade %s", settings.key), e);
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
        return newIndices;
    }

    @Override
    public String reindex(String index) throws IOException {
        String newIndex = index.toLowerCase() + "_" + Version.CURRENT.major;
        if (this.createIndex(newIndex)) {
            // Reindex
            String requestStr = String.format("{  \"source\": {    \"index\": \"%s\"  },  \"dest\": {"
                    + "    \"index\": \"%s\"  },  \"script\": {"
                    + "    \"source\": \"      ctx._source.type = ctx._type;" + "      ctx._type = '_doc';    \"  }}",
                                              index, newIndex);
            HttpEntity entity = new NStringEntity(requestStr, ContentType.APPLICATION_JSON);
            try {
                Request request = new Request("POST", "_reindex");
                request.setEntity(entity);
                Response response = client.getLowLevelClient().performRequest(request);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return newIndex;
                } else {
                    LOGGER.error(response.getStatusLine().toString());
                }
            } finally {
                ((NStringEntity) entity).close();
            }
        }
        return null;
    }

    @Override
    public void refresh(String index) {
        // To make just saved documents searchable, the associated index must be refreshed
        try {
            RefreshRequest request = Requests.refreshRequest(index.toLowerCase());
            client.indices().refresh(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public <T extends IIndexable> T get(String index, String type, String id, Class<T> clazz) {
        GetRequest request = new GetRequest(index.toLowerCase(), TYPE, id);
        try {
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            if (!response.isExists()) {
                return null;
            }
            return gson.fromJson(response.getSourceAsString(), clazz);
        } catch (final JsonSyntaxException | IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public boolean delete(String index, String type, String id) {
        DeleteRequest request = new DeleteRequest(index.toLowerCase(), TYPE, id);
        try {
            DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
            return (response.getResult() == Result.DELETED) || (response.getResult() == Result.NOT_FOUND);
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    private void checkDocument(IIndexable doc) {
        if (Strings.isNullOrEmpty(doc.getDocId()) || Strings.isNullOrEmpty(doc.getType())) {
            throw new IllegalArgumentException("docId and type are mandatory on an IIndexable object");
        }
    }

    @Override
    public boolean save(String index, IIndexable doc) {
        checkDocument(doc);
        try {
            IndexResponse response = client.index(
                                                  new IndexRequest(index.toLowerCase(), TYPE, doc.getDocId())
                                                          .source(gson.toJson(doc), XContentType.JSON),
                                                  RequestOptions.DEFAULT);
            return response.getResult() == Result.CREATED; // Else UPDATED
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public <T extends IIndexable> BulkSaveResult saveBulk(String inIndex, BulkSaveResult bulkSaveResult,
            StringBuilder errorBuffer, @SuppressWarnings("unchecked") T... documents) {
        try {
            // Use existing one or create
            BulkSaveResult result = bulkSaveResult == null ? new BulkSaveResult() : bulkSaveResult;
            if (documents.length == 0) {
                return result;
            }
            String index = inIndex.toLowerCase();
            // Check mandatory properties (as docId, type, ...)
            for (T doc : documents) {
                checkDocument(doc);
            }
            // Create Save bulk request
            BulkRequest bulkRequest = new BulkRequest();
            Map<String, T> map = new HashMap<>();
            for (T doc : documents) {
                bulkRequest
                        .add(new IndexRequest(index, TYPE, doc.getDocId()).source(gson.toJson(doc), XContentType.JSON));
                map.put(doc.getDocId(), doc);
            }
            // Bulk save
            BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            // Parse response to creata a more exploitable object
            for (BulkItemResponse itemResponse : response.getItems()) {
                T document = map.get(itemResponse.getId());
                if (itemResponse.isFailed()) {
                    // Add item it and its associated exception
                    if (document instanceof DataObject) {
                        DataObjectFeature docFeature = (((DataObject) document).getFeature());
                        result.addInErrorDoc(itemResponse.getId(), itemResponse.getFailure().getCause(),
                                             Optional.ofNullable(docFeature.getSession()),
                                             Optional.ofNullable(docFeature.getSessionOwner()));
                    } else {
                        result.addInErrorDoc(itemResponse.getId(), itemResponse.getFailure().getCause(),
                                             Optional.empty(), Optional.empty());
                    }
                    String msg = String.format("Document of type %s and id %s with label %s cannot be saved",
                                               documents[0].getClass(), itemResponse.getId(),
                                               map.get(itemResponse.getId()));
                    // Log error
                    LOGGER.warn(msg, itemResponse.getFailure().getCause());
                    // Add error msg to buffer
                    if (errorBuffer != null) {
                        if (errorBuffer.length() > 0) {
                            errorBuffer.append('\n');
                        }
                        errorBuffer.append(msg).append('\n').append("Cause: ");
                        // ElasticSearch creates Exception on exception (root one is more appropriate)
                        Throwable exception = Throwables.getRootCause(itemResponse.getFailure().getCause());
                        errorBuffer.append(exception.getMessage());
                    }
                } else {
                    if (document instanceof DataObject) {
                        DataObjectFeature docFeature = (((DataObject) document).getFeature());
                        result.addSavedDoc(itemResponse.getId(), Optional.ofNullable(docFeature.getSession()),
                                           Optional.ofNullable(docFeature.getSessionOwner()));
                    } else {
                        result.addSavedDoc(itemResponse.getId(), Optional.empty(), Optional.empty());
                    }
                }
            }
            // To make just saved documents searchable, the associated index must be refreshed
            this.refresh(index);
            return result;
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IIndexable> void searchAll(SearchKey<T, T> searchKey, Consumer<T> action, ICriterion inCrit) {
        try {

            SearchSourceBuilder builder = createSourceBuilder4Agg(addTypes(inCrit, searchKey.getSearchTypes()));
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(TYPE).source(builder);
            ICriterion crit = inCrit == null ? ICriterion.all() : inCrit;
            crit = addTypes(crit, searchKey.getSearchTypes());
            builder.query(crit.accept(CRITERION_VISITOR)).size(DEFAULT_SCROLLING_HITS_SIZE);
            request.source(builder);
            request.scroll(TimeValue.timeValueMinutes(KEEP_ALIVE_SCROLLING_TIME_MN));
            SearchResponse scrollResp = client.search(request, RequestOptions.DEFAULT);

            // Scroll until no hits are returned
            do {
                for (final SearchHit hit : scrollResp.getHits().getHits()) {
                    action.accept(gson.fromJson(hit.getSourceAsString(), (Class<T>) IIndexable.class));
                }

                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollResp.getScrollId());
                scrollRequest.scroll(TimeValue.timeValueMinutes(KEEP_ALIVE_SCROLLING_TIME_MN));
                scrollResp = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            } while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while
            // loop.
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    /**
     * Returns a tuple containing at the same time attribute values from search documents and facets
     * @param <R> Type of document to apply search
     * @param searchKey search key for documents of type R
     * @param criterion search criterion on documents of type R
     * @param attributeSource document attribute to return
     *                        @param facetsMap facets wanted
     * @return a tuple containing at the same time attribute values from search documents and facets
     */
    private <R> Tuple<SortedSet<Object>, Set<IFacet<?>>> searchJoined(SearchKey<?, R> searchKey, ICriterion criterion,
            String attributeSource, Map<String, FacetType> facetsMap) {
        try {
            // Add ".keyword" if attribute mapping type is of type text
            String attribute = isTextMapping(searchKey.getSearchIndex(), attributeSource)
                    ? attributeSource + KEYWORD_SUFFIX
                    : attributeSource;
            SortedSet<Object> uniqueValues = new TreeSet<>(Comparator.comparing(Objects::hashCode));
            Set<IFacet<?>> facets = unique(searchKey, addTypes(criterion, searchKey.getSearchTypes()), attribute,
                                           Integer.MAX_VALUE, uniqueValues, facetsMap);
            return new Tuple<>(uniqueValues, facets);
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public <T> Page<T> searchAllLimited(String index, Class<T> clazz, Pageable pageRequest) {
        try {
            final List<T> results = new ArrayList<>();
            SearchRequest request = new SearchRequest(index.toLowerCase());

            SearchSourceBuilder builder = new SearchSourceBuilder().from((int) pageRequest.getOffset())
                    .size(pageRequest.getPageSize());
            request.source(builder);

            SearchResponse response = client.search(request);
            SearchHits hits = response.getHits();
            for (SearchHit hit : hits) {
                results.add(gson.fromJson(hit.getSourceAsString(), clazz));
            }
            return new PageImpl<>(results, pageRequest, response.getHits().getTotalHits());
        } catch (final JsonSyntaxException | IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public <T extends IIndexable> FacetPage<T> search(SearchKey<T, T> searchKey, Pageable pageRequest, ICriterion crit,
            Map<String, FacetType> facetsMap) {
        ICriterion criterion = addTypes(crit, searchKey.getSearchTypes());
        // Search context is different from Earth search context, check if it's a geo-search
        if (searchKey.getCrs() != Crs.WGS_84) {
            // Does criterion tree contain a BoundaryBox or Polygon criterion, if so => make a projection on WGS84
            if (GeoHelper.containsPolygonOrBboxCriterion(criterion)) {
                GeoCriterionWithPolygonOrBboxVisitor visitor = new GeoCriterionWithPolygonOrBboxVisitor(
                        searchKey.getCrs());
                criterion = criterion.accept(visitor);
                PolygonCriterion polygonCrit = GeoHelper.findPolygonCriterion(criterion);
                if (polygonCrit != null) {
                    LOGGER.debug("Searching intersection with polygon {} projected on WGS84...",
                                 Arrays.stream(polygonCrit.getCoordinates()[0]).map(Arrays::toString)
                                         .collect(Collectors.joining(",")));
                }
            } else if (GeoHelper.containsCircleCriterion(criterion)) {
                // For Astro, circleCriterion radius is in fact the half-angle of the cone in degrees
                // We must change it into projected equivalent radius
                if (searchKey.getCrs() == Crs.ASTRO) {
                    CircleCriterion initialCircleCriterion = GeoHelper.findCircleCriterion(crit);
                    // Radius MUST NOT HAVE A UNIT
                    initialCircleCriterion
                            .setRadius(FastMath.toRadians(Double.parseDouble(initialCircleCriterion.getRadius()))
                                    * AUTHALIC_SPHERE_RADIUS);
                }
                return searchWithCircleCriterionInProjectedCrs(searchKey, pageRequest, facetsMap, criterion);
            }
        }
        return search0(searchKey, pageRequest, criterion, facetsMap);
    }

    /**
     * Particular case when search is asked with a circle criterion into a CRS which is not WGS84.
     */
    private <T extends IIndexable> FacetPage<T> searchWithCircleCriterionInProjectedCrs(SearchKey<T, T> searchKey,
            Pageable pageRequest, Map<String, FacetType> facetsMap, ICriterion criterion) {
        // If criterion contains a Circle criterion, it is more complicated :
        // Mars and Earth flattenings are different (and so astro, it's a perfect sphere)
        // So we obtain a max radius cricle and a min radius circle for earth projected coordinates
        // Every shapes into min radius circle are guaranted to be ok
        // Every shapes outo max radius circle are guaranted to not be ok
        // Other ones must be unitary tested one by one
        GeoCriterionWithCircleVisitor visitor = new GeoCriterionWithCircleVisitor(searchKey.getCrs());
        Pair<ICriterion, ICriterion> critOnWgs84Pair = criterion.accept(visitor);
        // In case of symetry (=> same radius for inner and outer circles) same criterion is returned into pair
        if (critOnWgs84Pair.getFirst().equals(critOnWgs84Pair.getSecond())) {
            long start = System.currentTimeMillis();
            FacetPage<T> page = search0(searchKey, pageRequest, critOnWgs84Pair.getFirst(), facetsMap);
            LOGGER.debug("Simple symetric circle search with radius: {} (duration: {} ms)",
                         GeoHelper.findCircleCriterion(critOnWgs84Pair.getFirst()).getRadius(),
                         System.currentTimeMillis() - start);
            return page;
        }
        // Criterion permiting to retrieve shapes betwwen both circles
        ICriterion betweenInnerAndOuterCirclesCriterionOnWgs84 = critOnWgs84Pair.getSecond();

        // FIRST: retrieve all data into inner circle
        ICriterion innerCircleOnWgs84Criterion = critOnWgs84Pair.getFirst();
        long start = System.currentTimeMillis();
        FacetPage<T> intoInnerCirclePage = search0(searchKey, pageRequest, innerCircleOnWgs84Criterion, facetsMap);
        // If more than MAX_PAGE_SIZE => TooManyResultException (too complicated case)
        if (!intoInnerCirclePage.isLast()) {
            throw new RsRuntimeException(
                    new TooManyResultsException("Please refine criteria to avoid exceeding page size limit"));
        }
        CircleCriterion innerCircleCrit = GeoHelper.findCircleCriterion(innerCircleOnWgs84Criterion);
        LOGGER.debug("Found {} points into inner circle with radius {} and center {} projected on WGS84 (search duration: {} ms)",
                     intoInnerCirclePage.getNumberOfElements(), innerCircleCrit.getRadius(),
                     Arrays.toString(innerCircleCrit.getCoordinates()), System.currentTimeMillis() - start);
        // SECOND: retrieve all data between inner and outer circles
        start = System.currentTimeMillis();
        FacetPage<T> betweenInnerAndOuterCirclesPage = search0(searchKey, pageRequest,
                                                               betweenInnerAndOuterCirclesCriterionOnWgs84, facetsMap);
        // If more than MAX_PAGE_SIZE => TooManyResultException (too complicated case)
        if (!intoInnerCirclePage.isLast()) {
            throw new RsRuntimeException(
                    new TooManyResultsException("Please refine criteria to avoid exceeding page size limit"));
        }
        LOGGER.debug("Found {} points between inner and outer circles (search duration: {} ms)",
                     betweenInnerAndOuterCirclesPage.getNumberOfElements(), System.currentTimeMillis() - start);

        // THIRD: keep only entities with a shape nearer than specified radius from specified center
        // Retrieve radius of specified circle on given Crs
        CircleCriterion circleCriterionOnCrs = GeoHelper.findCircleCriterion(criterion);
        double maxRadiusOnCrs = EsHelper.toMeters(circleCriterionOnCrs.getRadius());
        double[] center = circleCriterionOnCrs.getCoordinates();

        // Test all data one by one
        List<T> inOuterCircleEntities = new ArrayList<>();
        for (T entity : betweenInnerAndOuterCirclesPage.getContent()) {
            if (entity instanceof ILocalizable) {
                IGeometry shape = ((ILocalizable) entity).getNormalizedGeometry();
                if (GeoHelper.isNearer(shape, center, maxRadiusOnCrs, searchKey.getCrs())) {
                    inOuterCircleEntities.add(entity);
                } else {
                    LOGGER.error("Remove " + entity);
                }
            }
        }
        // Merge data
        if (!inOuterCircleEntities.isEmpty()) {
            LOGGER.debug("Keep {} points between inner and outer circles", inOuterCircleEntities.size());
            List<T> resultList = new ImmutableList.Builder<T>().addAll(intoInnerCirclePage.getContent())
                    .addAll(inOuterCircleEntities).build();
            return new FacetPage<>(resultList, null, intoInnerCirclePage.getPageable(), resultList.size());
        } else {
            LOGGER.debug("Keep no points between inner and outer circles");
        }
        return intoInnerCirclePage;
    }

    /**
     * Inner search on WGS84 method.
     * <b>NOTE : criterion already contains restricition on types !!</b>
     */
    @SuppressWarnings("unchecked")
    private <T extends IIndexable> FacetPage<T> search0(SearchKey<T, T> searchKey, Pageable pageRequest,
            ICriterion criterion, Map<String, FacetType> facetsMap) {
        String index = searchKey.getSearchIndex();
        try {
            final List<T> results = new ArrayList<>();

            Sort sort = pageRequest.getSort();
            // page size is max or page offset is > max page size, prepare sort for search_after
            if ((pageRequest.getOffset() >= MAX_RESULT_WINDOW) || (pageRequest.getPageSize() == MAX_RESULT_WINDOW)) {
                // A sort is mandatory to permit use of searchAfter (id by default if none provided)
                sort = (sort == null) || sort.isUnsorted() ? Sort.by("ipId") : pageRequest.getSort();
                // To assure unicity, always add "ipId" as a sort parameter
                if (sort.getOrderFor("ipId") == null) {
                    sort = sort.and(Sort.by("ipId"));
                }
            }
            Object[] lastSearchAfterSortValues = null;
            // If page starts over index 10 000, advance with searchAfter just before last request
            if (pageRequest.getOffset() >= MAX_RESULT_WINDOW) {
                lastSearchAfterSortValues = advanceWithSearchAfter(criterion, searchKey, pageRequest, index, sort);
            }

            final Object[] finalLastSearchAfterSortValues = lastSearchAfterSortValues;
            final Sort finalSort = sort;
            Consumer<SearchSourceBuilder> lastSearchAfterCustomizer = (builder) -> {
                try {
                    // If searchAfter has been executed (in that case manageSortRequest() has already been called)
                    if (finalLastSearchAfterSortValues != null) {
                        builder.searchAfter(finalLastSearchAfterSortValues).from(0);
                        manageSortRequest(index, builder, finalSort);
                    } else if ((finalSort != null) && finalSort.isSorted()) {
                        manageSortRequest(index, builder, finalSort);
                    }
                } catch (IOException e) {
                    //not nice but if IOException would be thrown (not using a consumer), it would be simply handle like that
                    throw new RsRuntimeException(e);
                }
            };

            Tuple<SearchResponse, Set<IFacet<?>>> responseNFacets = searchWithFacets(searchKey, criterion, pageRequest,
                                                                                     lastSearchAfterCustomizer, sort,
                                                                                     facetsMap);
            SearchResponse response = responseNFacets.v1();
            long start = System.currentTimeMillis();
            SearchHits hits = response.getHits();
            for (SearchHit hit : hits) {
                try {
                    results.add(gson.fromJson(hit.getSourceAsString(), (Class<T>) IIndexable.class));
                } catch (JsonParseException e) {
                    LOGGER.error("Unable to jsonify entity with id {}, source: \"{}\"", hit.getId(),
                                 hit.getSourceAsString());
                    throw new RsRuntimeException(e);
                }
            }
            LOGGER.debug("After Elasticsearch request execution, gsonification : {} ms",
                         System.currentTimeMillis() - start);
            return new FacetPage<>(results, responseNFacets.v2(), pageRequest, response.getHits().getTotalHits());
        } catch (final JsonSyntaxException | IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    private Tuple<SearchResponse, Set<IFacet<?>>> searchWithFacets(SearchKey<?, ?> searchKey, ICriterion criterion,
            Pageable pageRequest, Consumer<SearchSourceBuilder> searchSourceBuilderCustomizer, Sort sort,
            Map<String, FacetType> facetsMap) throws IOException {
        String index = searchKey.getSearchIndex();
        SearchRequest request = new SearchRequest(index).types(TYPE);
        SearchSourceBuilder builder = createSourceBuilder4Agg(criterion, (int) pageRequest.getOffset(),
                                                              pageRequest.getPageSize());

        if (searchSourceBuilderCustomizer != null) {
            searchSourceBuilderCustomizer.accept(builder);
        }

        // Managing aggregations if some facets are asked
        boolean twoPassRequestNeeded = manageFirstPassRequestAggregations(facetsMap, builder);
        request.source(builder);
        // Launch the request
        long start = System.currentTimeMillis();
        LOGGER.trace("ElasticsearchRequest: {}", request.toString());
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        LOGGER.debug("Elasticsearch request execution only : {} ms", System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        Set<IFacet<?>> facetResults = new HashSet<>();
        if (response.getHits().getTotalHits() != 0) {
            // At least one numeric facet is present, we need to replace all numeric facets by associated range
            // facets
            if (twoPassRequestNeeded) {
                // Rebuild request
                request = new SearchRequest(index).types(TYPE);
                builder = createSourceBuilder4Agg(criterion, (int) pageRequest.getOffset(), pageRequest.getPageSize());

                if (searchSourceBuilderCustomizer != null) {
                    searchSourceBuilderCustomizer.accept(builder);
                }
                Map<String, Aggregation> aggsMap = response.getAggregations().asMap();
                manageSecondPassRequestAggregations(facetsMap, builder, aggsMap);
                // Relaunch the request with replaced facets
                request.source(builder);
                LOGGER.trace("ElasticsearchRequest (2nd pass): {}", request.toString());
                response = client.search(request, RequestOptions.DEFAULT);
            }

            // If offset >= MAX_RESULT_WINDOW or page size = MAX_RESULT_WINDOW, this means a next page should exist
            // (not necessarly)
            if ((pageRequest.getOffset() >= MAX_RESULT_WINDOW) || (pageRequest.getPageSize() == MAX_RESULT_WINDOW)) {
                saveReminder(searchKey, pageRequest, criterion, sort, response);
            }

            extractFacetsFromResponse(facetsMap, response, facetResults);
        }
        LOGGER.debug("After Elasticsearch request execution, aggs and searchAfter management : {} ms",
                     System.currentTimeMillis() - start);
        return new Tuple<>(response, facetResults);
    }

    /**
     * extract facets according to response and facetsMap and put them into facetResults
     */
    private void extractFacetsFromResponse(Map<String, FacetType> facetsMap, SearchResponse response,
            Set<IFacet<?>> facetResults) {
        if ((facetsMap != null) && (response.getAggregations() != null)) {
            // Get the new aggregations result map
            Map<String, Aggregation> aggsMap = response.getAggregations().asMap();
            // Fill the facet set
            for (Map.Entry<String, FacetType> entry : facetsMap.entrySet()) {
                FacetType facetType = entry.getValue();
                String attributeName = entry.getKey();
                fillFacets(aggsMap, facetResults, facetType, attributeName, response.getHits().getTotalHits());
            }
        }
    }

    private void saveReminder(SearchKey<?, ?> searchKey, Pageable pageRequest, ICriterion crit, Sort sort,
            SearchResponse response) {
        if (response.getHits().getHits().length != 0) {
            // Store last sort value in order to use searchAfter next time
            Object[] sortValues = response.getHits().getAt(response.getHits().getHits().length - 1).getSortValues();
            OffsetDateTime expirationDate = OffsetDateTime.now().plus(KEEP_ALIVE_SCROLLING_TIME_MN, ChronoUnit.MINUTES);
            // Create a AbstractReminder and save it into ES for next page
            SearchAfterReminder reminder = new SearchAfterReminder(crit, searchKey, sort, pageRequest.next());
            reminder.setExpirationDate(expirationDate);
            reminder.setSearchAfterSortValues(sortValues);

            save(REMINDER_IDX, reminder);
            // Create a task to be executed after KEEP_ALIVE_SCROLLING_TIME_MN that delete all reminders whom
            // expiration date has been reached
            // No need to add reminder type criterion because reminder type is useless since ES6
            reminderCleanExecutor
                    .schedule(() -> deleteByQuery(REMINDER_IDX, ICriterion.le("expirationDate", OffsetDateTime.now())),
                              KEEP_ALIVE_SCROLLING_TIME_MN, TimeUnit.MINUTES);
        }
    }

    /**
     * <b>NOTE: critBuilder already contains restriction on types</b>
     */
    private <T extends IIndexable> Object[] advanceWithSearchAfter(ICriterion crit, SearchKey<T, T> searchKey,
            Pageable pageRequest, String index, Sort sort) {
        try {
            Object[] sortValues = null;
            int searchPageNumber = 0;
            Pageable searchReminderPageRequest;
            if (indexExists(REMINDER_IDX)) {
                // First check existence of AbstractReminder for exact given pageRequest from ES
                SearchAfterReminder reminder = new SearchAfterReminder(crit, searchKey, sort, pageRequest);
                reminder = get(REMINDER_IDX, reminder);
                if (reminder != null) {
                    LOGGER.debug("Found search after for offset {}", pageRequest.getOffset());
                    return reminder.getSearchAfterSortValues();
                }
                // Then check if a closer one exists (advance is done by MAX_RESULT_WINDOW steps so we must take this
                // into account)
                searchPageNumber = (int) ((pageRequest.getOffset() - (pageRequest.getOffset() % MAX_RESULT_WINDOW))
                        / MAX_RESULT_WINDOW);
                while (searchPageNumber > 0) {
                    searchReminderPageRequest = PageRequest.of(searchPageNumber, MAX_RESULT_WINDOW);
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
            SearchRequest request = new SearchRequest(index).types(TYPE);
            // By default, launch request from 0 to 10_000 (without aggregations)...
            int offset = 0;
            int pageSize = MAX_RESULT_WINDOW;
            SearchSourceBuilder builder = createSourceBuilder4Agg(crit, offset, pageSize);
            manageSortRequest(index, builder, sort);
            request.source(builder);
            // ...Except if a closer reminder has already been found
            if (sortValues != null) {
                offset = searchPageNumber * MAX_RESULT_WINDOW;
            } else {
                LOGGER.debug("Search (after) : offset {}", offset);
                SearchResponse response = client.search(request, RequestOptions.DEFAULT);
                sortValues = response.getHits().getAt(response.getHits().getHits().length - 1).getSortValues();
                offset += MAX_RESULT_WINDOW;
            }
            OffsetDateTime expirationDate = OffsetDateTime.now().plus(KEEP_ALIVE_SCROLLING_TIME_MN, ChronoUnit.MINUTES);

            int nextToLastOffset = (int) (pageRequest.getOffset() - (pageRequest.getOffset() % MAX_RESULT_WINDOW));
            // Execute as many request with search after as necessary to advance to next to last page of
            // MAX_RESULT_WINDOW size until offset
            while (offset < nextToLastOffset) {
                // Change offset
                LOGGER.debug("Search after : offset {}", offset);
                builder.from(0).searchAfter(sortValues);
                SearchResponse response = client.search(request, RequestOptions.DEFAULT);
                sortValues = response.getHits().getAt(response.getHits().getHits().length - 1).getSortValues();
                // Create a AbstractReminder and save it into ES for next page
                SearchAfterReminder reminder = new SearchAfterReminder(crit, searchKey, sort,
                        PageRequest.of(offset / MAX_RESULT_WINDOW, MAX_RESULT_WINDOW).next());
                reminder.setExpirationDate(expirationDate);
                reminder.setSearchAfterSortValues(response.getHits().getAt(response.getHits().getHits().length - 1)
                        .getSortValues());

                save(REMINDER_IDX, reminder);
                offset += MAX_RESULT_WINDOW;
            }
            // Beware of offset that is a multiple of MAX_RESULT_WINDOW
            if (pageRequest.getOffset() != offset) {
                int size = (int) (pageRequest.getOffset() - offset);
                LOGGER.debug("Search after : offset {}, size {}", offset, size);
                builder.size(size).searchAfter(sortValues).from(0); // needed by searchAfter
                SearchResponse response = client.search(request, RequestOptions.DEFAULT);
                sortValues = response.getHits().getAt(response.getHits().getHits().length - 1).getSortValues();
            }

            // Create a task to be executed after KEEP_ALIVE_SCROLLING_TIME_MN that delete all reminders whom
            // expiration date has been reached
            // No need to add type restriction, reminder is useless since ES6
            reminderCleanExecutor
                    .schedule(() -> deleteByQuery(REMINDER_IDX, ICriterion.le("expirationDate", OffsetDateTime.now())),
                              KEEP_ALIVE_SCROLLING_TIME_MN, TimeUnit.MINUTES);
            return sortValues;
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    private SearchSourceBuilder createSourceBuilder4Agg(ICriterion criterion) {
        return createSourceBuilder4Agg(criterion, 0, 0);
    }

    /**
     * Build a SearchSourceBuilder following given ICriterion on searchKey
     */
    private SearchSourceBuilder createSourceBuilder4Agg(ICriterion criterion, int from, int size) {
        // Use filter instead of "direct" query (in theory, quickest because no score is computed)
        QueryBuilder critBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
                .filter(criterion.accept(CRITERION_VISITOR));
        // Only return hits information
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(critBuilder);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        return searchSourceBuilder;
    }

    @Override
    public <T extends IIndexable> Long count(SearchKey<?, T> searchKey, ICriterion criterion) {
        try {
            SearchSourceBuilder builder = createSourceBuilder4Agg(addTypes(criterion, searchKey.getSearchTypes()));
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(TYPE).source(builder);
            // Launch the request
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            return response.getHits().getTotalHits();
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public <T extends IIndexable> double sum(SearchKey<?, T> searchKey, ICriterion criterion, String attName) {
        try {
            SearchSourceBuilder builder = createSourceBuilder4Agg(addTypes(criterion, searchKey.getSearchTypes()));
            builder.aggregation(AggregationBuilders.sum(attName).field(attName));
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(TYPE).source(builder);
            // Launch the request
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            return ((Sum) response.getAggregations().get(attName)).getValue();
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public <T extends IIndexable> OffsetDateTime minDate(SearchKey<?, T> searchKey, ICriterion criterion,
            String attName) {
        try {
            SearchSourceBuilder builder = createSourceBuilder4Agg(addTypes(criterion, searchKey.getSearchTypes()));
            builder.aggregation(AggregationBuilders.min(attName).field(attName));
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(TYPE).source(builder);
            // Launch the request
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            Min min = response.getAggregations().get(attName);
            if ((min == null) || !Double.isFinite(min.getValue())) {
                return null;
            }
            return OffsetDateTimeAdapter.parse(min.getValueAsString());
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public <T extends IIndexable> OffsetDateTime maxDate(SearchKey<?, T> searchKey, ICriterion criterion,
            String attName) {
        try {
            SearchSourceBuilder builder = createSourceBuilder4Agg(addTypes(criterion, searchKey.getSearchTypes()));
            builder.aggregation(AggregationBuilders.max(attName).field(attName));
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(TYPE).source(builder);
            // Launch the request
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            Max max = response.getAggregations().get(attName);
            if ((max == null) || !Double.isFinite(max.getValue())) {
                return null;
            }
            return OffsetDateTimeAdapter.parse(max.getValueAsString());
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public <T extends IIndexable> Aggregations getAggregations(SearchKey<?, T> searchKey, ICriterion criterion,
            Collection<QueryableAttribute> attributes) {
        try {
            SearchSourceBuilder builder = createSourceBuilder4Agg(addTypes(criterion, searchKey.getSearchTypes()));
            for (QueryableAttribute qa : attributes) {
                if (qa.isTextAttribute() && (qa.getTermsLimit() > 0)) {
                    builder.aggregation(AggregationBuilders.terms(qa.getAttributeName())
                            .field(qa.getAttributeName() + KEYWORD_SUFFIX).size(qa.getTermsLimit()));
                } else if (!qa.isTextAttribute()) {
                    builder.aggregation(AggregationBuilders.stats(qa.getAttributeName()).field(qa.getAttributeName()));
                }
            }
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(TYPE).source(builder);
            // Launch the request
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // Update attributes with aggregation if any
            if (response.getAggregations() != null) {
                for (Aggregation agg : response.getAggregations()) {
                    attributes.stream().filter(a -> agg.getName().equals(a.getAttributeName())).findFirst()
                            .ifPresent(a -> a.setAggregation(agg));
                }
                return response.getAggregations();
            } else {
                return new Aggregations(Lists.newArrayList());
            }
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    /**
     * Retrieve sorted set of given attribute unique string values following request
     * @param searchKey search key
     * @param crit criterion
     * @param attName string attribute name (full path)
     * @param <T> search type
     * @return a TreeSet&lt;String>
     */
    @Override
    public <T extends IIndexable> SortedSet<String> uniqueAlphaSorted(SearchKey<?, T> searchKey, ICriterion crit,
            String attName, int maxCount) {
        SortedSet<String> result = new TreeSet<>();
        unique(searchKey, addTypes(crit, searchKey.getSearchTypes()), attName, maxCount, result, new HashMap<>());
        return result;
    }

    /**
     * Retrieve set of given attribute unique typed values following request
     * @param searchKey search key
     * @param crit criterion
     * @param attName attribute name (full path)
     * @param <T> search type
     * @param <R> result type
     * @return an HashSet
     */
    public <T, R> Set<R> unique(SearchKey<?, T> searchKey, ICriterion crit, String attName) {
        Set<R> result = new HashSet<>();
        unique(searchKey, addTypes(crit, searchKey.getSearchTypes()), attName, Integer.MAX_VALUE, result,
               new HashMap<>());
        return result;
    }

    /**
     * Retrieve set of given attribute unique typed values following request
     * @param searchKey search key
     * @param crit criterion
     * @param inAttName attribute name (full path)
     * @param maxCount maximum value search
     * @param set contains unique values wanted (modified)
     * @param facetsMap facets wanted
     * @param <T> type of data on which unique values are searched
     * @param <R> type of attribute value
     * @return facets asked
     */
    @SuppressWarnings("unchecked")
    private <T, R, S extends Set<R>> Set<IFacet<?>> unique(SearchKey<?, T> searchKey, ICriterion crit, String inAttName,
            int maxCount, S set, Map<String, FacetType> facetsMap) {
        try {

            String attName = isTextMapping(searchKey.getSearchIndex(), inAttName) ? inAttName + ".keyword" : inAttName;
            Consumer<SearchSourceBuilder> addUniqueTermAgg = (builder) -> builder
                    .aggregation(AggregationBuilders.terms(attName).field(attName).size(maxCount));

            Tuple<SearchResponse, Set<IFacet<?>>> responseNFacets = searchWithFacets(searchKey, crit,
                                                                                     PageRequest.of(0, 1),
                                                                                     addUniqueTermAgg, null, facetsMap);
            Terms terms = responseNFacets.v1().getAggregations().get(attName);
            for (Terms.Bucket bucket : terms.getBuckets()) {
                set.add((R) bucket.getKey());
            }
            return responseNFacets.v2();
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> List<R> search(SearchKey<?, R> searchKey, ICriterion criterion, String sourceAttribute) {
        try {
            SortedSet<Object> objects = searchAllCache.getUnchecked(new CacheKey(searchKey,
                    addTypes(criterion, searchKey.getSearchTypes()), sourceAttribute)).v1();
            return objects.stream().map(o -> (R) o).collect(Collectors.toList());
        } catch (final JsonSyntaxException e) {
            throw new RsRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, U> List<U> search(SearchKey<?, R> searchKey, ICriterion criterion, String sourceAttribute,
            Function<R, U> transformFct) {
        try {
            SortedSet<Object> objects = searchAllCache.getUnchecked(new CacheKey(searchKey,
                    addTypes(criterion, searchKey.getSearchTypes()), sourceAttribute)).v1();
            return objects.stream().map(o -> (R) o).map(transformFct).collect(Collectors.toList());
        } catch (final JsonSyntaxException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> Tuple<List<U>, Set<IFacet<?>>> search(SearchKey<?, R[]> searchKey, ICriterion criterion,
            String sourceAttribute, Predicate<R> filterPredicate, Function<R, U> transformFct,
            Map<String, FacetType> facetsMap) {
        try {
            Tuple<SortedSet<Object>, Set<IFacet<?>>> objects = searchAllCache.getUnchecked(new CacheKey(searchKey,
                    addTypes(criterion, searchKey.getSearchTypes()), sourceAttribute, facetsMap));
            return new Tuple<>(objects.v1().stream().map(o -> (R) o).distinct().filter(filterPredicate)
                    .map(transformFct).collect(Collectors.toList()), objects.v2());

        } catch (final JsonSyntaxException e) {
            throw new RsRuntimeException(e);
        }
    }

    /**
     * Is given attribute (can be a composite attribute like toto.titi) of type text from ES mapping ?
     * @param inIndex concerned index
     * @param attribute attribute from type
     * @return true or false
     */
    private boolean isTextMapping(String inIndex, String attribute) throws IOException {
        String index = inIndex.toLowerCase();
        try {
            Response response = client.getLowLevelClient()
                    .performRequest(new Request("GET", index + "/_mapping/" + TYPE + "/field/" + attribute));
            try (InputStream is = response.getEntity().getContent()) {
                Map<String, Object> map = XContentHelper.convertToMap(XContentType.JSON.xContent(), is, true);
                // If attribute exists, response should contain this chain of several maps :
                // <index>."mappings".<type>.<attribute>."mapping".<attribute_last_path>."type"
                if ((map != null) && !map.isEmpty()) {
                    // In case attribute is toto.titi.tutu, we will need "tutu" further
                    String lastPathAtt = attribute.contains(".") ? attribute.substring(attribute.lastIndexOf('.') + 1)
                            : attribute;
                    // BEWARE : instead of map.get(index) on the innermost map value retrieval, we use directly
                    // map.values().iterator().next() to get value associated to singleton element whatever the key is
                    // Indeed, because of Elasticsearch version 6 single type update, some indices are retrieved through
                    // an alias. Asking an alias mapping returned a block with index name, not alias name
                    return toMap(toMap(toMap(toMap(toMap(toMap(map.values().iterator().next()).get("mappings"))
                            .get(TYPE)).get(attribute)).get("mapping")).get(lastPathAtt)).get("type").equals("text");

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
     * Add sort to the request
     * @param builder search request
     * @param sort map(attribute name, true if ascending)
     */
    private void manageSortRequest(String index, SearchSourceBuilder builder, Sort sort) throws IOException {
        // Convert Sort into linked hash map
        LinkedHashMap<String, Boolean> ascSortMap = new SortToLinkedHashMap().convert(sort);

        // Because string attributes are not indexed with Elasticsearch, it is necessary to add ".keyword" at
        // end of attribute name into sort request. So we need to know string attributes
        Response response = client.getLowLevelClient().performRequest(new Request("GET",
                index + "/_mapping/field/" + Joiner.on(",").join(ascSortMap.keySet())));
        try (InputStream is = response.getEntity().getContent()) {
            Map<String, Object> map = XContentHelper.convertToMap(XContentType.JSON.xContent(), is, true);
            if ((map != null) && !map.isEmpty()) {
                // NOTE: in our context, attributes have same metada for all types so once we found one, we stop.
                // To do that, we create a new LinkedHashMap to KEEPS keys order !!! (crucial)
                LinkedHashMap<String, Boolean> updatedAscSortMap = new LinkedHashMap<>(ascSortMap.size());
                for (Map.Entry<String, Boolean> sortEntry : ascSortMap.entrySet()) {
                    String attribute = sortEntry.getKey();
                    if (isTextMapping(map, attribute)) {
                        updatedAscSortMap.put(attribute + KEYWORD_SUFFIX, sortEntry.getValue());
                    } else {
                        updatedAscSortMap.put(attribute, sortEntry.getValue());
                    }
                }

                // Add sort to request
                updatedAscSortMap.forEach((key, value) -> builder.sort(SortBuilders.fieldSort(key)
                        .order(value ? SortOrder.ASC : SortOrder.DESC).unmappedType("double")));
                // "double" because a type is necessary. This has only an impact when seaching on several indices if
                // property is mapped on one and no on the other(s). Will see this when it happens (if it happens a day)
                // entry -> builder.sort(entry.getKey(), entry.getValue() ? SortOrder.ASC : SortOrder.DESC));
            }
        }
    }

    /**
     * Add aggregations to the search request.
     * @param facetsMap asked facets
     * @param builder search request
     * @return true if a second pass is needed (managing range facets)
     */
    private boolean manageFirstPassRequestAggregations(Map<String, FacetType> facetsMap, SearchSourceBuilder builder) {
        boolean twoPassRequestNeeded = false;
        if (facetsMap != null) {
            // Numeric/date facets needs :
            // First to add a percentiles aggregation to retrieved 9 values corresponding to
            // 10%, 20 %, ..., 90 % of the values
            // Secondly to add a range aggregation with these 9 values in order to retrieve 10 buckets of equal
            // size of values
            for (Map.Entry<String, FacetType> entry : facetsMap.entrySet()) {
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
     * Add aggregations to the search request (second pass). For range aggregations, use percentiles results (from first
     * pass request results) to create range aggregagtions.
     * @param facetsMap asked facets
     * @param builder search request
     * @param aggsMap first pass aggregagtions results
     */
    private void manageSecondPassRequestAggregations(Map<String, FacetType> facetsMap, SearchSourceBuilder builder,
            Map<String, Aggregation> aggsMap) {
        for (Map.Entry<String, FacetType> entry : facetsMap.entrySet()) {
            FacetType facetType = entry.getValue();
            String attributeName = entry.getKey();
            String attName;
            // Replace percentiles aggregations by range aggregations
            if ((facetType == FacetType.NUMERIC) || (facetType == FacetType.DATE)) {
                attName = facetType == FacetType.NUMERIC ? attributeName + NUMERIC_FACET_SUFFIX
                        : attributeName + DATE_FACET_SUFFIX;
                Percentiles percentiles = (Percentiles) aggsMap.get(attName);
                // No percentile values for this property => skip aggregation
                if (Iterables.all(percentiles, p -> Double.isNaN(p.getValue()))) {
                    continue;
                }
                AggregationBuilder aggBuilder = facetType == FacetType.NUMERIC
                        ? FacetType.RANGE_DOUBLE.accept(aggBuilderFacetTypeVisitor, attributeName, percentiles)
                        : FacetType.RANGE_DATE.accept(aggBuilderFacetTypeVisitor, attributeName, percentiles);
                // In case range contains only one value, better remove facet
                if (aggBuilder != null) {
                    // And add max and min aggregations
                    builder.aggregation(aggBuilder);
                    // And add max and min aggregations
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
            String attributeName, long totalHits) {
        switch (facetType) {
            case STRING:
                fillStringFacets(aggsMap, facets, attributeName);
                break;
            case BOOLEAN:
                fillBooleanFacets(aggsMap, facets, attributeName, totalHits);
                break;
            case NUMERIC:
                fillNumericFacets(aggsMap, facets, attributeName);
                break;
            case DATE:
                fillDateFacets(aggsMap, facets, attributeName);
                break;
            default:
                break;
        }
    }

    private void fillDateFacets(Map<String, Aggregation> aggsMap, Set<IFacet<?>> facets, String attributeName) {
        org.elasticsearch.search.aggregations.bucket.range.Range dateRange = (org.elasticsearch.search.aggregations.bucket.range.Range) aggsMap
                .get(attributeName + AggregationBuilderFacetTypeVisitor.RANGE_FACET_SUFFIX);
        if (dateRange != null) {
            Map<Range<OffsetDateTime>, Long> valueMap = new LinkedHashMap<>();
            for (Bucket bucket : dateRange.getBuckets()) {
                // Retrieve min and max aggregagtions to replace -Infinity and +Infinity
                Min min = (Min) aggsMap.get(attributeName + AggregationBuilderFacetTypeVisitor.MIN_FACET_SUFFIX);
                Max max = (Max) aggsMap.get(attributeName + AggregationBuilderFacetTypeVisitor.MAX_FACET_SUFFIX);
                // Parsing ranges
                Range<OffsetDateTime> valueRange;
                // Case with no value : every bucket has a NaN value (as from, to or both)
                if (Objects.equals(bucket.getTo(), Double.NaN) || Objects.equals(bucket.getFrom(), Double.NaN)) {
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
    }

    private void fillNumericFacets(Map<String, Aggregation> aggsMap, Set<IFacet<?>> facets, String attributeName) {
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
                if (Objects.equals(bucket.getTo(), Double.NaN) || Objects.equals(bucket.getFrom(), Double.NaN)) {
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
                      // range is then [min -> value [, because min value is scaled it is necessary to choose a little
                      // less
                    valueRange = Range.closedOpen(EsHelper.scaledDown(min.getValue()), (Double) bucket.getTo());
                } else if (Objects.equals(bucket.getTo(), Double.POSITIVE_INFINITY)) { // [value -> +∞)
                    // range is then [value, max], because max value is scaled it is necessary to choose a little more
                    valueRange = Range.closed((Double) bucket.getFrom(), EsHelper.scaledUp(max.getValue()));
                } else { // [value -> value [
                    valueRange = Range.closedOpen((Double) bucket.getFrom(), (Double) bucket.getTo());
                }
                valueMap.put(valueRange, bucket.getDocCount());
            }
            facets.add(new NumericFacet(attributeName, valueMap));
        }
    }

    private void fillStringFacets(Map<String, Aggregation> aggsMap, Set<IFacet<?>> facets, String attributeName) {
        Terms terms = (Terms) aggsMap.get(attributeName + AggregationBuilderFacetTypeVisitor.STRING_FACET_SUFFIX);
        if (terms.getBuckets().isEmpty()) {
            return;
        }
        Map<String, Long> valueMap = new LinkedHashMap<>(terms.getBuckets().size());
        terms.getBuckets().forEach(b -> valueMap.put(b.getKeyAsString(), b.getDocCount()));
        facets.add(new StringFacet(attributeName, valueMap, terms.getSumOfOtherDocCounts()));
    }

    private void fillBooleanFacets(Map<String, Aggregation> aggsMap, Set<IFacet<?>> facets, String attributeName,
            long totalHits) {
        Terms terms = (Terms) aggsMap.get(attributeName + AggregationBuilderFacetTypeVisitor.STRING_FACET_SUFFIX);
        if (terms.getBuckets().isEmpty()) {
            return;
        }
        Map<Boolean, Long> valueMap = new LinkedHashMap<>(terms.getBuckets().size());
        AtomicLong docWithTrueOrFalseCount = new AtomicLong(0);
        terms.getBuckets().forEach(b -> {
            valueMap.put(Boolean.valueOf(b.getKeyAsString()), b.getDocCount());
            docWithTrueOrFalseCount.addAndGet(b.getDocCount());
        });

        // A boolean bucket boes not set null values into "sum_other_doc_count" so "others" is computed from
        // total - true values - false values
        facets.add(new BooleanFacet(attributeName, valueMap, totalHits - docWithTrueOrFalseCount.get()));
    }

    @Override
    public <T> Page<T> multiFieldsSearch(SearchKey<T, T> searchKey, Pageable pageRequest, Object inValue,
            String... fields) {
        try {
            final List<T> results = new ArrayList<>();
            // OffsetDateTime must be formatted to be correctly used following Gson mapping
            Object value = inValue instanceof OffsetDateTime ? OffsetDateTimeAdapter.format((OffsetDateTime) inValue)
                    : inValue;
            // Create filter query with all asked types and multi match query
            BoolQueryBuilder filterBuilder = QueryBuilders.boolQuery();
            for (String type : searchKey.getSearchTypes()) {
                filterBuilder = filterBuilder.must(QueryBuilders.matchPhraseQuery("type", type));
            }
            filterBuilder = filterBuilder.must(QueryBuilders.multiMatchQuery(value, fields));

            QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
                    .filter(filterBuilder);
            SearchSourceBuilder builder = new SearchSourceBuilder().query(queryBuilder)
                    .from((int) pageRequest.getOffset()).size(pageRequest.getPageSize());
            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(TYPE).source(builder);

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            SearchHits hits = response.getHits();
            for (SearchHit hit : hits) {
                results.add(gson.fromJson(hit.getSourceAsString(), searchKey.fromType(hit.getType())));
            }
            return new PageImpl<>(results, pageRequest, response.getHits().getTotalHits());
        } catch (final JsonSyntaxException | IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public <T extends IIndexable & IDocFiles> void computeInternalDataFilesSummary(SearchKey<T, T> searchKey,
            ICriterion crit, String discriminantProperty, DocFilesSummary summary, String... fileTypes) {
        try {
            if ((fileTypes == null) || (fileTypes.length == 0)) {
                throw new IllegalArgumentException("At least one file type must be provided");
            }
            SearchSourceBuilder builder = createSourceBuilder4Agg(addTypes(crit, searchKey.getSearchTypes()));
            // Add files count and files sum size aggregations
            addFilesCountAndSumAggs(searchKey, discriminantProperty, builder, fileTypes);

            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(TYPE).source(builder);
            // Launch the request
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            // First "global" aggregations results
            summary.addDocumentsCount(response.getHits().getTotalHits());
            Aggregations aggs = response.getAggregations();
            long totalFileCount = 0;
            long totalFileSize = 0;
            for (String fileType : fileTypes) {
                ValueCount valueCount = aggs.get("total_" + fileType + "_files_count");
                totalFileCount += valueCount.getValue();
                Sum sum = aggs.get("total_" + fileType + "_files_size");
                totalFileSize += sum.getValue();
            }
            summary.addFilesCount(totalFileCount);
            summary.addFilesSize(totalFileSize);
            // Then discriminants buckets aggregations results
            Terms buckets = aggs.get(discriminantProperty);
            for (Terms.Bucket bucket : buckets.getBuckets()) {
                // Usualy discriminant = tag name
                String discriminant = bucket.getKeyAsString();
                if (!summary.getSubSummariesMap().containsKey(discriminant)) {
                    summary.getSubSummariesMap().put(discriminant, new DocFilesSubSummary(fileTypes));
                }
                DocFilesSubSummary discSummary = summary.getSubSummariesMap().get(discriminant);
                discSummary.addDocumentsCount(bucket.getDocCount());
                Aggregations discAggs = bucket.getAggregations();
                long filesCount = 0;
                long filesSize = 0;
                for (String fileType : fileTypes) {
                    ValueCount valueCount = discAggs.get(fileType + "_files_count");
                    filesCount += valueCount.getValue();
                    Sum sum = discAggs.get(fileType + "_files_size");
                    filesSize += sum.getValue();
                    FilesSummary filesSummary = discSummary.getFileTypesSummaryMap().get(fileType);
                    filesSummary.addFilesCount(valueCount.getValue());
                    filesSummary.addFilesSize((long) sum.getValue());
                }
                discSummary.addFilesCount(filesCount);
                discSummary.addFilesSize(filesSize);

            }
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    private <T extends IIndexable & IDocFiles> void addFilesCountAndSumAggs(SearchKey<T, T> searchKey,
            String discriminantProperty, SearchSourceBuilder builder, String[] fileTypes) throws IOException {
        // Add aggregations to manage compute summary
        // First "global" aggregations on each asked file types
        for (String fileType : fileTypes) {
            // file count
            builder.aggregation(AggregationBuilders.count("total_" + fileType + "_files_count")
                    .field("feature.files." + fileType + ".filesize")); // Only count files with a size
            // file size sum
            builder.aggregation(AggregationBuilders.sum("total_" + fileType + "_files_size")
                    .field("feature.files." + fileType + ".filesize"));
        }
        // Then bucket aggregation by discriminants
        String termsFieldProperty = discriminantProperty;
        if (isTextMapping(searchKey.getSearchIndex(), discriminantProperty)) {
            termsFieldProperty += KEYWORD_SUFFIX;
        }
        // Discriminant distribution aggregator
        TermsAggregationBuilder termsAggBuilder = AggregationBuilders.terms(discriminantProperty)
                .field(termsFieldProperty).size(Integer.MAX_VALUE);
        // and "total" aggregations on each asked file types
        for (String fileType : fileTypes) {
            // files count
            termsAggBuilder.subAggregation(AggregationBuilders.count(fileType + "_files_count")
                    .field("feature.files." + fileType + ".filesize"));
            // file size sum
            termsAggBuilder.subAggregation(AggregationBuilders.sum(fileType + "_files_size")
                    .field("feature.files." + fileType + ".filesize"));
        }
        builder.aggregation(termsAggBuilder);
    }

    @Override
    public <T extends IIndexable & IDocFiles> void computeExternalDataFilesSummary(SearchKey<T, T> searchKey,
            ICriterion crit, String discriminantProperty, DocFilesSummary summary, String... fileTypes) {
        try {
            if ((fileTypes == null) || (fileTypes.length == 0)) {
                throw new IllegalArgumentException("At least one file type must be provided");
            }

            SearchSourceBuilder builder = createSourceBuilder4Agg(addTypes(crit, searchKey.getSearchTypes()));
            // Add files cardinality aggregation
            addFilesCardinalityAgg(searchKey, discriminantProperty, builder, fileTypes);

            SearchRequest request = new SearchRequest(searchKey.getSearchIndex()).types(TYPE).source(builder);

            // Launch the request
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // First "global" aggregations results
            summary.addDocumentsCount(response.getHits().getTotalHits());
            Aggregations aggs = response.getAggregations();
            long totalFileCount = 0;
            for (String fileType : fileTypes) {
                Cardinality cardinality = aggs.get("total_" + fileType + "_files_count");
                totalFileCount += cardinality.getValue();
            }
            summary.addFilesCount(totalFileCount);
            // Then discriminants buckets aggregations results
            Terms buckets = aggs.get(discriminantProperty);
            for (Terms.Bucket bucket : buckets.getBuckets()) {
                String discriminant = bucket.getKeyAsString();
                if (!summary.getSubSummariesMap().containsKey(discriminant)) {
                    summary.getSubSummariesMap().put(discriminant, new DocFilesSubSummary(fileTypes));
                }
                DocFilesSubSummary discSummary = summary.getSubSummariesMap().get(discriminant);
                discSummary.addDocumentsCount(bucket.getDocCount());
                Aggregations discAggs = bucket.getAggregations();
                long filesCount = 0;
                for (String fileType : fileTypes) {
                    Cardinality cardinality = discAggs.get(fileType + "_files_count");
                    filesCount += cardinality.getValue();
                    discSummary.getFileTypesSummaryMap().get(fileType).addFilesCount(cardinality.getValue());
                }
                discSummary.addFilesCount(filesCount);
            }
        } catch (IOException e) {
            throw new RsRuntimeException(e);
        }
    }

    /**
     * Difference between addFilesCardinalityAgg and addFilesCountAndSumAggs is on the type of aggregagtion (and the
     * file size sum of course).
     * In first case, propertie values are counted (for internal data files, it is sufficient because data files should
     * be differents), in second, distinct property values are counted (and for external files, which is the case here,
     * nothing prevents from use same uri on several data objects)
     */
    private <T extends IIndexable & IDocFiles> void addFilesCardinalityAgg(SearchKey<T, T> searchKey,
            String discriminantProperty, SearchSourceBuilder builder, String[] fileTypes) throws IOException {
        // Add aggregations to manage compute summary
        // First "global" aggregations on each asked file types
        for (String fileType : fileTypes) {
            // file cardinality
            builder.aggregation(AggregationBuilders.cardinality("total_" + fileType + "_files_count")
                    .field("feature.files." + fileType + ".uri" + KEYWORD_SUFFIX)); // Only count files with a size
        }
        // Then bucket aggregation by discriminants
        String termsFieldProperty = discriminantProperty;
        if (isTextMapping(searchKey.getSearchIndex(), discriminantProperty)) {
            termsFieldProperty += KEYWORD_SUFFIX;
        }
        // Discriminant distribution aggregator
        TermsAggregationBuilder termsAggBuilder = AggregationBuilders.terms(discriminantProperty)
                .field(termsFieldProperty).size(Integer.MAX_VALUE);
        // and "total" aggregations on each asked file types
        for (String fileType : fileTypes) {
            // files cardinality
            termsAggBuilder.subAggregation(AggregationBuilders.cardinality(fileType + "_files_count")
                    .field("feature.files." + fileType + ".uri" + KEYWORD_SUFFIX));
        }
        builder.aggregation(termsAggBuilder);
    }
}
