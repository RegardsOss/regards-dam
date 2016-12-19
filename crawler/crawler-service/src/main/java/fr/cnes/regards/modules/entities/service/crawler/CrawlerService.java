package fr.cnes.regards.modules.entities.service.crawler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.stereotype.Service;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

@Service
public class CrawlerService implements ICrawlerService {

    /**
     * Client to ElasticSearch base
     */
    private final TransportClient client;

    public CrawlerService(/* ICollectionsRequestService pCollectionsRequestService */) {
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
    public void close() {
        this.client.close();
    }
}
