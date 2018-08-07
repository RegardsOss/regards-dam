package fr.cnes.regards.modules.dam.domain.entities.metadata;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * DataObject metadata. This object is only used by Elasticsearch
 * @author oroussel
 */
public class DataObjectMetadata {

    /**
     * Multimap { groupName, (datasetIpIds) }
     */
    private final Multimap<String, DatasetAccessRight> groups = HashMultimap.create();

    /**
     * Multimap { modelId, (datasetIpIds) }
     */
    private final Multimap<Long, String> modelIds = HashMultimap.create();

    /**
     * @param dataAccessGranted true if data access is granted for (group, dataset ip id)
     */
    public void addGroup(String groupName, String datasetIpId, boolean dataAccessGranted) {
        groups.put(groupName, new DatasetAccessRight(datasetIpId, dataAccessGranted));
    }

    public void removeGroup(String groupName, String datasetIpId) {
        // accessRight value doesn't count, DatasetAccessRight only use datasetIpId for equality
        groups.remove(groupName, new DatasetAccessRight(datasetIpId, true));
    }

    /**
     * Remove given ipId from all values (groups multimap AND modelIds multimap)
     */
    public void removeDatasetIpId(String datasetIpId) {
        for (Iterator<DatasetAccessRight> i = groups.values().iterator(); i.hasNext();) {
            if (i.next().getDataset().equals(datasetIpId)) {
                i.remove();
            }
        }
        for (Iterator<String> i = modelIds.values().iterator(); i.hasNext();) {
            if (i.next().equals(datasetIpId)) {
                i.remove();
            }
        }

    }

    public Set<String> getGroups() {
        return groups.keySet();
    }

    /**
     * Retrieve a map of { group -> data access}. data access is a true boolean if at least one associated dataset
     * grants
     * data access, false otherwise
     */
    public Map<String, Boolean> getGroupsAccessRightsMap() {
        return Maps.transformValues(groups
                .asMap(), rights -> rights.stream().anyMatch(datasetAccessRight -> datasetAccessRight.isAccessRight()));
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

    @SuppressWarnings("unused")
    private static final class DatasetAccessRight {

        private String dataset;

        /**
         * <b>Data</b> access right
         */
        private boolean dataAccessRight;

        public DatasetAccessRight() {
        }

        public DatasetAccessRight(String dataset, boolean accessRight) {
            this.dataset = dataset;
            this.dataAccessRight = accessRight;
        }

        public String getDataset() {
            return dataset;
        }

        public void setDataset(String dataset) {
            this.dataset = dataset;
        }

        public boolean isAccessRight() {
            return dataAccessRight;
        }

        public void setAccessRight(boolean accessRight) {
            this.dataAccessRight = accessRight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass())) {
                return false;
            }
            DatasetAccessRight that = (DatasetAccessRight) o;
            return Objects.equals(dataset, that.dataset);
        }

        @Override
        public int hashCode() {

            return Objects.hash(dataset);
        }
    }
}
