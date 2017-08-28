package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

/**
 * The unique intend of this interface is to avoid calling IEsRepository.computeDataFilesSummary() on data that do not
 * specify an attribute "files" of type Map&lt;String, DataFile> (or more precisely a map with a key of type "something
 * that is serialized into String")
 * @author oroussel
 */
public interface IDocFiles {
    Map<?, DataFile> getFiles();
}
