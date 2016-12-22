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
import org.elasticsearch.client.transport.TransportClient;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

@Repository
public class EsRepository implements IEsRepository {

    /**
     * Client to ElasticSearch base
     */
    private final TransportClient client;

    /**
     * Json mapper
     */
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public EsRepository(/* ICollectionsRequestService pCollectionsRequestService */) {
        this.client = new PreBuiltTransportClient(Settings.EMPTY);
        try {
            this.client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        } catch (final UnknownHostException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public boolean createIndex(String pIndex) {
        return client.admin().indices().prepareCreate(pIndex).get().isAcknowledged();
    }

    @Override
    public boolean deleteIndex(String pIndex) {
        return client.admin().indices().prepareDelete(pIndex).get().isAcknowledged();

    }

    @Override
    public String[] findIndices() {
        return Iterables.toArray(Iterables
                .transform(client.admin().indices().prepareGetSettings().get().getIndexToSettings(), (c) -> c.key),
                                 String.class);
    }

    @Override
    public boolean indexExists(String pName) {
        return client.admin().indices().prepareExists(pName).get().isExists();
    }

    @Override
    public boolean save(String index, String type, String id, Object document) {
        try {
            final IndexResponse response = client.prepareIndex(index, type, id)
                    .setSource(jsonMapper.writeValueAsBytes(document)).get();
            return (response.getResult() == Result.CREATED);
        } catch (final JsonProcessingException jpe) {
            throw Throwables.propagate(jpe);
        }
    }

    @Override
    public Map<String, Throwable> saveBulk(String index, String type, Map<String, ?> documentMap) {
        try {
            final BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (final Map.Entry<String, ?> entry : documentMap.entrySet()) {
                bulkRequest.add(client.prepareIndex(index, type, entry.getKey())
                        .setSource(jsonMapper.writeValueAsBytes(entry.getValue())));
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
        } catch (final JsonProcessingException jpe) {
            throw Throwables.propagate(jpe);
        }
    }

    @Override
    public <T> T get(String index, String type, String id, Class<T> clazz) {
        try {
            final GetResponse response = client.prepareGet(index, type, id).get();
            if (!response.isExists()) {
                return null;
            }
            return jsonMapper.readValue(response.getSourceAsBytes(), clazz);
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean delete(String index, String type, String id) {
        final DeleteResponse response = client.prepareDelete(index, type, id).get();
        return (response.getResult() == Result.DELETED);
    }

    @Override
    public boolean merge(String index, String type, String id, Map<String, Object> mergedPropertiesMap) {
        try {
            final Map<String, Map<String, Object>> mapMap = new HashMap<>();
            final XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
            for (final Map.Entry<String, Object> entry : mergedPropertiesMap.entrySet()) {
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
            final UpdateResponse response = client.prepareUpdate(index, type, id).setDoc(builder.endObject()).get();
            return (response.getResult() == Result.UPDATED);
        } catch (final IOException jpe) {
            throw Throwables.propagate(jpe);
        }
    }

    @Override
    public <T> Page<T> searchAllLimited(String index, Class<T> clazz, int pageSize) {
        return this.searchAllLimited(index, clazz, new PageRequest(0, pageSize));
    }

    @Override
    public <T> Page<T> searchAllLimited(String index, Class<T> clazz, Pageable pageRequest) {
        try {
            final List<T> results = new ArrayList<>();
            final SearchResponse response = client.prepareSearch(index).setFrom(pageRequest.getOffset())
                    .setSize(pageRequest.getPageSize()).get();
            final SearchHits hits = response.getHits();
            for (final SearchHit hit : hits) {
                results.add(jsonMapper.readValue(hit.getSourceAsString(), clazz));
            }
            return new PageImpl<>(results, pageRequest, response.getHits().getTotalHits());
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }

    }

    @Override
    public void searchAll(String index, Consumer<SearchHit> action) {
        final QueryBuilder qb = QueryBuilders.matchAllQuery();

        SearchResponse scrollResp = client.prepareSearch(index).setScroll(new TimeValue(500)).setQuery(qb).setSize(100)
                .get();
        // Scroll until no hits are returned
        do {
            for (final SearchHit hit : scrollResp.getHits().getHits()) {
                action.accept(hit);
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(500)).execute()
                    .actionGet();
        } while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while
                                                              // loop.
    }

    @Override
    public void close() {
        this.client.close();
    }
}
