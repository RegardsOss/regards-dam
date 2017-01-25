package fr.cnes.regards.modules.crawler.dao;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
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
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.jboss.netty.handler.timeout.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import fr.cnes.regards.modules.crawler.dao.querybuilder.QueryBuilderVisitor;
import fr.cnes.regards.modules.crawler.domain.IIndexable;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;

/**
 * Elasticsearch repository implementation
 */
@Repository
@PropertySource("classpath:es.properties")
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
     * QueryBuilder visitor used for Elasticsearch search requests
     */
    private static final QueryBuilderVisitor CRITERION_VISITOR = new QueryBuilderVisitor();

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
        return client.admin().indices().prepareCreate(pIndex).get().isAcknowledged();
    }

    @Override
    public boolean delete(String pIndex, String pType, String pId) {
        final DeleteResponse response = client.prepareDelete(pIndex, pType, pId).get();
        return (response.getResult() == Result.DELETED);
    }

    @Override
    public boolean deleteIndex(String pIndex) {
        return client.admin().indices().prepareDelete(pIndex).get().isAcknowledged();

    }

    @Override
    public String[] findIndices() {
        return Iterables
                .toArray(Iterables.transform(client.admin().indices().prepareGetSettings().get().getIndexToSettings(),
                                             (pSetting) -> pSetting.key),
                         String.class);
    }

    @Override
    public <T> T get(String pIndex, String pType, String pId, Class<T> pClass) {
        try {
            final GetResponse response = client.prepareGet(pIndex, pType, pId).get();
            if (!response.isExists()) {
                return null;
            }
            return gson.fromJson(response.getSourceAsString(), pClass);
            // return jsonMapper.readValue(response.getSourceAsBytes(), pClass);
        } catch (final JsonSyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean indexExists(String pName) {
        return client.admin().indices().prepareExists(pName).get().isExists();
    }

    @Override
    public boolean merge(String pIndex, String pType, String pId, Map<String, Object> pMergedPropertiesMap) {
        try {
            final Map<String, Map<String, Object>> mapMap = new HashMap<>();
            final XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
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
            final UpdateResponse response = client.prepareUpdate(pIndex, pType, pId).setDoc(builder.endObject()).get();
            return (response.getResult() == Result.UPDATED);
        } catch (final IOException jpe) {
            throw Throwables.propagate(jpe);
        }
    }

    private void checkDocument(IIndexable pDoc) throws IllegalArgumentException {
        if (Strings.isNullOrEmpty(pDoc.getDocId()) || Strings.isNullOrEmpty(pDoc.getType())) {
            throw new IllegalArgumentException("docId and type are mandatory on an IIndexable object");
        }
    }

    @Override
    public boolean save(String pIndex, IIndexable pDocument) {
        checkDocument(pDocument);
        final IndexResponse response = client.prepareIndex(pIndex, pDocument.getType(), pDocument.getDocId())
                .setSource(gson.toJson(pDocument)).get();
        return (response.getResult() == Result.CREATED);
    }

    @Override
    public void refresh(String pIndex) {
        // To make just saved documents searchable, the associated index must be refreshed
        client.admin().indices().prepareRefresh(pIndex).get();
    }

    @Override
    public <T extends IIndexable> Map<String, Throwable> saveBulk(String pIndex,
            @SuppressWarnings("unchecked") T... pDocuments) throws IllegalArgumentException {
        for (T doc : pDocuments) {
            checkDocument(doc);
        }
        final BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (T doc : pDocuments) {
            bulkRequest.add(client.prepareIndex(pIndex, doc.getType(), doc.getDocId()).setSource(gson.toJson(doc)));
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
        client.admin().indices().prepareRefresh(pIndex).get();
        return errorMap;
    }

    @Override
    public void searchAll(String pIndex, Consumer<SearchHit> pAction) {
        final QueryBuilder qb = QueryBuilders.matchAllQuery();

        SearchResponse scrollResp = client.prepareSearch(pIndex).setScroll(new TimeValue(KEEP_ALIVE_SCROLLING_TIME_MS))
                .setQuery(qb).setSize(DEFAULT_SCROLLING_HITS_SIZE).get();
        // Scroll until no hits are returned
        do {
            for (final SearchHit hit : scrollResp.getHits().getHits()) {
                pAction.accept(hit);
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
            SearchResponse response;
            int errorCount = 0;
            do {
                response = client.prepareSearch(pIndex).setFrom(pPageRequest.getOffset())
                        .setSize(pPageRequest.getPageSize()).get();
                errorCount += response.isTimedOut() ? 1 : 0;
                if (errorCount == 3) {
                    throw new TimeoutException("Get 3 timeouts while attempting to retrieve data");
                }
            } while (response.isTimedOut());
            final SearchHits hits = response.getHits();
            for (final SearchHit hit : hits) {
                results.add(gson.fromJson(hit.getSourceAsString(), pClass));
            }
            return new PageImpl<>(results, pPageRequest, response.getHits().getTotalHits());
        } catch (final JsonSyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public <T> Page<T> search(String pIndex, Class<T> pClass, int pPageSize, ICriterion criterion) {
        return this.search(pIndex, pClass, new PageRequest(0, pPageSize), criterion);
    }

    @Override
    public <T> Page<T> search(String pIndex, Class<T> pClass, Pageable pPageRequest, ICriterion criterion) {
        try {
            final List<T> results = new ArrayList<>();
            final SearchResponse response = client.prepareSearch(pIndex).setQuery(criterion.accept(CRITERION_VISITOR))
                    .setFrom(pPageRequest.getOffset()).setSize(pPageRequest.getPageSize()).get();

            final SearchHits hits = response.getHits();
            for (final SearchHit hit : hits) {
                results.add(gson.fromJson(hit.getSourceAsString(), pClass));
            }
            return new PageImpl<>(results, pPageRequest, response.getHits().getTotalHits());
        } catch (final JsonSyntaxException e) {
            throw Throwables.propagate(e);
        }
    }
}
