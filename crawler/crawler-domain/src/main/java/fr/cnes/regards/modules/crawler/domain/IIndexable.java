package fr.cnes.regards.modules.crawler.domain;

/**
 * Identifies that something is indexable into Elasticsearch (need and id and a type)
 */
public interface IIndexable {

    String getDocId();

    String getType();
}
