package fr.cnes.regards.modules.entities.domain.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Dataset metadata. This object is only used by Elasticsearch
 * @author oroussel
 */
public class DatasetMetadata {

    /**
     * Associated data objects groups.
     * Same groups as dataset ones except some if rights don't permit access to data objects
     */
    private Map<String, Boolean> dataObjectsGroups = new HashMap<>();

    public Set<String> getDataObjectsGroups() {
        return dataObjectsGroups.keySet();
    }

    public void setDataObjectsGroups(Map<String, Boolean> dataObjectsGroups) {
        this.dataObjectsGroups = dataObjectsGroups;
    }

    public Map<String, Boolean> getDataObjectsGroupsMap() {
        return dataObjectsGroups;
    }
}
