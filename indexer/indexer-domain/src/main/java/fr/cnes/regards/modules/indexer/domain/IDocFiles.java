package fr.cnes.regards.modules.indexer.domain;

import com.google.common.collect.Multimap;

/**
 * The unique intend of this interface is to avoid calling IEsRepository.computeDataFilesSummary() on data that do not
 * specify an attribute "files" whose type is Multimap&lt;String, DataFile> (or more precisely a multimap with a key of
 * type "something that is serialized into String")
 * @author oroussel
 */
public interface IDocFiles {
    Multimap<?, DataFile> getFiles();
}
