package fr.cnes.regards.modules.entities.service.crawler;

public interface ICrawlerService {

    /**
     * Create specified index
     *
     * @return true if acknowledged by Elasticsearch
     */
    boolean createIndex(String pIndex);

    /**
     * Delete specified index
     *
     * @return true if acknowledged by Elasticsearch
     */
    boolean deleteIndex(String pIndex);

    String[] findIndices();

    /**
     * Close Client
     */
    void close();
}
