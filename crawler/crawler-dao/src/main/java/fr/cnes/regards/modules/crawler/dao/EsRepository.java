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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import fr.cnes.regards.modules.crawler.domain.IIndexable;

/**
 * Elasticsearch repository implementation
 */
@Repository
public class EsRepository implements IEsRepository {

    /**
     * Elasticsearch cluster name
     */
    private static final int ES_PORT = 9300;

    /**
     * Elasticsearch port
     */
    private static final String ES_CLUSTER_NAME = "regards";

    /**
     * Scrolling keeping alive Time in ms when searching into Elasticsearch
     */
    private static final int KEEP_ALIVE_SCROLLING_TIME_MS = 500;

    /**
     * Default number of hits retrieved by scrolling
     */
    private static final int DEFAULT_SCROLLING_HITS_SIZE = 100;

    /**
     * Client to ElasticSearch base
     */
    private final TransportClient client;

    /**
     * Json mapper
     */
    private Gson gson;

    /**
     * Constructor
     * @param pGson JSon mapper bean
     */
    public EsRepository(Gson pGson) {
        this.gson = pGson;
        client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", ES_CLUSTER_NAME).build());
        try {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), ES_PORT));
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
            final SearchResponse response = client.prepareSearch(pIndex).setFrom(pPageRequest.getOffset())
                    .setSize(pPageRequest.getPageSize()).get();
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
