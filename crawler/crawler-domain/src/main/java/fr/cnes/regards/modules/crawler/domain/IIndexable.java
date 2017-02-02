package fr.cnes.regards.modules.crawler.domain;

/**
 * Identifies that something is indexable into Elasticsearch (need andid and a type)
 * @author oroussel
 */
public interface IIndexable {

    String getDocId();

    String getType();
}
