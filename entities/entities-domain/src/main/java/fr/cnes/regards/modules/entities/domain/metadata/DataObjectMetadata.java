package fr.cnes.regards.modules.entities.domain.metadata;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * DataObject metadata. This object is only used by Elasticsearch
 * @author oroussel
 */
public class DataObjectMetadata {

    /**
     * Multimap { groupName, (datasetIpIds) }
     */
    private Multimap<String, String> groups = HashMultimap.create();

    /**
     * Multimap { modelId, (datasetIpIds) }
     */
    private Multimap<Long, String> modelIds = HashMultimap.create();

    public void addGroup(String groupName, String datasetIpId) {
        groups.put(groupName, datasetIpId);
    }

    public void removeGroup(String groupName, String datasetIpId) {
        groups.remove(groupName, datasetIpId);
    }

    /**
     * Remove given ipId from all values (groups multimap AND modelIds multimap)
     */
    public void removeDatasetIpId(String datasetIpId) {
        for (Iterator<String> i = groups.values().iterator(); i.hasNext(); ) {
            if (i.next().equals(datasetIpId)) {
                i.remove();
            }
        }
        for (Iterator<String> i = modelIds.values().iterator(); i.hasNext(); ) {
            if (i.next().equals(datasetIpId)) {
                i.remove();
            }
        }

    }

    public Set<String> getGroups() {
        return groups.keySet();
    }

    public void addModelId(long modelId, String datasetIpId) {
        modelIds.put(modelId, datasetIpId);
    }

    public void removeModelId(long modelId, String datasetIpId) {
        modelIds.remove(modelId, datasetIpId);
    }

    public Set<Long> getModelIds() {
        return modelIds.keySet();
    }

}

