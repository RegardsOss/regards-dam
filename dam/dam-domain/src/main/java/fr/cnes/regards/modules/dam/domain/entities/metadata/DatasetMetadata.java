package fr.cnes.regards.modules.dam.domain.entities.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Dataset metadata. This object is only used by Elasticsearch<br/>
 *
 * Register all access rights for data object level :
 * <ul>
 * <li>Map keys represent groups with full access on metadata (i.e. AccessLevel set to FULL_ACCESS)</li>
 * <li>Map value represents the data access rights for the related group : true if and only if DataAccessLevel set to
 * INHERITED_ACCESS
 * (CUSTOM_ACCESS not supported at the moment)</li>
 * </ul>
 *
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
