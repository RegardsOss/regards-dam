package fr.cnes.regards.modules.entities.domain.metadata;

import java.util.HashSet;
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
    private Set<String> dataObjectsGroups = new HashSet<>();

    public Set<String> getDataObjectsGroups() {
        return dataObjectsGroups;
    }

    public void setDataObjectsGroups(Set<String> dataObjectsGroups) {
        this.dataObjectsGroups = dataObjectsGroups;
    }
}
