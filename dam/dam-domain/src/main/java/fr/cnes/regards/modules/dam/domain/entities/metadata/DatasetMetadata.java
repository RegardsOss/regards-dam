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

    public class DataObjectGroup {

        private final String groupName;

        private final Boolean dataObjectAccess;

        private final Boolean datasetAccess;

        private final Long metaDataObjectAccessFilterPluginId;

        private final Long dataObjectAccessFilterPluginId;

        public DataObjectGroup(String groupName, Boolean datasetAccess, Boolean dataObjectAccess,
                Long metaDataObjectAccessFilterPlugin, Long dataObjectAccessFilterPlugin) {
            super();
            this.groupName = groupName;
            this.dataObjectAccess = dataObjectAccess;
            this.datasetAccess = datasetAccess;
            this.metaDataObjectAccessFilterPluginId = metaDataObjectAccessFilterPlugin;
            this.dataObjectAccessFilterPluginId = dataObjectAccessFilterPlugin;
        }

        public String getGroupName() {
            return groupName;
        }

        public Boolean getDataObjectAccess() {
            return dataObjectAccess;
        }

        public Long getDataObjectAccessFilterPluginId() {
            return dataObjectAccessFilterPluginId;
        }

        public Boolean getDatasetAccess() {
            return datasetAccess;
        }

        public Long getMetaDataObjectAccessFilterPluginId() {
            return metaDataObjectAccessFilterPluginId;
        }

    }

    /**
     * Associated data objects groups.
     * Same groups as dataset ones except some if rights don't permit access to data objects
     */
    private Map<String, DataObjectGroup> dataObjectsGroups = new HashMap<>();

    public Set<String> getDataObjectsGroups() {
        return dataObjectsGroups.keySet();
    }

    public void setDataObjectsGroups(Map<String, DataObjectGroup> dataObjectsGroups) {
        this.dataObjectsGroups = dataObjectsGroups;
    }

    public void addDataObjectGroup(String groupName, Boolean datasetAccess, Boolean dataObjectAccess,
            Long metaDataObjectAccessFilterPlugin, Long dataObjectAccessFilterPlugin) {
        this.dataObjectsGroups.put(groupName, new DataObjectGroup(groupName, datasetAccess, dataObjectAccess,
                metaDataObjectAccessFilterPlugin, dataObjectAccessFilterPlugin));
    }

    public Map<String, DataObjectGroup> getDataObjectsGroupsMap() {
        return dataObjectsGroups;
    }
}
