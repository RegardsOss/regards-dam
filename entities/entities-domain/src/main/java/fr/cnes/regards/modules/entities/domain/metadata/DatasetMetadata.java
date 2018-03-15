package fr.cnes.regards.modules.entities.domain.metadata;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dataset metadata. This object is only used by Elasticsearch
 * @author oroussel
 */
public class DatasetMetadata {

    /**
     * Associated data objects groups.
     * Same groups as dataset ones except some if rights don't permit access to data objects
     */
    private Set<AccessRight> dataObjectsGroups = new HashSet<>();

    public Set<String> getDataObjectsGroups() {
        return dataObjectsGroups.stream().map(AccessRight::getGroup).collect(Collectors.toSet());
    }

    public void setDataObjectsGroups(Map<String, Boolean> dataObjectsGroups) {
        this.dataObjectsGroups = dataObjectsGroups.entrySet().stream()
                .map(e -> new AccessRight(e.getKey(), e.getValue())).collect(Collectors.toSet());
    }

    public Map<String, Boolean> getDataObjectsGroupsMap() {
        return dataObjectsGroups.stream().collect(Collectors.toMap(AccessRight::getGroup, AccessRight::isAccessRight));
    }

    private static final class AccessRight {

        private String group;

        private boolean accessRight;

        public AccessRight() {

        }

        public AccessRight(String group, boolean accessRight) {
            this.group = group;
            this.accessRight = accessRight;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public boolean isAccessRight() {
            return accessRight;
        }

        public void setAccessRight(boolean accessRight) {
            this.accessRight = accessRight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AccessRight that = (AccessRight) o;
            return Objects.equals(group, that.group);
        }

        @Override
        public int hashCode() {

            return Objects.hash(group);
        }
    }
}
